package com.YouTech.jamba.models;

import android.graphics.Bitmap;

public class AudioParams {
    public String ARTIST;
    public String ALBUM;
    public String TITLE;
    public Bitmap IMAGE;
    public Long DURATION;
    public AudioParams(String ARTIST, String ALBUM, String TITLE, Bitmap IMAGE, Long DURATION) {
        this.ARTIST = ARTIST;
        this.ALBUM = ALBUM;
        this.TITLE = TITLE;
        this.IMAGE = IMAGE;
        this.DURATION = DURATION;
    }
}
