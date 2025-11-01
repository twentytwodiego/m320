package ch.tbz.beatlog.persistence;

import ch.tbz.beatlog.domain.Song;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JsonRepository implements Repository {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataDir;
    private final Path songsFile;

    // einfacher Cache im Speicher
    private final Map<String, Song> songs = new LinkedHashMap<>();
    private boolean loaded = false;

    public JsonRepository(Properties config) {
        String dir = Objects.requireNonNullElse(config.getProperty("data.dir"), "data");
        String songsName = Objects.requireNonNullElse(config.getProperty("songs.file"), "songs.json");
        this.dataDir = Path.of(dir);
        this.songsFile = dataDir.resolve(songsName);
    }

    private void ensureLoaded() {
        if (loaded) return;
        try {
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            if (Files.exists(songsFile)) {
                try (Reader r = Files.newBufferedReader(songsFile)) {
                    Type listType = new TypeToken<List<Song>>(){}.getType();
                    List<Song> list = gson.fromJson(r, listType);
                    if (list != null) {
                        for (Song s : list) songs.put(s.getId(), s);
                    }
                }
            }
            loaded = true;
        } catch (IOException e) {
            throw new RuntimeException("Konnte Daten nicht laden: " + e.getMessage(), e);
        }
    }

    private void persistSongs() {
        try {
            List<Song> list = new ArrayList<>(songs.values());
            try (Writer w = Files.newBufferedWriter(songsFile)) {
                gson.toJson(list, w);
            }
        } catch (IOException e) {
            throw new RuntimeException("Konnte Songs nicht speichern: " + e.getMessage(), e);
        }
    }

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
}
