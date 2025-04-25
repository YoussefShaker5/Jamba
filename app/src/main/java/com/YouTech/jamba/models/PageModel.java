package com.YouTech.jamba.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class PageModel implements Serializable {
    ArrayList<String> tracks;
    Map<String, ArrayList<String>> folders;
    Map<String, ArrayList<String>> albums;
    Map<String, ArrayList<String>> artists;

    public PageModel(ArrayList<String> tracks, Map<String, ArrayList<String>> folders, Map<String, ArrayList<String>> albums, Map<String, ArrayList<String>> artists) {
        this.tracks = tracks;
        this.folders = folders;
        this.albums = albums;
        this.artists = artists;
    }

    public ArrayList<String> getTracks() {
        return tracks;
    }

    public Map<String, ArrayList<String>> getFolders() {
        return folders;
    }

    public Map<String, ArrayList<String>> getAlbums() {
        return albums;
    }

    public Map<String, ArrayList<String>> getArtists() {
        return artists;
    }
}
