package com.example.musicmanagementapplication;

public class Music {
    public String title;
    public String artist;
    private String album;
    private int duration;
    private String genre;


    public Music() {
    }

    public Music(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public Music(String title, String artist, String album, int duration, String genre) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.genre = genre;
    }

    public String getTitle() {
        return this.title;
    }
    public String getArtist() {
        return this.artist;
    }

}