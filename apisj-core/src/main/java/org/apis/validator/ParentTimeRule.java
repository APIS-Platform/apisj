/*
 * Copyright (c) [2016] [ <ether.camp> ]
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

import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.core.BlockHeader;
import org.apis.util.TimeUtils;

/**
 * Checks if {@link BlockHeader#timestamp} >= {@link BlockHeader#timestamp} + 9 of parent's block
 * {@link BlockHeader#timestamp} can not precede the real time.
 *
 * @author Daniel
 * @since 2018-05-10
 */
public class ParentTimeRule extends DependentBlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();
        Constants constants = SystemProperties.getDefault().getBlockchainConfig().getConfigForBlock(header.getNumber()).getConstants();

        if (header.getTimestamp() - parent.getTimestamp() <  constants.getBLOCK_TIME()) {
            errors.add(String.format("#%d: block timestamp is less than parentBlock timestamp + 9\n%d < %d + 9", header.getNumber(), header.getTimestamp(), parent.getTimestamp()));
            return false;
        }

        else if(header.getTimestamp()*1000 > TimeUtils.getRealTimestamp()) {
            errors.add(String.format("#%d: block timestamp is bigger than realtime", header.getNumber()));
            return false;
        }

        return true;
    }
}
