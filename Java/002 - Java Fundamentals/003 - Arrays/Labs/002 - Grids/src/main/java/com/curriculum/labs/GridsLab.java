package com.curriculum.labs;

import java.util.Arrays;

/**
 * Grids Lab — a small theater seating chart console app we build up piece
 * by piece.
 *
 * Follow the README's guided walkthrough. Each part tells you what to add
 * and points at one of the numbered markers below.
 */
public class GridsLab {

    // 5 rows, 8 seats per row. '.' means open, 'X' means booked.
    static final int ROWS = 5;
    static final int SEATS_PER_ROW = 8;

    public static void main(String[] args) {
        System.out.println("=== Theater Seating Chart ===");

        // [MARKER 1] Part 1: declare and create the 2D char grid here, filled with '.'.

        // [MARKER 2] Part 2: render the grid with row/seat labels here.

        // [MARKER 3] Part 3: book a seat by coordinates (bounds-checked) here,
        //            then render the grid again to see the change.

        // [MARKER 4] Part 4: count and print free seats per row here.

        System.out.println("Done.");
    }

    // [MARKER 5] Part 2: static void renderGrid(char[][] grid) goes here.

    // [MARKER 6] Part 3: static boolean bookSeat(char[][] grid, int row, int seat) goes here.

    // [MARKER 7] Part 4: static int freeSeatsInRow(char[][] grid, int row) goes here.
}
