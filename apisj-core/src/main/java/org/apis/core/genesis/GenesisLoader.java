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
package org.apis.core.genesis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import org.apis.config.BlockchainNetConfig;
import org.apis.config.SystemProperties;
import org.apis.core.AccountState;
import org.apis.core.BlockHeader;
import org.apis.core.Genesis;
import org.apis.crypto.HashUtil;
import org.apis.db.ByteArrayWrapper;
import org.apis.trie.SecureTrie;
import org.apis.trie.Trie;
import org.apis.util.ByteUtil;
import org.apis.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.apis.util.ByteUtil.*;

public class GenesisLoader {

    /**
     * Load genesis from passed location or from classpath `genesis` directory
     */
    public static GenesisJson loadGenesisJson(SystemProperties config, ClassLoader classLoader) throws RuntimeException {
        final String genesisFile = config.getProperty("genesisFile", null);
        final String genesisResource = config.genesisInfo();

        // #1 try to find genesis at passed location
        if (genesisFile != null) {
            try (InputStream is = new FileInputStream(new File(genesisFile))) {
                return loadGenesisJson(is);
            } catch (Exception e) {
                showLoadError("Problem loading genesis file from " + genesisFile, genesisFile, genesisResource);
            }
        }

        // #2 fall back to old genesis location at `src/main/resources/genesis` directory
        InputStream is = classLoader.getResourceAsStream("genesis/" + genesisResource);
        if (is != null) {
            try {
                return loadGenesisJson(is);
            } catch (Exception e) {
                showLoadError("Problem loading genesis file from resource directory", genesisFile, genesisResource);
            }
        } else {
            showLoadError("Genesis file was not found in resource directory", genesisFile, genesisResource);
        }

        return null;
    }

    private static void showLoadError(String message, String genesisFile, String genesisResource) {
        Utils.showErrorAndExit(
            message,
            "Config option 'genesisFile': " + genesisFile,
            "Config option 'genesis': " + genesisResource);
    }

    public static Genesis parseGenesis(BlockchainNetConfig blockchainNetConfig, GenesisJson genesisJson) throws RuntimeException {
        try {
            Genesis genesis = createBlockForJson(genesisJson);

            genesis.setPremine(generatePreMine(blockchainNetConfig, genesisJson.getAlloc()));

            byte[] rootHash = generateRootHash(genesis.getPremine());
            genesis.setStateRoot(rootHash);

            return genesis;
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showErrorAndExit("Problem parsing genesis", e.getMessage());
        }
        return null;
    }

    /**
     * Method used much in tests.
     */
    public static Genesis loadGenesis(InputStream resourceAsStream) {
        GenesisJson genesisJson = loadGenesisJson(resourceAsStream);
        return parseGenesis(SystemProperties.getDefault().getBlockchainConfig(), genesisJson);
    }

    public static GenesisJson loadGenesisJson(InputStream genesisJsonIS) throws RuntimeException {
        String json = null;
        try {
            json = new String(ByteStreams.toByteArray(genesisJsonIS), Charset.forName("UTF-8"));

            ObjectMapper mapper = new ObjectMapper()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);

            return mapper.readValue(json, GenesisJson.class);
        } catch (Exception e) {

            Utils.showErrorAndExit("Problem parsing genesis: "+ e.getMessage(), json);

            throw new RuntimeException(e.getMessage(), e);
        }
    }


    private static Genesis createBlockForJson(GenesisJson genesisJson) {

        byte[] nonce       = prepareNonce(ByteUtil.hexStringToBytes(genesisJson.nonce));
        byte[] mixHash     = hexStringToBytesValidate(genesisJson.mixhash, 32, false);
        byte[] coinbase    = hexStringToBytesValidate(genesisJson.coinbase, 20, false);

        byte[] timestampBytes = hexStringToBytesValidate(genesisJson.timestamp, 8, true);
        long   timestamp         = ByteUtil.byteArrayToLong(timestampBytes);

        byte[] parentHash  = hexStringToBytesValidate(genesisJson.parentHash, 32, false);
        byte[] extraData   = hexStringToBytesValidate(genesisJson.extraData, 32, true);

        byte[] gasLimitBytes    = hexStringToBytesValidate(genesisJson.gasLimit, 8, true);
        long   gasLimit         = ByteUtil.byteArrayToLong(gasLimitBytes);



        return new Genesis(parentHash, coinbase, Genesis.ZERO_HASH_2048,
                            BigInteger.ZERO, BigInteger.ZERO, 0, gasLimit, 0, BigInteger.ZERO, timestamp, extraData,
                            mixHash, nonce);
    }

    private static byte[] hexStringToBytesValidate(String hex, int bytes, boolean notGreater) {
        byte[] ret = ByteUtil.hexStringToBytes(hex);
        if (notGreater) {
            if (ret.length > bytes) {
                throw new RuntimeException("Wrong value length: " + hex + ", expected length < " + bytes + " bytes");
            }
        } else {
            if (ret.length != bytes) {
                throw new RuntimeException("Wrong value length: " + hex + ", expected length " + bytes + " bytes");
            }
        }
        return ret;
    }

    /**
     * Prepares nonce to be correct length
     * 올바른 길이로 nonce 값을 준비한다
     * @param nonceUnchecked    unchecked, user-provided nonce, 체크되지 않은, 사용자가 제공한 nonce
     * @return  correct nonce, 올바른 nonce
     * @throws RuntimeException when nonce is too long, nonce가 너무 길 경우에 발생
     */
    private static byte[] prepareNonce(byte[] nonceUnchecked) {
        if (nonceUnchecked.length > 8) {
            throw new RuntimeException(String.format("Invalid nonce, should be %s length", BlockHeader.NONCE_LENGTH));
        } else if (nonceUnchecked.length == 8) {
            return nonceUnchecked;
        }
        byte[] nonce = new byte[BlockHeader.NONCE_LENGTH];
        int diff = BlockHeader.NONCE_LENGTH - nonceUnchecked.length;
        for (int i = diff; i < BlockHeader.NONCE_LENGTH; ++i) {
            nonce[i] = nonceUnchecked[i - diff];
        }
        return nonce;
    }


    /*
     * Genesis 파일에서 APIS를 할당할 지갑 목록을 불러와서 할당한다.
     */
    private static Map<ByteArrayWrapper, Genesis.PremineAccount> generatePreMine(BlockchainNetConfig blockchainNetConfig, Map<String, GenesisJson.AllocatedAccount> allocs){

        final Map<ByteArrayWrapper, Genesis.PremineAccount> preMine = new HashMap<>();

        for (String key : allocs.keySet()){

            final byte[] address = hexStringToBytes(key);
            final GenesisJson.AllocatedAccount alloc = allocs.get(key);
            final Genesis.PremineAccount state = new Genesis.PremineAccount();
            AccountState accountState = new AccountState(blockchainNetConfig.getCommonConstants().getInitialNonce(), parseHexOrDec(alloc.balance));

            if (alloc.nonce != null) {
                accountState = accountState.withNonce(parseHexOrDec(alloc.nonce));
            }

            if (alloc.code != null) {
                final byte[] codeBytes = hexStringToBytes(alloc.code);
                accountState = accountState.withCodeHash(HashUtil.sha3(codeBytes));
                state.code = codeBytes;
            }

            if(alloc.addressMask != null && !alloc.addressMask.isEmpty()) {
                accountState = accountState.withAddressMask(alloc.addressMask);
            }

            state.accountState = accountState;
            preMine.put(wrap(address), state);
        }

        return preMine;
    }

    /**
     * @param rawValue either hex started with 0x or dec
     * return BigInteger
     */
    private static BigInteger parseHexOrDec(String rawValue) {
        if (rawValue != null) {
            return rawValue.startsWith("0x") ? bytesToBigInteger(hexStringToBytes(rawValue)) : new BigInteger(rawValue);
        } else {
            return BigInteger.ZERO;
        }
    }

    public static byte[] generateRootHash(Map<ByteArrayWrapper, Genesis.PremineAccount> premine){

        Trie<byte[]> state = new SecureTrie((byte[]) null);

        for (ByteArrayWrapper key : premine.keySet()) {
            state.put(key.getData(), premine.get(key).accountState.getEncoded());
        }

        return state.getRootHash();
    }
}
