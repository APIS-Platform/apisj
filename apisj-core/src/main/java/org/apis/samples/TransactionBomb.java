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
import org.apis.core.Transaction;
import org.apis.core.TransactionReceipt;
import org.apis.crypto.HashUtil;
import org.apis.facade.Apis;
import org.apis.facade.ApisFactory;
import org.apis.util.ByteUtil;
import org.apis.listener.EthereumListenerAdapter;
import org.spongycastle.util.encoders.Hex;

import java.util.Collections;
import java.util.List;

import static org.apis.crypto.HashUtil.sha3;

public class TransactionBomb extends EthereumListenerAdapter {


    Apis apis = null;
    boolean startedTxBomb = false;

    public TransactionBomb(Apis apis) {
        this.apis = apis;
    }

    public static void main(String[] args) {

        Apis apis = ApisFactory.createEthereum();
        apis.addListener(new TransactionBomb(apis));
    }


    @Override
    public void onSyncDone(SyncState state) {

        // We will send transactions only
        // after we have the full chain syncs
        // - in order to prevent old nonce usage
        startedTxBomb = true;
        System.err.println(" ~~~ SYNC DONE ~~~ ");
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {

        if (startedTxBomb){
            byte[] sender = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");
            long nonce = apis.getRepository().getNonce(sender).longValue();;

            for (int i=0; i < 20; ++i){
                sendTx(nonce);
                ++nonce;
                sleep(10);
            }
        }
    }

    private void sendTx(long nonce){

        byte[] gasPrice = ByteUtil.longToBytesNoLeadZeroes(1_000_000_000_000L);
        byte[] gasLimit = ByteUtil.longToBytesNoLeadZeroes(21000);

        byte[] toAddress = Hex.decode("9f598824ffa7068c1f2543f04efb58b6993db933");
        byte[] value = ByteUtil.longToBytesNoLeadZeroes(10_000);

        Transaction tx = new Transaction(ByteUtil.longToBytesNoLeadZeroes(nonce),
                gasPrice,
                gasLimit,
                toAddress,
                value,
                null,
                apis.getChainIdForNextBlock());

        byte[] privKey = HashUtil.sha3("cow".getBytes());
        tx.sign(privKey);

        apis.getChannelManager().sendTransaction(Collections.singletonList(tx), null);
        System.err.println("Sending tx: " + Hex.toHexString(tx.getHash()));
    }

    private void sleep(int millis){
        try {Thread.sleep(millis);}
        catch (InterruptedException e) {e.printStackTrace();}
    }
}
