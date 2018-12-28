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
package org.apis.samples;

import org.apis.core.Block;
import org.apis.core.TransactionReceipt;
import org.apis.facade.Apis;
import org.apis.facade.ApisFactory;
import org.apis.facade.Repository;
import org.apis.listener.EthereumListenerAdapter;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

public class FollowAccount extends EthereumListenerAdapter {


    Apis apis = null;

    public FollowAccount(Apis apis) {
        this.apis = apis;
    }

    public static void main(String[] args) {

        Apis apis = ApisFactory.createEthereum();
        apis.addListener(new FollowAccount(apis));
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {

        byte[] cow = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");

        // Get snapshot some time ago - 10% blocks ago
        long bestNumber = apis.getBlockchain().getBestBlock().getNumber();
        long oldNumber = (long) (bestNumber * 0.9);

        Block oldBlock = apis.getBlockchain().getBlockByNumber(oldNumber);

        Repository repository = apis.getRepository();
        Repository snapshot = apis.getSnapshotTo(oldBlock.getStateRoot());

        BigInteger nonce_ = snapshot.getNonce(cow);
        BigInteger nonce = repository.getNonce(cow);

        System.err.println(" #" + block.getNumber() + " [cd2a3d9] => snapshot_nonce:" +  nonce_ + " latest_nonce:" + nonce);
    }
}
