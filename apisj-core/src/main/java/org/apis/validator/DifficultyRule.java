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

import org.apis.config.SystemProperties;
import org.apis.core.BlockHeader;
import org.apis.util.BIUtil;

import java.math.BigInteger;

/**
 * Checks block's difficulty against calculated difficulty value
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class DifficultyRule extends DependentBlockHeaderRule {

    private final SystemProperties config;

    public DifficultyRule(SystemProperties config) {
        this.config = config;
    }

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();

        // POS 테스트를 위해 임의의 난이도로 조정한다
        //BigInteger calcDifficulty = header.calcDifficulty(config.getBlockchainConfig(), parent);
        BigInteger calcDifficulty = BigInteger.valueOf(1000000);
        BigInteger difficulty = header.getDifficultyBI();

        if (!BIUtil.isEqual(difficulty, calcDifficulty)) {

            errors.add(String.format("#%d: difficulty != calcDifficulty", header.getNumber()));
            return false;
        }

        return true;
    }
}
