package ch.tbz.beatlog.service;

import ch.tbz.beatlog.common.ValidationException;
import ch.tbz.beatlog.domain.Song;
import ch.tbz.beatlog.domain.ListeningSession;
import ch.tbz.beatlog.domain.DataSnapshot;
import ch.tbz.beatlog.persistence.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LibraryService {

    private final Repository repo;

    public LibraryService(Repository repo) {
        this.repo = repo;
    }

    public void addSong(Song s) {
        validateSong(s, true);
        repo.saveSong(s);
    }

    public void updateSong(Song s) {
        Objects.requireNonNull(s);
        if (s.getId() == null) throw new ValidationException("Song-ID fehlt für Update.");
        validateSong(s, false);
        Optional<Song> existing = repo.findSongById(s.getId());
        if (existing.isEmpty()) throw new ValidationException("Song nicht gefunden für Update.");
        repo.saveSong(s);
    }

    public void deleteSong(String id) {
        if (id == null || id.isBlank()) throw new ValidationException("Ungültige Song-ID.");
        repo.deleteSong(id);
    }

    public List<Song> listSongs() { return repo.loadAllSongs(); }
    public Optional<Song> getSong(String id) { return repo.findSongById(id); }

    private void validateSong(Song s, boolean isCreate) {
        if (s == null) throw new ValidationException("Song ist null.");
        if (!isCreate && (s.getId() == null || s.getId().isBlank()))
            throw new ValidationException("Song-ID fehlt.");
        if (s.getTitle() == null || s.getTitle().isBlank())
            throw new ValidationException("Titel darf nicht leer sein.");
        if (s.getArtist() == null || s.getArtist().isBlank())
            throw new ValidationException("Artist darf nicht leer sein.");
        if (s.getDurationSec() <= 0)
            throw new ValidationException("Dauer muss > 0 sein.");
        if (s.getRating() < 1 || s.getRating() > 10)
            throw new ValidationException("Rating muss zwischen 1 und 10 liegen.");
    }

    public void logSession(ListeningSession s) {
        if (s.getSongId() == null || s.getSongId().isBlank())
            throw new ValidationException("Song-ID fehlt für Session.");
        if (s.getTimestamp() == null) {
            s = new ListeningSession(
                    s.getId(),
                    s.getSongId(),
                    Instant.now(),
                    s.getMood(),
                    s.getNote(),
                    s.getRatingOverride());
        }
        repo.saveSession(s);
    }

    public List<ListeningSession> listSessions() {
        return repo.loadAllSessions();
    }

    public DataSnapshot snapshot() { return repo.getSnapshot(); }
    public void replaceAll(DataSnapshot snap) { repo.replaceAll(snap); }
    public void backupToFile(String path) { repo.exportAll(path); }
    public void restoreFromFile(String path) { repo.importAll(path); }
}
