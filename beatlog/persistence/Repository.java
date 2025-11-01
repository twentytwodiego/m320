package ch.tbz.beatlog.persistence;

import ch.tbz.beatlog.domain.Song;

import java.util.List;
import java.util.Optional;

public interface Repository {
    void saveSong(Song song);              // create oder update per id
    Optional<Song> findSongById(String id);
    List<Song> loadAllSongs();
    void deleteSong(String id);
}
