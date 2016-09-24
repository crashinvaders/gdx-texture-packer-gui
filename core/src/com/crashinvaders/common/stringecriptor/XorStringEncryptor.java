package com.crashinvaders.common.stringecriptor;

import com.crashinvaders.texturepackergui.utils.CommonUtils;

public class XorStringEncryptor implements StringEncryptor {

    protected final String key;

    public XorStringEncryptor(String key) {
        this.key = key;
    }

    @Override
    public String encrypt(String value) {
        return CommonUtils.xor(key, value);
    }

    @Override
    public String decrypt(String value) {
        return CommonUtils.xor(key, value);
    }
}
