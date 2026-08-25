package com.curriculum.labs;

/**
 * A low-level, checked failure from the (simulated) storage layer.
 * Pretend this comes from a library we don't own — we can't change it,
 * only translate it at our boundary.
 */
public class StorageFailure extends Exception {
    public StorageFailure(String message) {
        super(message);
    }
}
