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
package org.apis.net.eth.message;

import org.apis.core.RewardPoint;
import org.apis.core.Transaction;
import org.apis.util.ByteUtil;
import org.apis.util.RLP;
import org.apis.util.RLPElement;
import org.apis.util.RLPList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around an APIS RewardPoint message on the network
 *
 * @see EthMessageCodes#REWARD_POINT
 */
public class RewardPointMessage extends EthMessage {

    private List<RewardPoint> rewardPoints;

    RewardPointMessage(byte[] encoded) {
        super(encoded);
    }

    public RewardPointMessage(RewardPoint rewardPoint) {
        rewardPoints = new ArrayList<>();
        rewardPoints.add(rewardPoint);
        parsed = true;
    }

    public RewardPointMessage(List<RewardPoint> rewardPointList) {
        this.rewardPoints = rewardPointList;
        parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        rewardPoints = new ArrayList<>();
        //TODO i++ 확인해야한다.
        for (RLPElement aParamsList : paramsList) {
            RLPList rlpRpData = (RLPList) aParamsList;
            RewardPoint rp = new RewardPoint(rlpRpData.getRLPData());
            rewardPoints.add(rp);
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (RewardPoint rp : rewardPoints)
            encodedElements.add(rp.getEncoded());

        byte[][] encodedElementArray = encodedElements.toArray(new byte[encodedElements.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }


    public List<RewardPoint> getRewardPoints() {
        parse();
        return rewardPoints;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.REWARD_POINT;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        parse();

        if(rewardPoints == null) {
            return "RewardPointMessage is Empty";
        } else {
            final StringBuilder sb = new StringBuilder();
            if (rewardPoints.size() < 4) {
                for (RewardPoint rewardPoint : rewardPoints)
                    sb.append("\n   ").append(rewardPoint.toString());
            } else {
                for (int i = 0; i < 3; i++) {
                    sb.append("\n   ").append(rewardPoints.get(i).toString());
                }
                sb.append("\n   ").append("[Skipped ").append(rewardPoints.size() - 3).append(" rewardPoints]");
            }
            return "[" + getCommand().name() + " num:" + rewardPoints.size() + " " + sb.toString() + "]";
        }
    }
}