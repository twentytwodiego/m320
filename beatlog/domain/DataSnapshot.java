package ch.tbz.beatlog.domain;

import java.util.ArrayList;
import java.util.List;


public class DataSnapshot {
    private List<Song> songs = new ArrayList<>();
    private List<ListeningSession> sessions = new ArrayList<>();

    public DataSnapshot() {}

    public DataSnapshot(List<Song> songs, List<ListeningSession> sessions) {
        if (songs != null) this.songs = songs;
        if (sessions != null) this.sessions = sessions;
    }

    public List<Song> getSongs() { return songs; }
    public void setSongs(List<Song> songs) { this.songs = songs; }

    public List<ListeningSession> getSessions() { return sessions; }
    public void setSessions(List<ListeningSession> sessions) { this.sessions = sessions; }
}
