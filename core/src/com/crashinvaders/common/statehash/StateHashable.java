package com.crashinvaders.common.statehash;

/** Interface for the the dedicated state hash logic.
 * Separated has method is required to not clash with original {@link Object#hashCode()}. */
public interface StateHashable {
    int computeStateHash();
}
