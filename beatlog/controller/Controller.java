package ch.tbz.beatlog.controller;

import ch.tbz.beatlog.common.Ids;
import ch.tbz.beatlog.domain.ListeningSession;
import ch.tbz.beatlog.domain.Song;
import ch.tbz.beatlog.service.LibraryService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Controller {
    private final LibraryService service;

    public Controller(LibraryService service) {
        this.service = service;
    }

    // UC-01
    public String createSong(String title, String artist, String genre, int year, int durationSec,
                             String mood, int rating, Set<String> tags) {
        String id = Ids.newId();
        Song s = new Song(id, title, artist, genre, year, durationSec, mood, rating, tags);
        service.addSong(s);
        return id;
    }

    public void updateSong(String id, String title, String artist, String genre, int year, int durationSec,
                           String mood, int rating, Set<String> tags) {
        Song s = new Song(id, title, artist, genre, year, durationSec, mood, rating, tags);
        service.updateSong(s);
    }

    public void deleteSong(String id) { service.deleteSong(id); }
    public List<Song> getAllSongs() { return service.listSongs(); }
    public Optional<Song> getSong(String id) { return service.getSong(id); }

    // UC-02
    public void logListeningSession(String songId, String mood, String note, Integer ratingOverride) {
        ListeningSession s = new ListeningSession(
                Ids.newId(), songId, Instant.now(), mood, note, ratingOverride);
        service.logSession(s);
    }

    public List<ListeningSession> getAllSessions() { return service.listSessions(); }
}
