package org.apis.net.eth.message;

import org.apis.core.MinerState;
import org.apis.core.Transaction;
import org.apis.util.RLP;
import org.apis.util.RLPElement;
import org.apis.util.RLPList;

import java.util.ArrayList;
import java.util.List;

/**
 * 네트워크에 POS 채굴자 정보를 전달하는 메시지를 구현한다
 *
 * 블럭이 채굴될 때, 무분별하게 블럭이 생성되어 네트워크에 전파됨으로써
 * 데이터가 낭비되는 것을 방지하기 위해
 * 네트워크 상에 어떤 채굴자가 존재하는지 사전에 파악할 필요가 있다.
 * 모든 채굴자는 이 채굴자 리스트를 이용해서 다음 블럭의 채굴자가 누가 될 것인지 사전에 예상할 수 있다.
 * 이를 통해 새롭게 생성되는 블럭의 개수를 적절하게 조절함으로써
 * 네트워크에서 데이터의 낭비를 줄이고 효율적으로 POS 방식의 채굴을 구현할 수 있다.
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
        for (RLPElement aParamsList : paramsList) {
            RLPList rlpTxData = (RLPList) aParamsList;
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