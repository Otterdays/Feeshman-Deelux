package com.yourname.feeshmandeelux;

/**
 * Result of an inventory delta after reeling one catch.
 */
public record CatchDelta(String itemId, boolean treasure, boolean junk, boolean enchanted) {

    public static CatchDelta unknown() {
        return new CatchDelta(null, false, false, false);
    }
}
