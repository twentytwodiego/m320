package ch.tbz.beatlog.service;

import ch.tbz.beatlog.domain.Song;
import java.util.List;

public interface SmartPlaylistStrategy {
    List<Song> generate(List<Song> inputSongs);
    String getName();
}
