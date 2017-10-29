package com.crashinvaders.common.stringencriptor;

import com.badlogic.gdx.utils.Base64Coder;

public class Base64StringEncryptor implements StringEncryptor {

    @Override
    public String encrypt(String value) {
        return Base64Coder.encodeString(value);
    }

    @Override
    public String decrypt(String value) {
        return Base64Coder.decodeString(value);
    }
}
