package ch.tbz.beatlog.persistence;

import ch.tbz.beatlog.domain.Song;
import ch.tbz.beatlog.domain.ListeningSession;
import ch.tbz.beatlog.domain.DataSnapshot;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public class JsonRepository implements Repository {

    private final Gson gson;
    private final Path dataDir;
    private final Path songsFile;
    private final Path sessionsFile;

    private final Map<String, Song> songs = new LinkedHashMap<>();
    private final List<ListeningSession> sessions = new ArrayList<>();
    private boolean loaded = false;

    public JsonRepository(Properties config) {
        String dir = Objects.requireNonNullElse(config.getProperty("data.dir"), "data");
        String songsName = Objects.requireNonNullElse(config.getProperty("songs.file"), "songs.json");
        String sessionsName = Objects.requireNonNullElse(config.getProperty("sessions.file"), "sessions.json");

        this.dataDir = Path.of(dir);
        this.songsFile = dataDir.resolve(songsName);
        this.sessionsFile = dataDir.resolve(sessionsName);

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, t, ctx) ->
                        new JsonPrimitive(src.toString()))
                .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, t, ctx) ->
                        Instant.parse(json.getAsString()))
                .create();
    }

    private void ensureLoaded() {
        if (loaded) return;
        try {
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);

            // Songs laden
            if (Files.exists(songsFile)) {
                try (Reader r = Files.newBufferedReader(songsFile)) {
                    Type listType = new TypeToken<List<Song>>() {}.getType();
                    List<Song> list = gson.fromJson(r, listType);
                    if (list != null) for (Song s : list) songs.put(s.getId(), s);
                }
            }

            // Sessions laden
            if (Files.exists(sessionsFile)) {
                try (Reader r = Files.newBufferedReader(sessionsFile)) {
                    Type listType = new TypeToken<List<ListeningSession>>() {}.getType();
                    List<ListeningSession> list = gson.fromJson(r, listType);
                    if (list != null) sessions.addAll(list);
                }
            }

            loaded = true;
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Laden: " + e.getMessage(), e);
        }
    }

    private void persistSongs() {
        try (Writer w = Files.newBufferedWriter(songsFile)) {
            gson.toJson(new ArrayList<>(songs.values()), w);
        } catch (IOException e) {
            throw new RuntimeException("Konnte Songs nicht speichern: " + e.getMessage(), e);
        }
    }

    private void persistSessions() {
        try (Writer w = Files.newBufferedWriter(sessionsFile)) {
            gson.toJson(sessions, w);
        } catch (IOException e) {
            throw new RuntimeException("Konnte Sessions nicht speichern: " + e.getMessage(), e);
        }
    }

    // ---- SONGS ----
    @Override
    public void saveSong(Song song) {
        ensureLoaded();
        songs.put(song.getId(), song);
        persistSongs();
    }

    @Override
    public Optional<Song> findSongById(String id) {
        ensureLoaded();
        return Optional.ofNullable(songs.get(id));
    }

    @Override
    public List<Song> loadAllSongs() {
        ensureLoaded();
        return new ArrayList<>(songs.values());
    }

    @Override
    public void deleteSong(String id) {
        ensureLoaded();
        songs.remove(id);
        persistSongs();
    }

    // ---- SESSIONS ----
    @Override
    public void saveSession(ListeningSession session) {
        ensureLoaded();
        sessions.add(session);
        persistSessions();
    }

    @Override
    public List<ListeningSession> loadAllSessions() {
        ensureLoaded();
        return new ArrayList<>(sessions);
    }

    // ---- UC-05 SNAPSHOT/EXPORT/IMPORT ----
    @Override
    public DataSnapshot getSnapshot() {
        ensureLoaded();
        return new DataSnapshot(new ArrayList<>(songs.values()), new ArrayList<>(sessions));
    }

    @Override
    public void replaceAll(DataSnapshot snapshot) {
        ensureLoaded();
        songs.clear();
        sessions.clear();

        if (snapshot.getSongs() != null) {
            for (Song s : snapshot.getSongs()) {
                songs.put(s.getId(), s);
            }
        }
        if (snapshot.getSessions() != null) {
            sessions.addAll(snapshot.getSessions());
        }

        // persistieren
        persistSongs();
        persistSessions();
    }

    @Override
    public void exportAll(String filePath) {
        ensureLoaded();
        DataSnapshot snap = getSnapshot();
        Path target = Path.of(filePath);
        try {
            if (target.getParent() != null && !Files.exists(target.getParent())) {
                Files.createDirectories(target.getParent());
            }
            try (Writer w = Files.newBufferedWriter(target)) {
                gson.toJson(snap, w);
            }
        } catch (IOException e) {
            throw new RuntimeException("Export fehlgeschlagen: " + e.getMessage(), e);
        }
    }

    @Override
    public void importAll(String filePath) {
        Path src = Path.of(filePath);
        if (!Files.exists(src)) throw new RuntimeException("Import-Datei nicht gefunden: " + filePath);
        try (Reader r = Files.newBufferedReader(src)) {
            DataSnapshot snap = gson.fromJson(r, DataSnapshot.class);
            if (snap == null) throw new RuntimeException("Import-Datei leer oder ungültig.");
            replaceAll(snap);
        } catch (IOException e) {
            throw new RuntimeException("Import fehlgeschlagen: " + e.getMessage(), e);
        }
    }
}
