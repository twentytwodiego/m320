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

        Repository repo = new JsonRepository(config);
        LibraryService service = new LibraryService(repo);
        Controller controller = new Controller(service);

        System.out.println("=== BeatLog (UC-01) – Songs verwalten ===");
        while (true) {
            System.out.println("""
                \nMenü:
                1) Songs anzeigen
                2) Song anlegen
                3) Song bearbeiten
                4) Song löschen
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
                    case "0" -> { System.out.println("Tschüss!"); return; }
                    default -> System.out.println("Unbekannte Option.");
                }
            } catch (Exception e) {
                System.out.println("Fehler: " + e.getMessage());
            }
        }
    }

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
        String title = ask("Titel");
        String artist = ask("Artist");
        String genre = ask("Genre");
        int year = askInt("Jahr (z.B. 2022)", 1900, 2100);
        int duration = askInt("Dauer in Sekunden", 1, 60*60*24);
        String mood = ask("Mood (optional, leer erlaubt)");
        int rating = askInt("Rating (1..10)", 1, 10);
        Set<String> tags = parseTags(ask("Tags (kommagetrennt, optional)"));

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
        int duration = askIntDefault("Dauer (Sek)", 1, 60*60*24, old.getDurationSec());
        String mood = askDefault("Mood (leer erlaubt)", old.getMood() == null ? "" : old.getMood());
        int rating = askIntDefault("Rating (1..10)", 1, 10, old.getRating());
        Set<String> tags = parseTags(askDefault("Tags (kommagetrennt)", old.getTags() == null ? "" : String.join(",", old.getTags())));

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

    // Helpers

    private String ask(String label) {
        while (true) {
            System.out.print(label + ": ");
            String v = sc.nextLine().trim();
            if (!v.isEmpty() || label.toLowerCase().contains("mood") || label.toLowerCase().contains("tags"))
                return v;
            System.out.println("Eingabe darf nicht leer sein.");
        }
    }

    private String askDefault(String label, String def) {
        System.out.print(label + " [" + (def == null ? "" : def) + "]: ");
        String v = sc.nextLine().trim();
        return v.isEmpty() ? (def == null ? "" : def) : v;
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
