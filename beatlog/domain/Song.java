package ch.tbz.beatlog.domain;

import java.util.Objects;
import java.util.Set;

public class Song {
    private String id;          
    private String title;
    private String artist;
    private String genre;
    private int year;
    private int durationSec;    
    private String mood;        
    private int rating;         
    private Set<String> tags;   

    public Song() {} 

    public Song(String id, String title, String artist, String genre, int year,
                int durationSec, String mood, int rating, Set<String> tags) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.year = year;
        this.durationSec = durationSec;
        this.mood = mood;
        this.rating = rating;
        this.tags = tags;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    @Override
    public String toString() {
        return "%s — %s (%s, %d) [%ds, rating %d]"
                .formatted(artist, title, genre, year, durationSec, rating);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
