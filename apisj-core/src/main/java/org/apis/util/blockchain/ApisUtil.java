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

import org.apache.commons.lang3.StringUtils;
import org.apis.util.BIUtil;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

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

    public static BigInteger ONE_APIS() {
        return convert(1, Unit.APIS);
    }

    /**
     * String 형태로 입력받은 숫자를 다른 Unit 의 String 형태로 반환한다.
     * 1Apis -> 1000mApis
     *
     * @param number 변환하려는 숫자 문자열
     * @param from 입력받은 숫자의 단위 (aApis, uApis 등)
     * @param to 반환하려는 숫자의 단위 (aApis, uApis 등)
     * @return
     */
    public static String convert(String number, Unit from, Unit to, char separator, boolean removeEndZeros){
        number = clearNumber(number);

        // aApis 단위로 변환
        String temp = ApisUtil.readableApis(number, ',', from, false).replaceAll(",","").replaceAll("\\.","");
        BigInteger pureNumber = new BigInteger(temp);
        pureNumber.multiply(BigInteger.valueOf(10).pow(getDecimalPoint(Unit.APIS)));
        return ApisUtil.readableApis(pureNumber, to, separator, removeEndZeros);
    }

    public static String readableApis(BigInteger attoApis) {
        return readableApis(attoApis, ',', false);
    }

    public static String readableApis(BigInteger attoApis, char separator, boolean removeEndZeros) {
        String attoString = attoApis.toString();

        if(attoString.length() > 18) {
            String left = attoString.substring(0, attoString.length() - 18);
            String right = attoString.substring(attoString.length() - 18, attoString.length());

            BigInteger leftNumber = new BigInteger(left);
            String pattern = "###,###";
            DecimalFormatSymbols symbol = new DecimalFormatSymbols(Locale.US);
            symbol.setDecimalSeparator('.');
            symbol.setGroupingSeparator(separator);
            NumberFormat formatter = new DecimalFormat(pattern, symbol);
            left = formatter.format(leftNumber);

            if(removeEndZeros) {
                right = StringUtils.stripEnd(right, "0");
            }

            if(right.isEmpty()) {
                return left;
            } else {
                return left + "." + right;
            }
        } else {
            for(;attoString.length() < 18;) {
                attoString = "0" + attoString;
            }

            if(removeEndZeros) {
                attoString = StringUtils.stripEnd(attoString, "0");

                if(attoString.isEmpty()) {
                    return "0";
                }
            }

            return "0." + attoString;

        }
    }

    public static String readableApis(BigInteger attoApis, Unit to,  char separator, boolean removeEndZeros) {
        String attoString = attoApis.toString();

        if(attoString.length() > getDecimalPoint(to)) {
            String left = attoString.substring(0, attoString.length() - getDecimalPoint(to));
            String right = attoString.substring(attoString.length() - getDecimalPoint(to), attoString.length());

            BigInteger leftNumber = new BigInteger(left);
            String pattern = "###,###";
            DecimalFormatSymbols symbol = new DecimalFormatSymbols(Locale.US);
            symbol.setDecimalSeparator('.');
            symbol.setGroupingSeparator(separator);
            NumberFormat formatter = new DecimalFormat(pattern, symbol);
            left = formatter.format(leftNumber);

            if(removeEndZeros) {
                right = StringUtils.stripEnd(right, "0");
            }

            if(right.isEmpty()) {
                return left;
            } else {
                return left + "." + right;
            }
        } else {
            for(;attoString.length() < getDecimalPoint(to);) {
                attoString = "0" + attoString;
            }

            if(removeEndZeros) {
                attoString = StringUtils.stripEnd(attoString, "0");

                if(attoString.isEmpty()) {
                    return "0";
                }
            }

            return "0." + attoString;

        }
    }




    public static String clearNumber(String number) {
        number = number.replaceAll("\\s+","");
        number = number.replaceAll("_", "");
        number = number.replaceAll(",", "");
        number = number.replaceAll("[^\\d.]", "");
        return number;
    }

    /**
     * String 형태로 입력받은 숫자를 읽기 쉬운 형태로 변환하여 반환한다. (반환하는 숫자의 단위 = Apis)
     *
     * @param number 변환하려는 숫자 문자열
     * @param separator 1000 단위마다 구분지을 문자 (기본값 ,)
     * @param unit 입력받은 숫자의 단위 (aApis, uApis 등)
     * @param removeEndZeros 소수점의 오른쪽에 나열된 0 문자들을 삭제할지 여부
     * @return 읽기 쉽게 변환된 숫자 문자열
     */
    public static String readableApis(String number, char separator, Unit unit, boolean removeEndZeros) {
        number = clearNumber(number);

        String[] splitNumber = number.split("\\.");
        BigInteger pureNumber = BigInteger.ZERO;
        int decimalPoint = 0;
        if(splitNumber.length == 1) {
            pureNumber = new BigInteger((splitNumber[0].length() == 0)? "0" : splitNumber[0]);
        } else if(splitNumber.length > 1) {
            decimalPoint = splitNumber[1].length();
            pureNumber = (new BigInteger((splitNumber[0].length() == 0)? "0" : splitNumber[0])).multiply(BigInteger.valueOf(10).pow(decimalPoint)).add(new BigInteger((splitNumber[1].length() == 0)? "0" : splitNumber[1]));
        }

        int decimalPointByUnit;
        switch(unit) {
            case fAPIS:
                decimalPointByUnit = 3;
                break;
            case pAPIS:
                decimalPointByUnit = 6;
                break;
            case nAPIS:
                decimalPointByUnit = 9;
                break;
            case uAPIS:
                decimalPointByUnit = 12;
                break;
            case mAPIS:
                decimalPointByUnit = 15;
                break;
            case APIS:
                decimalPointByUnit = 18;
                break;
            case aAPIS:
            default:
                decimalPointByUnit = 0;
                break;
        }

        int finalDecimalPoint = decimalPointByUnit - decimalPoint;

        if(finalDecimalPoint >= 0) {
            pureNumber = pureNumber.multiply(BigInteger.valueOf(10).pow(finalDecimalPoint));
        } else {
            pureNumber = pureNumber.divide(BigInteger.valueOf(10).pow(Math.abs(finalDecimalPoint)));
        }

        return readableApis(pureNumber, separator, removeEndZeros);
    }

    public static int getDecimalPoint(Unit unit){
        int decimalPointByUnit;
        switch(unit) {
            case fAPIS:
                decimalPointByUnit = 3;
                break;
            case pAPIS:
                decimalPointByUnit = 6;
                break;
            case nAPIS:
                decimalPointByUnit = 9;
                break;
            case uAPIS:
                decimalPointByUnit = 12;
                break;
            case mAPIS:
                decimalPointByUnit = 15;
                break;
            case APIS:
                decimalPointByUnit = 18;
                break;
            case aAPIS:
            default:
                decimalPointByUnit = 0;
                break;
        }
        return decimalPointByUnit;
    }
}
