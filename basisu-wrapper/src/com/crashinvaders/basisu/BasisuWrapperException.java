package com.crashinvaders.basisu;

/**
 * An exception related to basisu-wrapper code both for native and Java side.
 */
public class BasisuWrapperException extends RuntimeException {
    private static final long serialVersionUID = -6258402319222323567L;

    public BasisuWrapperException(String message) {
        super(message);
    }
}
