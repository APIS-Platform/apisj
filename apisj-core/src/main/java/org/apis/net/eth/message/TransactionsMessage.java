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

import org.apis.core.Transaction;
import org.apis.util.AddressUtil;
import org.apis.util.ByteUtil;
import org.apis.util.RLP;
import org.apis.util.RLPList;
import org.apis.util.blockchain.ApisUtil;
import org.spongycastle.util.BigIntegers;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Wrapper around an Ethereum Transactions message on the network
 *
 * @see EthMessageCodes#TRANSACTIONS
 */
public class TransactionsMessage extends EthMessage {

    private List<Transaction> transactions;

    public TransactionsMessage(byte[] encoded) {
        super(encoded);
    }

    public TransactionsMessage(Transaction transaction) {

        transactions = new ArrayList<>();
        transactions.add(transaction);
        parsed = true;
    }

    public TransactionsMessage(List<Transaction> transactionList) {
        this.transactions = transactionList;
        parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        transactions = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            RLPList rlpTxData = (RLPList) paramsList.get(i);
            Transaction tx = new Transaction(rlpTxData.getRLPData());
            transactions.add(tx);
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (Transaction tx : transactions)
            encodedElements.add(tx.getEncoded());
        byte[][] encodedElementArray = encodedElements.toArray(new byte[encodedElements.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }


    public List<Transaction> getTransactions() {
        parse();
        return transactions;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.TRANSACTIONS;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        parse();
        final StringBuilder sb = new StringBuilder();
        if (transactions.size() < 4) {
            for (Transaction transaction : transactions)
                sb.append("\n   ").append(shortTxDesc(transaction, 64));
        } else {
            for (int i = 0; i < 3; i++) {
                sb.append("\n   ").append(shortTxDesc(transactions.get(i), 128));
            }
            sb.append("\n   ").append("[Skipped ").append(transactions.size() - 3).append(" transactions]");
        }
        return "[" + getCommand().name() + " num:"
                + transactions.size() + " " + sb.toString() + "]";
    }

    private String shortTxDesc(Transaction tx, int maxDataSize) {
        String dataS;
        if (tx.getData() == null) {
            dataS = "";
        } else if (tx.getData().length < maxDataSize) {
            dataS = ByteUtil.toHexString(tx.getData());
        } else {
            dataS = ByteUtil.toHexString(Arrays.copyOfRange(tx.getData(), 0, maxDataSize)) +
                    "... (" + tx.getData().length + " bytes)";
        }
        return "TX [" + "hash=" + ByteUtil.toHexString(tx.getHash()) +
                "  nonce=" + ByteUtil.bytesToBigInteger(tx.getNonce()) +
                ", gasPrice=" + ByteUtil.bytesToBigInteger(tx.getGasPrice()) +
                ", gasLimit=" + ByteUtil.bytesToBigInteger(tx.getGasLimit()) +
                ", receiveAddress=" + AddressUtil.getShortAddress(tx.getReceiveAddress()) +
                ", receiveAddressMask=" + new String(tx.getReceiveMask(), Charset.forName("UTF-8")) +
                ", sendAddress=" + AddressUtil.getShortAddress(tx.getSender()) +
                ", value=" + ApisUtil.readableApis(ByteUtil.bytesToBigInteger(tx.getValue())) +
                ", data=" + dataS +
                "]";
    }
}