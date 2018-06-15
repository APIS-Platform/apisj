package org.apis.net.eth.message;

import org.apis.core.Block;
import org.apis.core.Transaction;
import org.apis.util.RLP;
import org.apis.util.RLPElement;
import org.apis.util.RLPList;

import java.util.ArrayList;
import java.util.List;

public class MinedBlockMessage extends EthMessage {

    private List<Block> blocks;

    public MinedBlockMessage(byte[] encoded) {
        super(encoded);
    }

    public MinedBlockMessage(List<Block> blockList) {
        this.blocks = blockList;
        parsed = true;
    }

    private synchronized void parse() {
        if (parsed) return;
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);

        blocks = new ArrayList<>();
        for (RLPElement aParamsList : paramsList) {
            RLPList rlpTxData = (RLPList) aParamsList;
            Block block = new Block(rlpTxData.getRLPData());
            blocks.add(block);
        }
        parsed = true;
    }

    private void encode() {
        List<byte[]> encodedElements = new ArrayList<>();
        for (Block block : blocks)
            encodedElements.add(block.getEncoded());
        byte[][] encodedElementArray = encodedElements.toArray(new byte[encodedElements.size()][]);
        this.encoded = RLP.encodeList(encodedElementArray);
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }


    public List<Block> getBlocks() {
        parse();
        return blocks;
    }

    @Override
    public EthMessageCodes getCommand() {
        return EthMessageCodes.MINED_BLOCK_LIST;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    public String toString() {
        parse();
        final StringBuilder sb = new StringBuilder();
        if (blocks.size() < 4) {
            for (Block block : blocks)
                sb.append("\n   ").append(block.getShortDescr());
        } else {
            for (int i = 0; i < blocks.size(); i++) {
                sb.append("\n   ").append(blocks.get(i).getShortDescr());
            }
        }
        return "[" + getCommand().name() + " num:"
                + blocks.size() + "\n" + sb.toString() + "]";
    }
}