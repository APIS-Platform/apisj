package org.apis.util;

import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;

import java.nio.charset.Charset;

/**
 * Knowledge키와 관련된 유틸
 * 사용자가 입력한 암호로부터 knowledgeKey를 생성하거나, ProofCode를 반환한다.
 */
public class KnowledgeKeyUtil {

    public static ECKey getKnowledgeKey(String knowledgeSeed) {
        byte[] knowledgeCodeBytes = HashUtil.sha3(knowledgeSeed.getBytes(Charset.forName("UTF-8")));
        return ECKey.fromPrivate(knowledgeCodeBytes);
    }

    public static byte[] getProofCode(String knowledgeSeed) {
        return getKnowledgeKey(knowledgeSeed).getAddress();
    }
}
