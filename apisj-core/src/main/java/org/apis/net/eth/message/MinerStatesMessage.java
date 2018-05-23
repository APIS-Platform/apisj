package org.apis.net.eth.message;

import org.apis.core.MinerState;
import org.apis.core.Transaction;
import org.apis.util.RLP;
import org.apis.util.RLPList;

import java.util.ArrayList;
import java.util.List;

/**
 * 네트워크에 POS 채굴자 정보를 전달하는 메시지를 구현한다
 *
 * @see EthMessageCodes#MINER_LIST
 */
public class MinerStatesMessage extends EthMessage {

    private List<MinerState> minerStates;

    public MinerStatesMessage(byte[] encoded) {
        super(encoded);
    }

    public MinerStatesMessage(MinerState minerState) {

        minerStates = new ArrayList<>();
        minerStates.add(minerState);
        parsed = true;
    }

    public MinerStatesMessage(List<MinerState> minerStates) {
        this.minerStates = minerStates;
        parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        minerStates = new ArrayList<>();
        for (int i = 0; i < paramsList.size(); ++i) {
            RLPList rlpTxData = (RLPList) paramsList.get(i);
            MinerState minerState = new MinerState(rlpTxData.getRLPData());
            minerStates.add(minerState);
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (MinerState minerState : minerStates)
            encodedElements.add(minerState.getEncoded());
        byte[][] encodedElementArray = encodedElements.toArray(new byte[encodedElements.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }


    public List<MinerState> getMinerStates() {
        parse();
        return minerStates;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.MINER_LIST;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        parse();
        final StringBuilder sb = new StringBuilder();
        if (minerStates.size() < 4) {
            for (MinerState minerState : minerStates)
                sb.append("\n   ").append(minerState.toString());
        } else {
            for (int i = 0; i < 3; i++) {
                sb.append("\n   ").append(minerStates.get(i).toString());
            }
            sb.append("\n   ").append("[Skipped ").append(minerStates.size() - 3).append(" miner states]");
        }
        return "[" + getCommand().name() + " num:"
                + minerStates.size() + " " + sb.toString() + "]";
    }
}