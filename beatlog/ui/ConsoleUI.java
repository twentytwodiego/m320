package ch.tbz.beatlog.ui;

import ch.tbz.beatlog.controller.Controller;
import ch.tbz.beatlog.domain.Song;
import ch.tbz.beatlog.persistence.JsonRepository;
import ch.tbz.beatlog.persistence.Repository;
import ch.tbz.beatlog.service.LibraryService;

import java.util.*;

public class ConsoleUI {

    private final Scanner sc = new Scanner(System.in);

    public void start() {
        Properties config = new Properties();
        config.setProperty("data.dir", "data");
        config.setProperty("songs.file", "songs.json");
        config.setProperty("sessions.file", "sessions.json");

        Repository repo = new JsonRepository(config);
        LibraryService service = new LibraryService(repo);
        Controller controller = new Controller(service);

        System.out.println("=== BeatLog – Songs & Sessions (UC-01 + UC-02) ===");
        while (true) {
            System.out.println("""
                \nMenü:
                1) Songs anzeigen
                2) Song anlegen
                3) Song bearbeiten
                4) Song löschen
                5) Session loggen
                6) Sessions anzeigen
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
                    case "0" -> { System.out.println("Tschüss!"); return; }
                    default -> System.out.println("Unbekannte Option.");
                }
            } catch (Exception e) {
                System.out.println("Fehler: " + e.getMessage());
            }
        }
    }

    // ---- SONGS ----
    private void listSongs(Controller controller) {
        List<Song> songs = controller.getAllSongs();
        if (songs.isEmpty()) {
            System.out.println("(leer)");
            return;
        }
        int i = 1;
        for (Song s : songs) {
            System.out.printf("%d) [%s] %s%n", i++, s.getId(), s.toString());
        }
    }

    private void createSong(Controller controller) {
        System.out.println("— Song anlegen —");
        String title = ask("Titel", false);
        String artist = ask("Artist", false);
        String genre = ask("Genre", false);
        int year = askInt("Jahr (z.B. 2022)", 1900, 2100);
        int duration = askInt("Dauer in Sekunden", 1, 60 * 60 * 24);
        String mood = ask("Mood (optional, leer erlaubt)", true);     // jetzt wirklich optional
        int rating = askInt("Rating (1..10)", 1, 10);
        String tagsRaw = ask("Tags (kommagetrennt, optional)", true); // jetzt wirklich optional
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
        int idx = askInt("Welchen Eintrag bearbeiten? (Nummer)", 1, all.size());
        Song old = all.get(idx - 1);

        String title = askDefault("Titel", old.getTitle());
        String artist = askDefault("Artist", old.getArtist());
        String genre = askDefault("Genre", old.getGenre());
        int year = askIntDefault("Jahr", 1900, 2100, old.getYear());
        int duration = askIntDefault("Dauer (Sek)", 1, 60 * 60 * 24, old.getDurationSec());
        String mood = askDefault("Mood (leer erlaubt)", old.getMood() == null ? "" : old.getMood());
        int rating = askIntDefault("Rating (1..10)", 1, 10, old.getRating());
        String tagsRaw = askDefault("Tags (kommagetrennt)", old.getTags() == null ? "" : String.join(",", old.getTags()));
        Set<String> tags = parseTags(tagsRaw);

        controller.updateSong(old.getId(), title, artist, genre, year, duration, mood, rating, tags);
        System.out.println("Aktualisiert.");
    }

    private void deleteSong(Controller controller) {
        System.out.println("— Song löschen —");
        var all = controller.getAllSongs();
        if (all.isEmpty()) {
            System.out.println("Keine Songs vorhanden.");
            return;
        }
        listSongs(controller);
        int idx = askInt("Welchen Eintrag löschen? (Nummer)", 1, all.size());
        Song s = all.get(idx - 1);
        System.out.print("Sicher löschen? (j/N): ");
        String confirm = sc.nextLine().trim().toLowerCase();
        if (confirm.equals("j") || confirm.equals("ja")) {
            controller.deleteSong(s.getId());
            System.out.println("Gelöscht.");
        } else {
            System.out.println("Abgebrochen.");
        }
    }

    // ---- SESSIONS ----
    private void logSession(Controller controller) {
        var songs = controller.getAllSongs();
        if (songs.isEmpty()) {
            System.out.println("Keine Songs vorhanden. Bitte zuerst anlegen.");
            return;
        }
        listSongs(controller);
        int idx = askInt("Welchen Song hast du gehört?", 1, songs.size());
        var song = songs.get(idx - 1);

        String mood = ask("Mood beim Hören (optional)", true); // optional erlaubt
        String note = ask("Notiz (optional)", true);            // optional erlaubt
        String ratingStr = ask("Neues Rating (leer für keins)", true);
        Integer rating = ratingStr.isBlank() ? null : Integer.parseInt(ratingStr);

        controller.logListeningSession(song.getId(), mood, note, rating);
        System.out.println("Session gespeichert.");
    }

    private void listSessions(Controller controller) {
        var sessions = controller.getAllSessions();
        if (sessions.isEmpty()) {
            System.out.println("Keine Sessions vorhanden.");
            return;
        }
        int i = 1;
        for (var s : sessions) {
            System.out.println(i++ + ") " + s);
        }
    }

    // ---- Helpers (robust, mit optional-Flag) ----
    private String ask(String label, boolean optional) {
        while (true) {
            System.out.print(label + ": ");
            String v = sc.nextLine();
            if (optional) {
                return v.trim(); // darf leer sein
            }
            if (!v.trim().isEmpty()) {
                return v.trim();
            }
            System.out.println("Eingabe darf nicht leer sein.");
        }
    }

    // Beibehaltung der alten Signatur für Pflichtfelder
    private String ask(String label) {
        return ask(label, false);
    }

    private String askDefault(String label, String def) {
        System.out.print(label + " [" + (def == null ? "" : def) + "]: ");
        String v = sc.nextLine();
        return v.trim().isEmpty() ? (def == null ? "" : def) : v.trim();
    }

    private int askInt(String label, int min, int max) {
        while (true) {
            System.out.print(label + ": ");
            try {
                int n = Integer.parseInt(sc.nextLine().trim());
                if (n < min || n > max) {
                    System.out.println("Zahl muss zwischen %d und %d liegen.".formatted(min, max));
                    continue;
                }
                return n;
            } catch (NumberFormatException e) {
                System.out.println("Bitte eine Zahl eingeben.");
            }
        }
    }

    private int askIntDefault(String label, int min, int max, int def) {
        while (true) {
            System.out.print(label + " [" + def + "]: ");
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) return def;
            try {
                int n = Integer.parseInt(raw);
                if (n < min || n > max) {
                    System.out.println("Zahl muss zwischen %d und %d liegen.".formatted(min, max));
                    continue;
                }
                return n;
            } catch (NumberFormatException e) {
                System.out.println("Bitte eine Zahl eingeben.");
            }
        }
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
