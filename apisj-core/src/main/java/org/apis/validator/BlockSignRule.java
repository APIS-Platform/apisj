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
package org.apis.validator;

import org.apis.core.BlockHeader;
import org.apis.crypto.ECKey;
import org.apis.util.FastByteComparisons;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.SignatureException;

/**
 * Checks {@link BlockHeader#signature}
 */
public class BlockSignRule extends BlockHeaderRule {
    private static final int HASH_LENGTH = 32;
    private static final int ADDRESS_LENGTH = 20;

    @Override
    public ValidationResult validate(BlockHeader header) {
        if(header.isGenesis()) {
            return Success;
        }

        if (header.getSignature() == null)
            return fault("Signature is Null");

        ECKey.ECDSASignature signature = header.getSignature();

        if (BigIntegers.asUnsignedByteArray(signature.r).length > HASH_LENGTH)
            return fault("Signature R is not valid");
        if (BigIntegers.asUnsignedByteArray(signature.s).length > HASH_LENGTH)
            return fault("Signature S is not valid");
        if (header.getCoinbase() != null && header.getCoinbase().length != ADDRESS_LENGTH)
            return fault("Coinbase is not valid");

        try {
            byte[] coinbase = ECKey.signatureToAddress(header.getRawHash(), header.getSignature());
            if(!FastByteComparisons.equal(header.getCoinbase(), coinbase)) {
                return fault("The coinbase address is different from the signature");
            }
        } catch (SignatureException e) {
            return fault("Failed to change signature to address.");
        }

        return Success;
    }
}
