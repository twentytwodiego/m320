package ch.tbz.beatlog.persistence;

import ch.tbz.beatlog.domain.Song;
import ch.tbz.beatlog.domain.ListeningSession;
import ch.tbz.beatlog.domain.DataSnapshot;

import java.util.List;
import java.util.Optional;

public interface Repository {
    void saveSong(Song song);
    Optional<Song> findSongById(String id);
    List<Song> loadAllSongs();
    void deleteSong(String id);

    void saveSession(ListeningSession session);
    List<ListeningSession> loadAllSessions();


    DataSnapshot getSnapshot();
    void replaceAll(DataSnapshot snapshot);
    void exportAll(String filePath); 
    void importAll(String filePath);
}
