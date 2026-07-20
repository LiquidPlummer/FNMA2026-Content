package com.curriculum.labs;

import java.util.ArrayList;
import java.util.List;

/**
 * A playlist container — currently written like it's 2003: it holds
 * Objects, and every read needs a cast and a prayer. We modernize it
 * in the walkthrough.
 */
public class Playlist {
    private final List songs = new ArrayList();   // raw types, on purpose (for now)

    public void add(Object song) {
        songs.add(song);
    }

    public Object get(int index) {
        return songs.get(index);
    }

    public int size() {
        return songs.size();
    }
}
