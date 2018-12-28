package org.apis.util;

import org.apis.config.Constants;
import org.apis.util.blockchain.ApisUtil;
import org.apis.util.blockchain.MasternodeRewardData;

import java.math.BigInteger;

public class MasternodeRewardUtil {

    public static MasternodeRewardData calcRewards(Constants constants, BigInteger basicReward, int countGn, int countGl, int countMn, int countMl, int countPn, int countPl) {
        final int countGeneral = countGn + countGl;
        final int countMajor = countGn + countGl;
        final int countPrivate = countGn + countGl;

        BigInteger rewardGn  = BigInteger.ZERO;
        BigInteger rewardMn    = BigInteger.ZERO;
        BigInteger rewardPn  = BigInteger.ZERO;

        final BigInteger oneAPIS = ApisUtil.ONE_APIS();

        if(countGeneral > 0) {
            BigInteger weight = BigInteger.valueOf(countGeneral*100L).multiply(constants.getMASTERNODE_BALANCE_GENERAL()).divide(oneAPIS);
            rewardGn = basicReward.multiply(weight).divide(BigInteger.valueOf(countGeneral));
        }

        if(countMajor > 0) {
            BigInteger weight = BigInteger.valueOf(countMajor*105L).multiply(constants.getMASTERNODE_BALANCE_MAJOR()).divide(oneAPIS);
            rewardMn = basicReward.multiply(weight).divide(BigInteger.valueOf(countMajor));
        }

        if(countPrivate > 0) {
            BigInteger weight = BigInteger.valueOf(countPrivate*120L).multiply(constants.getMASTERNODE_BALANCE_PRIVATE()).divide(oneAPIS);
            rewardPn = basicReward.multiply(weight).divide(BigInteger.valueOf(countPrivate));
        }

        //ConsoleUtil.printlnPurple("Original General Reward : %s", ApisUtil.readableApis(rewardGn));
        //ConsoleUtil.printlnPurple("Original Major Reward : %s", ApisUtil.readableApis(rewardMn));
        //ConsoleUtil.printlnPurple("Original Private Reward : %s", ApisUtil.readableApis(rewardPn));


        /* Give rewards to late participating masternodes.
         *
         * 늦게 참여한 마스터노드는 보상의 70%만을 가져갈 수 있다.
         * 나머지 15%는 정상적으로 참여한 다른 마스터노드들에게 부여되고 나머지 15%는 재단에 부여된다.
         */
        BigInteger rewardGL = rewardGn.multiply(BigInteger.valueOf(70L)).divide(BigInteger.valueOf(100L));
        BigInteger rewardML = rewardMn.multiply(BigInteger.valueOf(70L)).divide(BigInteger.valueOf(100L));
        BigInteger rewardPL = rewardPn.multiply(BigInteger.valueOf(70L)).divide(BigInteger.valueOf(100L));

        BigInteger remainRewardByLateNode = BigInteger.ZERO;
        remainRewardByLateNode = remainRewardByLateNode.add(rewardGn.subtract(rewardGL).multiply(BigInteger.valueOf(countGl)));
        remainRewardByLateNode = remainRewardByLateNode.add(rewardMn.subtract(rewardML).multiply(BigInteger.valueOf(countMl)));
        remainRewardByLateNode = remainRewardByLateNode.add(rewardPn.subtract(rewardPL).multiply(BigInteger.valueOf(countPl)));


        /*
         * 늦게 참여한 마스터노드에게 분배하고 남은 보상을 다른 노드들에게 할당되는 부분과 재단에 할당되는 부분으로 나눠야 한다.
         * 다른 마스터노드에게는 1/2 만큼을 할당 (rewardToOtherNodes)
         * 재단에는 나머지 1/2 만큼을 할당 (rewardToFoundation)
         */
        BigInteger rewardToOtherNodes = remainRewardByLateNode.divide(BigInteger.valueOf(2));
        BigInteger rewardToFoundation = remainRewardByLateNode.subtract(rewardToOtherNodes);

        BigInteger remainGeneralWeight = BigInteger.valueOf(countGn * 100).multiply(constants.getMASTERNODE_BALANCE_GENERAL());
        BigInteger remainMajorWeight = BigInteger.valueOf(countMn * 105).multiply(constants.getMASTERNODE_BALANCE_MAJOR());
        BigInteger remainPrivateWeight = BigInteger.valueOf(countPn * 120).multiply(constants.getMASTERNODE_BALANCE_PRIVATE());
        BigInteger remainTotalWeight = remainGeneralWeight.add(remainMajorWeight).add(remainPrivateWeight);

        BigInteger debrisRewardGeneral = BigInteger.ZERO;
        BigInteger debrisRewardMajor = BigInteger.ZERO;
        BigInteger debrisRewardPrivate = BigInteger.ZERO;

        if(remainTotalWeight.compareTo(BigInteger.ZERO) > 0) {
            if(countGn > 0) {
                debrisRewardGeneral = rewardToOtherNodes.multiply(remainGeneralWeight).divide(remainTotalWeight).divide(BigInteger.valueOf(countGn));
            }
            if(countMn > 0) {
                debrisRewardMajor = rewardToOtherNodes.multiply(remainMajorWeight).divide(remainTotalWeight).divide(BigInteger.valueOf(countMn));
            }
            if(countPn > 0) {
                debrisRewardPrivate = rewardToOtherNodes.multiply(remainPrivateWeight).divide(remainTotalWeight).divide(BigInteger.valueOf(countPn));
            }
        }

        //ConsoleUtil.printlnPurple("Debris General Reward : %s", ApisUtil.readableApis(debrisRewardGeneral));
        //ConsoleUtil.printlnPurple("Debris Major Reward : %s", ApisUtil.readableApis(debrisRewardMajor));
        //ConsoleUtil.printlnPurple("Debris Private Reward : %s", ApisUtil.readableApis(debrisRewardPrivate));

        rewardGn = rewardGn.add(debrisRewardGeneral);
        rewardMn = rewardMn.add(debrisRewardMajor);
        rewardPn = rewardPn.add(debrisRewardPrivate);

        BigInteger totalReward = BigInteger.ZERO;
        totalReward = totalReward.add(rewardToFoundation);
        totalReward = totalReward.add(rewardGn.multiply(BigInteger.valueOf(countGn)));
        totalReward = totalReward.add(rewardGL.multiply(BigInteger.valueOf(countGl)));
        totalReward = totalReward.add(rewardMn.multiply(BigInteger.valueOf(countMn)));
        totalReward = totalReward.add(rewardML.multiply(BigInteger.valueOf(countMl)));
        totalReward = totalReward.add(rewardPn.multiply(BigInteger.valueOf(countPn)));
        totalReward = totalReward.add(rewardPL.multiply(BigInteger.valueOf(countPl)));

        return new MasternodeRewardData(rewardToFoundation, rewardGn, rewardGL, rewardMn, rewardML, rewardPn, rewardPL, totalReward);
    }


    public static BigInteger calcBasicReward(Constants constants, BigInteger storedMasternodeReward, long countGeneral, long countMajor, long countPrivate) {
        BigInteger oneAPIS = ApisUtil.ONE_APIS();
        BigInteger weightGeneral = BigInteger.valueOf(countGeneral*100L).multiply(constants.getMASTERNODE_BALANCE_GENERAL()).divide(oneAPIS);
        BigInteger weightMajor = BigInteger.valueOf(countMajor*105L).multiply(constants.getMASTERNODE_BALANCE_MAJOR()).divide(oneAPIS);
        BigInteger weightPrivate = BigInteger.valueOf(countPrivate*120L).multiply(constants.getMASTERNODE_BALANCE_PRIVATE()).divide(oneAPIS);

        BigInteger weightTotal = weightGeneral.add(weightMajor).add(weightPrivate);

        return storedMasternodeReward.divide(weightTotal);
    }
}
