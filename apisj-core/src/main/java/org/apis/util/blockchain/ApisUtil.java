/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.apis.util.blockchain;

import java.math.BigInteger;

/**
 * Created by Anton Nashatyrev on 22.06.2016.
 * Changed by Daniel on 25.03.2018.
 */
public class ApisUtil {
    // Using SI unit prefix
    public enum Unit {
        /*atto */aAPIS(BigInteger.valueOf(1)),
        /*femto*/fAPIS(BigInteger.valueOf(1_000)),
        /*pico */pAPIS(BigInteger.valueOf(1_000_000)),
        /*nano */nAPIS(BigInteger.valueOf(1_000_000_000)),
        /*micro*/uAPIS(BigInteger.valueOf(1_000_000_000_000L)),
        /*milli*/mAPIS(BigInteger.valueOf(1_000_000_000_000_000L)),
        APIS(BigInteger.valueOf(1_000_000_000_000_000_000L));

        BigInteger i;
        Unit(BigInteger i) {
            this.i = i;
        }
    }

    public static BigInteger convert(long amount, Unit unit) {
        return BigInteger.valueOf(amount).multiply(unit.i);
    }

    public static String readableApis(BigInteger attoApis) {
        String attoString = attoApis.toString();

        if(attoString.length() > 18) {
            String left = attoString.substring(0, attoString.length() - 18);
            String right = attoString.substring(attoString.length() - 18, attoString.length());

            return left + "." + right;
        } else {
            for(;attoString.length() < 18;) {
                attoString = "0" + attoString;
            }

            return "0." + attoString;
        }
    }
}
