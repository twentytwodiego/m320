package ch.tbz.beatlog.service;

import ch.tbz.beatlog.domain.Song;
import java.util.*;
import java.util.stream.Collectors;

public class PlaylistService {

    // --- Filter (UC-04) ---
    public List<Song> filterSongs(List<Song> allSongs, String mood, Integer minRating, String tag) {
        return allSongs.stream()
                .filter(s -> mood == null || (s.getMood() != null && s.getMood().equalsIgnoreCase(mood)))
                .filter(s -> minRating == null || s.getRating() >= minRating)
                .filter(s -> tag == null || (s.getTags() != null && s.getTags().stream()
                        .anyMatch(t -> t.equalsIgnoreCase(tag))))
                .collect(Collectors.toList());
    }

    // --- Playlist Strategien (UC-03) ---
    public static class RandomStrategy implements SmartPlaylistStrategy {
        public List<Song> generate(List<Song> inputSongs) {
            List<Song> copy = new ArrayList<>(inputSongs);
            Collections.shuffle(copy);
            return copy;
        }
        public String getName() { return "Zufällig"; }
    }

    public static class HighRatingStrategy implements SmartPlaylistStrategy {
        public List<Song> generate(List<Song> inputSongs) {
            return inputSongs.stream()
                    .sorted(Comparator.comparingInt(Song::getRating).reversed())
                    .collect(Collectors.toList());
        }
        public String getName() { return "Nach Rating absteigend"; }
    }

    public static class RecentYearStrategy implements SmartPlaylistStrategy {
        public List<Song> generate(List<Song> inputSongs) {
            return inputSongs.stream()
                    .sorted(Comparator.comparingInt(Song::getYear).reversed())
                    .collect(Collectors.toList());
        }
        public String getName() { return "Neueste zuerst"; }
    }

    public List<SmartPlaylistStrategy> getAvailableStrategies() {
        return List.of(new RandomStrategy(), new HighRatingStrategy(), new RecentYearStrategy());
    }
}
