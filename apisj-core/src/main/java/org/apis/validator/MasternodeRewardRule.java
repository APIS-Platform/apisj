package org.apis.validator;

import org.apis.config.Constants;
import org.apis.config.SystemProperties;
import org.apis.core.BlockHeader;

import java.math.BigInteger;

/**
 * 블록에서 마스터노드 보상을 분배하는데, 트랜잭션이 포함되지는 않았는지 체크한다.
 */
public class MasternodeRewardRule extends BlockHeaderRule {

    @Override
    public ValidationResult validate(BlockHeader header) {
        Constants constants = SystemProperties.getDefault().getBlockchainConfig().getConfigForBlock(header.getNumber()).getConstants();
        boolean isMasternodeRewardBlock = constants.isMasternodeRewardBlock(header.getNumber());

        if(isMasternodeRewardBlock && (header.getGasUsed() > 0 || header.getMineralUsed().compareTo(BigInteger.ZERO) > 0)) {
            return fault(String.format("A block[%d] that pays rewards to masternodes can not contain transactions.", header.getNumber()));
        }
        return Success;
    }
}
