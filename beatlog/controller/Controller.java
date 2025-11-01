package ch.tbz.beatlog.controller;

import ch.tbz.beatlog.common.Ids;
import ch.tbz.beatlog.domain.Song;
import ch.tbz.beatlog.service.LibraryService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Controller {
    private final LibraryService service;

    public Controller(LibraryService service) {
        this.service = service;
    }

    // CREATE
    public String createSong(String title, String artist, String genre, int year, int durationSec,
                             String mood, int rating, Set<String> tags) {
        String id = Ids.newId();
        Song s = new Song(id, title, artist, genre, year, durationSec, mood, rating, tags);
        service.addSong(s);
        return id;
    }

    // UPDATE
    public void updateSong(String id, String title, String artist, String genre, int year, int durationSec,
                           String mood, int rating, Set<String> tags) {
        Song s = new Song(id, title, artist, genre, year, durationSec, mood, rating, tags);
        service.updateSong(s);
    }

    // DELETE
    public void deleteSong(String id) {
        service.deleteSong(id);
    }

    public List<Song> getAllSongs() {
        return service.listSongs();
    }

    public Optional<Song> getSong(String id) {
        return service.getSong(id);
    }
}
