package org.apis.util;

import java.math.BigInteger;
import java.util.Comparator;

public class AscendingBigInteger implements Comparator<BigInteger> {
    @Override
    public int compare(BigInteger a, BigInteger b) {
        return b.compareTo(a);
    }
}
