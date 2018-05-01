package com.crashinvaders.common.statehash;

import com.badlogic.gdx.utils.Array;

public class StateHashUtils {

    public static int computeHash(Object... a) {
        if (a == null)
            return 0;

        int result = 1;

        for (int i = 0; i < a.length; i++) {
            Object element = a[i];

            result *= 31;

            if (element == null) continue;

            if (element instanceof Object[]) {
                result += computeHash(((Object[]) element));
            } else if (element instanceof Array) {
                result += computeHash(((Array) element).toArray());
            } else {
                result += element instanceof StateHashable ?
                        ((StateHashable) element).computeStateHash() :
                        element.hashCode();
            }
        }

        return result;
    }
}
