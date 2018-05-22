/*
 * Copyright (c) [2018] [ <APIS> ]
 * This file is part of the apisJ library.
 *
 * The apisJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The apisJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the apisJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.apis.validator;

import org.apis.core.BlockHeader;
import org.apis.core.Repository;

import java.math.BigInteger;

/**
 * Checks reward point value against block header
 *
 * @author Daniel
 */
public class ProofOfStakeRule extends BlockHeaderRule {

    @Override
    public ValidationResult validate(BlockHeader header) {

        long blockNumber = header.getNumber();

        BigInteger proof = header.getRewardPoint();
        //BigInteger calculated = header.calcRewardPointByBlockInfo();
        BigInteger calculated = BigInteger.ZERO;    //사용하지 않게 되어 변경..


        if (!header.isGenesis() && proof.compareTo(calculated) != 0) {
            return fault(String.format("#%d: proofValue > header.getRewardPoint()", header.getNumber()));
        }

        return Success;
    }
}