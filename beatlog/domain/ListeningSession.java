package ch.tbz.beatlog.domain;

import java.time.Instant;

public class ListeningSession {
    private String id;
    private String songId;
    private Instant timestamp;
    private String mood;
    private String note;
    private Integer ratingOverride; 

    public ListeningSession() {}

    public ListeningSession(String id, String songId, Instant timestamp,
                            String mood, String note, Integer ratingOverride) {
        this.id = id;
        this.songId = songId;
        this.timestamp = timestamp;
        this.mood = mood;
        this.note = note;
        this.ratingOverride = ratingOverride;
    }

    public String getId() { return id; }
    public String getSongId() { return songId; }
    public Instant getTimestamp() { return timestamp; }
    public String getMood() { return mood; }
    public String getNote() { return note; }
    public Integer getRatingOverride() { return ratingOverride; }

    @Override
    public String toString() {
        return "Session{songId='" + songId + "', mood='" + mood + "', note='" + note +
                "', ratingOverride=" + ratingOverride + ", timestamp=" + timestamp + "}";
    }
}
