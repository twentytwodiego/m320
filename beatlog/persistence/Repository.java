package ch.tbz.beatlog.persistence;

import ch.tbz.beatlog.domain.Song;
import ch.tbz.beatlog.domain.ListeningSession;

import java.util.List;
import java.util.Optional;

public interface Repository {
    // UC-01
    void saveSong(Song song);
    Optional<Song> findSongById(String id);
    List<Song> loadAllSongs();
    void deleteSong(String id);

    // UC-02
    void saveSession(ListeningSession session);
    List<ListeningSession> loadAllSessions();
}
