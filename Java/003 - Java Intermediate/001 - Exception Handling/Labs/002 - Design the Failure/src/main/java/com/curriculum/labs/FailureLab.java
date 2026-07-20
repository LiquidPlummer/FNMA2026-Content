package com.curriculum.labs;

/**
 * Drives the inventory service through a batch of orders.
 * Currently: an integer-code ladder. By the end of the lab: domain
 * exceptions caught in domain terms.
 */
public class FailureLab {

    public static void main(String[] args) {
        InventoryService service = new InventoryService();

        String[][] orders = {
            {"WIDGET-1", "4"},      // fine
            {"GADGET-2", "1"},      // in catalog, zero stock
            {"NO-SUCH-SKU", "2"},   // unknown
            {"XCORRUPT-7", "1"},    // storage blows up on X* keys
            {"WIDGET-1", "99"},     // more than remains
        };

        for (String[] order : orders) {
            String sku = order[0];
            int qty = Integer.parseInt(order[1]);

            int code = service.reserve(sku, qty);
            if (code == 0) {
                System.out.println("RESERVED  " + qty + " x " + sku);
            } else if (code == -1) {
                System.out.println("FAILED    " + sku + ": unknown SKU");
            } else if (code == -2) {
                System.out.println("FAILED    " + sku + ": not enough stock");
            } else if (code == -3) {
                System.out.println("FAILED    " + sku + ": storage problem (?)");
            } else {
                System.out.println("FAILED    " + sku + ": unrecognized code " + code);
            }
        }
    }
}
