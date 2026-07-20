package com.curriculum.labs;

/**
 * Container Lab — we take Playlist from raw Objects to a fully generic,
 * iterable, type-safe container.
 */
public class ContainerLab {

    public static void main(String[] args) {
        Playlist mix = new Playlist();
        mix.add("Blue in Green");
        mix.add("So What");
        mix.add(42);                            // nothing stops this. yet.

        String first = (String) mix.get(0);     // every read needs a cast
        System.out.println(first.toUpperCase());

        String third = (String) mix.get(2);     // compiles fine...
        System.out.println(third.toUpperCase());

        // [MARKER 1] Part 3: try out the bounded max(...) method here.

        // [MARKER 2] Part 4: for-each over the playlist, once it's Iterable.

        // [MARKER 3] Part 5: the mid-loop removal experiment.
    }

    // [MARKER 4] Part 3: the generic max(...) method goes here.
}
