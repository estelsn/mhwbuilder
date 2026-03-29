package io.MHWilds.mhwbuilder.util;

import com.github.f4b6a3.ksuid.KsuidCreator;

public class KsuidUtil {

    public static String generate(String prefix) {
        return prefix + "_" + KsuidCreator.getKsuid().toString();
    }
}
