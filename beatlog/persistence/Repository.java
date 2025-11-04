package ch.tbz.beatlog.persistence;

import ch.tbz.beatlog.domain.Song;
import ch.tbz.beatlog.domain.ListeningSession;
import ch.tbz.beatlog.domain.DataSnapshot;

import java.util.List;
import java.util.Optional;

public interface Repository {
    // UC-01 Songs
    void saveSong(Song song);
    Optional<Song> findSongById(String id);
    List<Song> loadAllSongs();
    void deleteSong(String id);

    // UC-02 Sessions
    void saveSession(ListeningSession session);
    List<ListeningSession> loadAllSessions();

    // UC-05 Backup/Restore (alle Daten)
    DataSnapshot getSnapshot();
    void replaceAll(DataSnapshot snapshot);
    void exportAll(String filePath); // schreibt Snapshot in eine Datei
    void importAll(String filePath); // lädt Snapshot aus Datei (überschreibt alles)
}
