package com.crashinvaders.common.stringencriptor;

import com.badlogic.gdx.utils.Array;

public class CompositeStringEncryptor implements StringEncryptor {

    private final Array<StringEncryptor> encryptors = new Array<>();

    public CompositeStringEncryptor(StringEncryptor... encryptors) {
        this.encryptors.addAll(encryptors);
    }

    @Override
    public String encrypt(String value) {
        for (int i = 0; i < encryptors.size; i++) {
            value = encryptors.get(i).encrypt(value);
        }
        return value;
    }

    @Override
    public String decrypt(String value) {
        for (int i = encryptors.size - 1; i >= 0; i--) {
            value = encryptors.get(i).decrypt(value);
        }
        return value;
    }
}
