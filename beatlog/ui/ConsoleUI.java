package ch.tbz.beatlog.ui;

import ch.tbz.beatlog.controller.Controller;
import ch.tbz.beatlog.domain.Song;
import ch.tbz.beatlog.persistence.JsonRepository;
import ch.tbz.beatlog.persistence.Repository;
import ch.tbz.beatlog.service.LibraryService;
import ch.tbz.beatlog.service.PlaylistService;
import ch.tbz.beatlog.service.SmartPlaylistStrategy;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class ConsoleUI {

    private final Scanner sc = new Scanner(System.in);
    private static final String MOOD_HINT = "(happy / sad / aggressive, optional)";

    public void start() {
        Properties config = new Properties();
        config.setProperty("data.dir", "data");
        config.setProperty("songs.file", "songs.json");
        config.setProperty("sessions.file", "sessions.json");

        Repository repo = new JsonRepository(config);
        LibraryService library = new LibraryService(repo);
        Controller controller = new Controller(library);
        PlaylistService playlists = new PlaylistService();

        System.out.println("=== BeatLog – Songs, Sessions & Smart Playlists ===");
        while (true) {
            System.out.println("""
                \nMenü:
                1) Songs anzeigen
                2) Song anlegen
                3) Song bearbeiten
                4) Song löschen
                5) Session loggen
                6) Sessions anzeigen
                7) Songs filtern (Mood/Rating/Tag)
                8) Smarte Playlist generieren
                9) Backup exportieren (alle Daten)
                10) Backup importieren (alle Daten)
                0) Beenden
                """);
            System.out.print("> ");
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> listSongs(controller);
                    case "2" -> createSong(controller);
                    case "3" -> editSong(controller);
                    case "4" -> deleteSong(controller);
                    case "5" -> logSession(controller);
                    case "6" -> listSessions(controller);
                    case "7" -> filterSongs(controller, playlists);
                    case "8" -> generateSmartPlaylist(controller, playlists);
                    case "9" -> doBackup(library);
                    case "10" -> doRestore(library);
                    case "0" -> { System.out.println("Tschüss!"); return; }
                    default -> System.out.println("Unbekannte Option.");
                }
            } catch (Exception e) {
                System.out.println("Fehler: " + e.getMessage());
            }
        }
    }

    // === UC-01 Songs ===
    private void listSongs(Controller controller) {
        List<Song> songs = controller.getAllSongs();
        if (songs.isEmpty()) {
            System.out.println("(leer)");
            return;
        }
        int i = 1;
        for (Song s : songs) {
            System.out.printf("%d) [%s] %s — %s (%d★, %s)%n",
                    i++, s.getId(), s.getArtist(), s.getTitle(), s.getRating(), s.getMood());
        }
    }

    private void createSong(Controller controller) {
        System.out.println("— Song anlegen —");
        String title = ask("Titel", false);
        String artist = ask("Artist", false);
        String genre = ask("Genre", false);
        int year = askInt("Jahr (z.B. 2022)", 1900, 2100);
        int duration = askInt("Dauer in Sekunden", 1, 60 * 60 * 24);
        String mood = ask("Mood " + MOOD_HINT, true);
        int rating = askInt("Rating (1..10)", 1, 10);
        String tagsRaw = ask("Tags (kommagetrennt, optional)", true);
        Set<String> tags = parseTags(tagsRaw);

        String id = controller.createSong(title, artist, genre, year, duration, mood, rating, tags);
        System.out.println("Angelegt mit ID: " + id);
    }

    private void editSong(Controller controller) {
        System.out.println("— Song bearbeiten —");
        var all = controller.getAllSongs();
        if (all.isEmpty()) {
            System.out.println("Keine Songs vorhanden.");
            return;
        }
        listSongs(controller);
        int idx = askInt("Welchen Song bearbeiten? (Nummer)", 1, all.size());
        Song old = all.get(idx - 1);

        String title = askDefault("Titel", old.getTitle());
        String artist = askDefault("Artist", old.getArtist());
        String genre = askDefault("Genre", old.getGenre());
        int year = askIntDefault("Jahr", 1900, 2100, old.getYear());
        int duration = askIntDefault("Dauer (Sek)", 1, 60 * 60 * 24, old.getDurationSec());
        String mood = askDefault("Mood " + MOOD_HINT, old.getMood() == null ? "" : old.getMood());
        int rating = askIntDefault("Rating (1..10)", 1, 10, old.getRating());
        String tagsRaw = askDefault("Tags", old.getTags() == null ? "" : String.join(",", old.getTags()));
        Set<String> tags = parseTags(tagsRaw);

        controller.updateSong(old.getId(), title, artist, genre, year, duration, mood, rating, tags);
        System.out.println("Song aktualisiert.");
    }

    private void deleteSong(Controller controller) {
        System.out.println("— Song löschen —");
        var all = controller.getAllSongs();
        if (all.isEmpty()) {
            System.out.println("Keine Songs vorhanden.");
            return;
        }
        listSongs(controller);
        int idx = askInt("Welchen Song löschen? (Nummer)", 1, all.size());
        Song s = all.get(idx - 1);
        System.out.print("Sicher löschen? (j/N): ");
        if (sc.nextLine().trim().equalsIgnoreCase("j")) {
            controller.deleteSong(s.getId());
            System.out.println("Gelöscht.");
        } else {
            System.out.println("Abgebrochen.");
        }
    }

    // === UC-02 Sessions ===
    private void logSession(Controller controller) {
        var songs = controller.getAllSongs();
        if (songs.isEmpty()) {
            System.out.println("Keine Songs vorhanden.");
            return;
        }
        listSongs(controller);
        int idx = askInt("Welchen Song gehört?", 1, songs.size());
        var song = songs.get(idx - 1);

        String mood = ask("Mood beim Hören " + MOOD_HINT, true);
        String note = ask("Notiz (optional)", true);
        String ratingStr = ask("Neues Rating (leer = keins)", true);
        Integer rating = ratingStr.isBlank() ? null : tryParseInt(ratingStr);

        controller.logListeningSession(song.getId(), mood, note, rating);
        System.out.printf("Session gespeichert für %s — %s%n", song.getArtist(), song.getTitle());
    }

    private void listSessions(Controller controller) {
        var sessions = controller.getAllSessions();
        if (sessions.isEmpty()) {
            System.out.println("Keine Sessions vorhanden.");
            return;
        }
        sessions.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        int i = 1;
        DateTimeFormatter fmt = DateTimeFormatter.ISO_INSTANT;
        for (var s : sessions) {
            var songOpt = controller.getSong(s.getSongId());
            String label = songOpt.map(x -> x.getArtist() + " — " + x.getTitle()).orElse("(Song gelöscht)");
            System.out.printf("%d) %s | %s | Mood: %s | Note: %s%n",
                    i++, fmt.format(s.getTimestamp()), label,
                    s.getMood() == null ? "-" : s.getMood(),
                    s.getNote() == null ? "-" : s.getNote());
        }
    }

    // === UC-04 Filter ===
    private void filterSongs(Controller controller, PlaylistService playlists) {
        System.out.println("— Filter —");
        String mood = ask("Mood-Filter " + MOOD_HINT.replace(", optional", ""), true);
        String minRatingStr = ask("Mindest-Rating (leer = beliebig)", true);
        Integer minRating = minRatingStr.isBlank() ? null : tryParseInt(minRatingStr);
        String tag = ask("Tag (leer = beliebig)", true);

        var filtered = playlists.filterSongs(controller.getAllSongs(),
                mood.isBlank() ? null : mood, minRating, tag.isBlank() ? null : tag);

        System.out.printf("Filter angewendet → %d Treffer%n", filtered.size());
        if (filtered.isEmpty()) return;

        int i = 1;
        for (var s : filtered) {
            System.out.printf("%d) %s — %s (%d★, %s)%n",
                    i++, s.getArtist(), s.getTitle(), s.getRating(), s.getMood());
        }
    }

    // === UC-03 Smarte Playlist ===
    private void generateSmartPlaylist(Controller controller, PlaylistService playlists) {
        var all = controller.getAllSongs();
        if (all.isEmpty()) {
            System.out.println("Keine Songs vorhanden.");
            return;
        }

        System.out.println("— Smarte Playlist —");
        var strategies = playlists.getAvailableStrategies();
        for (int i = 0; i < strategies.size(); i++) {
            System.out.printf("%d) %s%n", i + 1, strategies.get(i).getName());
        }
        int choice = askInt("Strategie wählen", 1, strategies.size());
        SmartPlaylistStrategy strategy = strategies.get(choice - 1);
        System.out.println("Strategie: " + strategy.getName());

        String mood = ask("Mood-Filter " + MOOD_HINT.replace(", optional", ""), true);
        String minRatingStr = ask("Mindest-Rating (leer = keiner)", true);
        Integer minRating = minRatingStr.isBlank() ? null : tryParseInt(minRatingStr);
        String tag = ask("Tag-Filter (leer = keiner)", true);

        var filtered = playlists.filterSongs(all,
                mood.isBlank() ? null : mood, minRating, tag.isBlank() ? null : tag);

        System.out.printf("Vor Strategie: %d Songs nach Filter%n", filtered.size());
        if (filtered.isEmpty()) {
            System.out.println("Keine Songs nach diesen Kriterien.");
            return;
        }

        var playlist = strategy.generate(filtered);

        System.out.println("\n🎧 Playlist – " + strategy.getName() + " (" + playlist.size() + " Titel)");
        int i = 1;
        for (var s : playlist) {
            System.out.printf("%d) %s — %s (%d★, %s)%n",
                    i++, s.getArtist(), s.getTitle(), s.getRating(), s.getMood());
        }
    }

    // === UC-05: Backup/Restore ===
    private void doBackup(LibraryService library) {
        String path = ask("Export-Datei (Standard: data/backup.json)", true);
        if (path.isBlank()) path = "data/backup.json";
        library.backupToFile(path);
        System.out.println("Backup gespeichert: " + path);
    }

    private void doRestore(LibraryService library) {
        String path = ask("Import-Datei (z.B. data/backup.json)", false);
        System.out.print("WARNUNG: Das überschreibt lokale Songs & Sessions. Fortfahren? (j/N): ");
        String ok = sc.nextLine().trim().toLowerCase();
        if (!ok.equals("j") && !ok.equals("ja")) {
            System.out.println("Abgebrochen.");
            return;
        }
        library.restoreFromFile(path);
        System.out.println("Import abgeschlossen.");
    }

    // === Helpers ===
    private String ask(String label, boolean optional) {
        while (true) {
            System.out.print(label + ": ");
            String v = sc.nextLine();
            if (optional) return v.trim();
            if (!v.trim().isEmpty()) return v.trim();
            System.out.println("Eingabe darf nicht leer sein.");
        }
    }

    private String askDefault(String label, String def) {
        System.out.print(label + " [" + def + "]: ");
        String v = sc.nextLine();
        return v.trim().isEmpty() ? def : v.trim();
    }

    private int askInt(String label, int min, int max) {
        while (true) {
            System.out.print(label + ": ");
            try {
                int n = Integer.parseInt(sc.nextLine().trim());
                if (n >= min && n <= max) return n;
                System.out.printf("Zahl muss zwischen %d und %d liegen.%n", min, max);
            } catch (Exception e) {
                System.out.println("Bitte Zahl eingeben.");
            }
        }
    }

    private int askIntDefault(String label, int min, int max, int def) {
        System.out.print(label + " [" + def + "]: ");
        String raw = sc.nextLine().trim();
        if (raw.isEmpty()) return def;
        try {
            int n = Integer.parseInt(raw);
            return (n >= min && n <= max) ? n : def;
        } catch (Exception e) {
            return def;
        }
    }

    private Integer tryParseInt(String raw) {
        try { return Integer.parseInt(raw.trim()); } catch (Exception e) { return null; }
    }

    private Set<String> parseTags(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptySet();
        String[] parts = raw.split(",");
        Set<String> set = new LinkedHashSet<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) set.add(t);
        }
        return set;
    }
}
    