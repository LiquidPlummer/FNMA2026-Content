package com.curriculum.labs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Inventory Importer — reads records.txt and reports what's in stock.
 *
 * The data file is dirty on purpose. Follow the README: we harden this
 * importer step by step until it survives every bad row.
 */
public class ImporterLab {

    public static void main(String[] args) throws IOException {
        System.out.println("=== Inventory Importer ===");

        // [MARKER 2] Part 4 replaces this reading section with a
        //            BufferedReader inside try-with-resources.
        List<String> lines = Files.readAllLines(Path.of("records.txt"));

        int imported = 0;
        double total = 0.0;

        for (String line : lines) {
            // [MARKER 1] Part 2 wraps this parsing block in a try/catch.
            String[] fields = line.split(",");
            String name = fields[0];
            int quantity = Integer.parseInt(fields[1]);
            String priceText = fields[2];
            System.out.printf("  %-10s x%-3d @ %s%n", name, quantity, priceText);
            imported++;
            total += lineValue(quantity, priceText);
        }

        System.out.println("Imported rows: " + imported);
        System.out.printf("Total inventory value: %.2f%n", total);
    }

    static double lineValue(int quantity, String priceText) {
        try {
            return quantity * Double.parseDouble(priceText);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
