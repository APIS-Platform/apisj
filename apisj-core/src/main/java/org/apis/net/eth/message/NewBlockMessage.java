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

import org.apis.core.Block;
import org.apis.util.RLP;
import org.apis.util.RLPList;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * Wrapper around an Ethereum Blocks message on the network
 *
 * @see EthMessageCodes#NEW_BLOCK
 */
public class NewBlockMessage extends EthMessage {

    private Block block;
    private byte[] rewardPoint;

    public NewBlockMessage(byte[] encoded) {
        super(encoded);
    }

    public NewBlockMessage(Block block, byte[] rewardPoint) {
        this.block = block;
        this.rewardPoint = rewardPoint;
        this.parsed = true;
        encode();
    }

    private void encode() {
        byte[] block = this.block.getEncoded();
        byte[] rewardPoint = RLP.encodeElement(this.rewardPoint);

        this.encoded = RLP.encodeList(block, rewardPoint);
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        RLPList blockRLP = ((RLPList) paramsList.get(0));
        block = new Block(blockRLP.getRLPData());
        rewardPoint = paramsList.get(1).getRLPData();

        parsed = true;
    }

    public Block getBlock() {
        parse();
        return block;
    }

    public byte[] getRewardPoint() {
        parse();
        return rewardPoint;
    }

    public BigInteger getRewardPointAsBigInt() {
        return new BigInteger(1, rewardPoint);
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.NEW_BLOCK;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        parse();

        String hash = this.getBlock().getShortHash();
        long number = this.getBlock().getNumber();
        return "NEW_BLOCK [ number: " + number + " hash:" + hash + " reward point: " + Hex.toHexString(rewardPoint) + " ]";
    }
}