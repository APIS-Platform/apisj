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
package org.apis;

import org.apis.cli.CLIInterface;
import org.apis.cli.CLIStart;
import org.apis.config.SystemProperties;
import org.apis.core.Block;
import org.apis.core.Repository;
import org.apis.core.Transaction;
import org.apis.core.TransactionReceipt;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.db.ByteArrayWrapper;
import org.apis.facade.Ethereum;
import org.apis.facade.EthereumFactory;
import org.apis.listener.EthereumListener;
import org.apis.listener.EthereumListenerAdapter;
import org.apis.rpc.RPCServer;
import org.apis.util.BIUtil;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;
import org.apis.util.blockchain.ApisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.*;


public class Start {

    private static Ethereum mEthereum;

    private static boolean synced = false;
    protected static Logger logger = LoggerFactory.getLogger("Start");

    static private Map<ByteArrayWrapper, TransactionReceipt> txWaiters =
            Collections.synchronizedMap(new HashMap<>());


    public static void main(String args[]) throws IOException, URISyntaxException {
        //CLIInterface.call(args);

        CLIStart cliStart = new CLIStart();

        cliStart.startKeystoreCheck();

        cliStart.startRpcServerCheck();



        final SystemProperties config = SystemProperties.getDefault();
        if(config == null) {
            System.out.println("Failed to load config");
            System.exit(0);
        }





        final boolean actionBlocksLoader = !config.blocksLoader().equals("");

        if (actionBlocksLoader) {
            config.setSyncEnabled(false);
            config.setDiscoveryEnabled(false);
        }

        mEthereum = EthereumFactory.createEthereum();
        mEthereum.addListener(mListener);
        mEthereum.getBlockMiner().setMinGasPrice(ApisUtil.convert(50, ApisUtil.Unit.nAPIS));

        if (actionBlocksLoader) {
            mEthereum.getBlockLoader().loadBlocks();
        }


        Properties prop = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };

        // start server
        try {
            InputStream input = new FileInputStream("config/rpc.properties");
            prop.load(input);

            int port =  Integer.parseInt(prop.getProperty("port"));
            String id = prop.getProperty("id");
            char[] pw = prop.getProperty("password").toCharArray();
            boolean use = Boolean.parseBoolean(prop.getProperty("use_rpc"));
            int allowMaxIP = Integer.parseInt(prop.getProperty("max_connections"));

            if (use) {
                RPCServer rpcServer = new RPCServer(port, id, pw, mEthereum);
                rpcServer.setMaxconnections(allowMaxIP);
                rpcServer.start();
            }
        } catch (IOException e) {
            System.exit(0);
        }
    }

    private static EthereumListener mListener = new EthereumListenerAdapter() {

        boolean isStartGenerateTx = false;

        @Override
        public void onSyncDone(SyncState state) {
            synced = true;
            System.out.println("SYND DONEDONEDONE");
        }

        long blockCount = 0;

        /**
         *  블록들을 전달받았으면 다른 노드들에게 현재의 RP를 전파해야한다.
         */
        @Override
        public void onBlock(Block block, List<TransactionReceipt> receipts) {
            if(!FastByteComparisons.equal(block.getCoinbase(), SystemProperties.getDefault().getMinerCoinbase())) {
                System.out.println("OnBlock : " + block.getNumber());
                System.out.println(block.getShortDescr());
            }

            /*if(BlockMiner.contractTxid != null) {
                TransactionInfo info = mEthereum.getTransactionInfo(BlockMiner.contractTxid);
                System.err.println(info.getReceipt().toString());
                byte[] contractAddress = info.getReceipt().getTransaction().getContractAddress();
                System.err.println(Hex.toHexString(contractAddress));

                String contractSol = ContractLoader.loadContractSource(ContractLoader.CONTRACT_ADDRESS_MASKING);
                try {
                    SolidityCompiler.Result result = SolidityCompiler.compile(contractSol.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);

                    if(result.isFailed()) {
                        logger.error("Contract compilation failed : \n" + result.errors);
                        return;
                    }

                    CompilationResult res = CompilationResult.parse(result.output);

                    CompilationResult.ContractMetadata metadata = res.getContract("AddressMasking");

                    if(metadata == null) {
                        return;
                    }

                    CallTransaction.Contract cont = new CallTransaction.Contract(metadata.abi);
                    ProgramResult r = mEthereum.callConstantFunction(Hex.toHexString(contractAddress), cont.getByName("owners"), 0);

                    Object[] ret = cont.getByName("owners").decodeResult(r.getHReturn());
                    System.err.println("Current contract data member value: " + Hex.toHexString((byte[]) ret[0]));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }*/



            /*Repository repo = ((Repository)mEthereum.getRepository()).getSnapshotTo(block.getStateRoot());
            System.out.println("MASK : " + repo.getMaskByAddress(Hex.decode("0000000000000000000000000000000000000003")));

            byte[] address = repo.getAddressByMask("테스트주소@me");
            System.out.println("ADDRESS : " + Hex.toHexString(address));*/

            SecureRandom rnd = new SecureRandom();

            if(synced) {
                //generateTransactions(rnd.nextInt(30));
            }
        }
    };

    protected static TransactionReceipt sendTxAndWait(byte[] receiveAddress, byte[] data) throws InterruptedException {
        ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));

        BigInteger nonce = mEthereum.getRepository().getNonce(senderKey.getAddress());
        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(mEthereum.getGasPrice()),
                ByteUtil.longToBytesNoLeadZeroes(30_000_000),
                receiveAddress,
                ByteUtil.longToBytesNoLeadZeroes(0),
                data,
                mEthereum.getChainIdForNextBlock());
        tx.sign(senderKey);
        logger.info("<=== Sending transaction: " + tx);
        mEthereum.submitTransaction(tx);

        return waitForTx(tx.getHash());
    }

    protected static TransactionReceipt waitForTx(byte[] txHash) throws InterruptedException {
        ByteArrayWrapper txHashW = new ByteArrayWrapper(txHash);
        txWaiters.put(txHashW, null);
        long startBlock = mEthereum.getBlockchain().getBestBlock().getNumber();
        while(true) {
            TransactionReceipt receipt = txWaiters.get(txHashW);
            if (receipt != null) {
                return receipt;
            } else {
                long curBlock = mEthereum.getBlockchain().getBestBlock().getNumber();
                if (curBlock > startBlock + 16) {
                    throw new RuntimeException("The transaction was not included during last 16 blocks: " + txHashW.toString().substring(0,8));
                } else {
                    logger.info("Waiting for block with transaction 0x" + txHashW.toString().substring(0,8) +
                            " included (" + (curBlock - startBlock) + " blocks received so far) ...");
                }
            }

            Thread.sleep(10_000);
        }
    }

    private static int lastNonce = 0;

    private static void generateTransactions(int num) {

        ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));

        if(lastNonce == 0) {
            lastNonce = mEthereum.getRepository().getNonce(senderKey.getAddress()).intValue();
        }

        //num = Math.max(100, num);
        for (int i = lastNonce, j = 0; j < num; i++, j++, lastNonce++) {
            {
                /*Repository repo = ((Repository)mEthereum.getRepository()).getSnapshotTo(mEthereum.getBlockchain().getBestBlock().getStateRoot());

                String mask = "테스트주소@me";
                byte[] address = repo.getAddressByMask(mask);


                byte[] nonce = ByteUtil.intToBytesNoLeadZeroes(i);
                byte[] data = ByteUtil.intToBytesNoLeadZeroes(i + 100);
                Transaction txs = new Transaction(nonce,
                        ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                        address, mask, BIUtil.toBI(i + 100).toByteArray(), nonce, mEthereum.getChainIdForNextBlock());
                txs.sign(senderKey);

                System.out.println("<== Submitting tx: " + txs);
                mEthereum.submitTransaction(txs);*/


                StringBuilder temp = new StringBuilder();
                Random rnd = new Random();
                for (int k = 0; k < 20; k++) {
                    int rIndex = rnd.nextInt(3);
                    switch (rIndex) {
                        case 0:
                            // a-z
                            temp.append((char) ((int) (rnd.nextInt(26)) + 97));
                            break;
                        case 1:
                            // A-Z
                            temp.append((char) ((int) (rnd.nextInt(26)) + 65));
                            break;
                        case 2:
                            // 0-9
                            temp.append((rnd.nextInt(10)));
                            break;
                    }
                }

                byte[] receiverAddr = ECKey.fromPrivate(HashUtil.sha3(temp.toString().getBytes())).getAddress();
                receiverAddr = Hex.decode("66a99a95246aa66237514f8aa03e2386351cf432");  //PARIS
                //receiverAddr = Hex.decode("026f7929da07156036b295bf256e12fac8f947d0");  //AMSTERDAM

                byte[] nonce = ByteUtil.intToBytesNoLeadZeroes(i);
                if (nonce.length == 0) {
                    nonce = new byte[]{0};
                }
                SecureRandom seed = new SecureRandom();
                byte[] value = seed.generateSeed(8);
                Transaction txs = new Transaction(nonce,
                        ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
                        receiverAddr, BIUtil.toBI(value).toByteArray(), new byte[0], mEthereum.getChainIdForNextBlock());
                txs.sign(senderKey);
                //logger.info("<== Submitting tx: " + txs);
                mEthereum.submitTransaction(txs);
            }
        }
    }

    static String apisToken = "pragma solidity ^0.4.18;\n" +
            "\n" +
            "/**\n" +
            " * @title ERC20Basic\n" +
            " * @dev Simpler version of ERC20 interface\n" +
            " * @dev see https://github.com/ethereum/EIPs/issues/179\n" +
            " */\n" +
            "contract ERC20Basic {\n" +
            "  uint256 public totalSupply;\n" +
            "  function balanceOf(address who) public view returns (uint256);\n" +
            "  function transfer(address to, uint256 value) public returns (bool);\n" +
            "  event Transfer(address indexed from, address indexed to, uint256 value);\n" +
            "}\n" +
            "\n" +
            "\n" +
            "/**\n" +
            " * @title SafeMath\n" +
            " * @dev Math operations with safety checks that throw on error\n" +
            " */\n" +
            "library SafeMath {\n" +
            "  function mul(uint256 a, uint256 b) internal pure returns (uint256) {\n" +
            "    if (a == 0) {\n" +
            "      return 0;\n" +
            "    }\n" +
            "    uint256 c = a * b;\n" +
            "    assert(c / a == b);\n" +
            "    return c;\n" +
            "  }\n" +
            "\n" +
            "  function div(uint256 a, uint256 b) internal pure returns (uint256) {\n" +
            "    // assert(b > 0); // Solidity automatically throws when dividing by 0\n" +
            "    uint256 c = a / b;\n" +
            "    // assert(a == b * c + a % b); // There is no case in which this doesn't hold\n" +
            "    return c;\n" +
            "  }\n" +
            "\n" +
            "  function sub(uint256 a, uint256 b) internal pure returns (uint256) {\n" +
            "    assert(b <= a);\n" +
            "    return a - b;\n" +
            "  }\n" +
            "\n" +
            "  function add(uint256 a, uint256 b) internal pure returns (uint256) {\n" +
            "    uint256 c = a + b;\n" +
            "    assert(c >= a);\n" +
            "    return c;\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "\n" +
            "/**\n" +
            " * @title Basic token\n" +
            " * @dev Basic version of StandardToken, with no allowances.\n" +
            " */\n" +
            "contract BasicToken is ERC20Basic {\n" +
            "  using SafeMath for uint256;\n" +
            "\n" +
            "  mapping(address => uint256) balances;\n" +
            "\n" +
            "  /**\n" +
            "  * @dev transfer token for a specified address\n" +
            "  * @param _to The address to transfer to.\n" +
            "  * @param _value The amount to be transferred.\n" +
            "  */\n" +
            "  function transfer(address _to, uint256 _value) public returns (bool) {\n" +
            "    require(_to != address(0));\n" +
            "    require(_value <= balances[msg.sender]);\n" +
            "\n" +
            "    // SafeMath.sub will throw if there is not enough balance.\n" +
            "    balances[msg.sender] = balances[msg.sender].sub(_value);\n" +
            "    balances[_to] = balances[_to].add(_value);\n" +
            "    Transfer(msg.sender, _to, _value);\n" +
            "    return true;\n" +
            "  }\n" +
            "\n" +
            "  /**\n" +
            "  * @dev Gets the balance of the specified address.\n" +
            "  * @param _owner The address to query the the balance of.\n" +
            "  * @return An uint256 representing the amount owned by the passed address.\n" +
            "  */\n" +
            "  function balanceOf(address _owner) public view returns (uint256 balance) {\n" +
            "    return balances[_owner];\n" +
            "  }\n" +
            "\n" +
            "}\n" +
            "\n" +
            "\n" +
            "/**\n" +
            " * @title ERC20 interface\n" +
            " * @dev see https://github.com/ethereum/EIPs/issues/20\n" +
            " */\n" +
            "contract ERC20 is ERC20Basic {\n" +
            "  function allowance(address owner, address spender) public view returns (uint256);\n" +
            "  function transferFrom(address from, address to, uint256 value) public returns (bool);\n" +
            "  function approve(address spender, uint256 value) public returns (bool);\n" +
            "  event Approval(address indexed owner, address indexed spender, uint256 value);\n" +
            "}\n" +
            "\n" +
            "\n" +
            "/**\n" +
            " * @title Standard ERC20 token\n" +
            " *\n" +
            " * @dev Implementation of the basic standard token.\n" +
            " * @dev https://github.com/ethereum/EIPs/issues/20\n" +
            " * @dev Based on code by FirstBlood: https://github.com/Firstbloodio/token/blob/master/smart_contract/FirstBloodToken.sol\n" +
            " */\n" +
            "contract StandardToken is ERC20, BasicToken {\n" +
            "\n" +
            "  mapping (address => mapping (address => uint256)) internal allowed;\n" +
            "\n" +
            "\n" +
            "  /**\n" +
            "   * @dev Transfer tokens from one address to another\n" +
            "   * @param _from address The address which you want to send tokens from\n" +
            "   * @param _to address The address which you want to transfer to\n" +
            "   * @param _value uint256 the amount of tokens to be transferred\n" +
            "   */\n" +
            "  function transferFrom(address _from, address _to, uint256 _value) public returns (bool) {\n" +
            "    require(_to != address(0));\n" +
            "    require(_value <= balances[_from]);\n" +
            "    require(_value <= allowed[_from][msg.sender]);\n" +
            "\n" +
            "    balances[_from] = balances[_from].sub(_value);\n" +
            "    balances[_to] = balances[_to].add(_value);\n" +
            "    allowed[_from][msg.sender] = allowed[_from][msg.sender].sub(_value);\n" +
            "    Transfer(_from, _to, _value);\n" +
            "    return true;\n" +
            "  }\n" +
            "\n" +
            "  /**\n" +
            "   * @dev Approve the passed address to spend the specified amount of tokens on behalf of msg.sender.\n" +
            "   *\n" +
            "   * Beware that changing an allowance with this method brings the risk that someone may use both the old\n" +
            "   * and the new allowance by unfortunate transaction ordering. One possible solution to mitigate this\n" +
            "   * race condition is to first reduce the spender's allowance to 0 and set the desired value afterwards:\n" +
            "   * https://github.com/ethereum/EIPs/issues/20#issuecomment-263524729\n" +
            "   * @param _spender The address which will spend the funds.\n" +
            "   * @param _value The amount of tokens to be spent.\n" +
            "   */\n" +
            "  function approve(address _spender, uint256 _value) public returns (bool) {\n" +
            "    allowed[msg.sender][_spender] = _value;\n" +
            "    Approval(msg.sender, _spender, _value);\n" +
            "    return true;\n" +
            "  }\n" +
            "\n" +
            "  /**\n" +
            "   * @dev Function to check the amount of tokens that an owner allowed to a spender.\n" +
            "   * @param _owner address The address which owns the funds.\n" +
            "   * @param _spender address The address which will spend the funds.\n" +
            "   * @return A uint256 specifying the amount of tokens still available for the spender.\n" +
            "   */\n" +
            "  function allowance(address _owner, address _spender) public view returns (uint256) {\n" +
            "    return allowed[_owner][_spender];\n" +
            "  }\n" +
            "\n" +
            "  /**\n" +
            "   * @dev Increase the amount of tokens that an owner allowed to a spender.\n" +
            "   *\n" +
            "   * approve should be called when allowed[_spender] == 0. To increment\n" +
            "   * allowed value is better to use this function to avoid 2 calls (and wait until\n" +
            "   * the first transaction is mined)\n" +
            "   * From MonolithDAO Token.sol\n" +
            "   * @param _spender The address which will spend the funds.\n" +
            "   * @param _addedValue The amount of tokens to increase the allowance by.\n" +
            "   */\n" +
            "  function increaseApproval(address _spender, uint _addedValue) public returns (bool) {\n" +
            "    allowed[msg.sender][_spender] = allowed[msg.sender][_spender].add(_addedValue);\n" +
            "    Approval(msg.sender, _spender, allowed[msg.sender][_spender]);\n" +
            "    return true;\n" +
            "  }\n" +
            "\n" +
            "  /**\n" +
            "   * @dev Decrease the amount of tokens that an owner allowed to a spender.\n" +
            "   *\n" +
            "   * approve should be called when allowed[_spender] == 0. To decrement\n" +
            "   * allowed value is better to use this function to avoid 2 calls (and wait until\n" +
            "   * the first transaction is mined)\n" +
            "   * From MonolithDAO Token.sol\n" +
            "   * @param _spender The address which will spend the funds.\n" +
            "   * @param _subtractedValue The amount of tokens to decrease the allowance by.\n" +
            "   */\n" +
            "  function decreaseApproval(address _spender, uint _subtractedValue) public returns (bool) {\n" +
            "    uint oldValue = allowed[msg.sender][_spender];\n" +
            "    if (_subtractedValue > oldValue) {\n" +
            "      allowed[msg.sender][_spender] = 0;\n" +
            "    } else {\n" +
            "      allowed[msg.sender][_spender] = oldValue.sub(_subtractedValue);\n" +
            "    }\n" +
            "    Approval(msg.sender, _spender, allowed[msg.sender][_spender]);\n" +
            "    return true;\n" +
            "  }\n" +
            "\n" +
            "}\n" +
            "\n" +
            "\n" +
            "/**\n" +
            " * @title Ownable\n" +
            " * @dev The Ownable contract has an owner address, and provides basic authorization control\n" +
            " * functions, this simplifies the implementation of \"user permissions\".\n" +
            " */\n" +
            "contract Ownable {\n" +
            "    address public owner;\n" +
            "    address public newOwner;\n" +
            "\n" +
            "\n" +
            "    event OwnershipTransferred(address indexed previousOwner, address indexed newOwner);\n" +
            "\n" +
            "\n" +
            "    /**\n" +
            "     * @dev The Ownable constructor sets the original `owner` of the contract to the sender\n" +
            "     * account.\n" +
            "     */\n" +
            "    function Ownable() public {\n" +
            "        owner = msg.sender;\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "    /**\n" +
            "     * @dev Throws if called by any account other than the owner.\n" +
            "     */\n" +
            "    modifier onlyOwner() {\n" +
            "        require(msg.sender == owner);\n" +
            "        _;\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "    /**\n" +
            "     * @dev Allows the current owner to transfer control of the contract to a newOwner.\n" +
            "     * @param _newOwner The address to transfer ownership to.\n" +
            "     */\n" +
            "    function transferOwnership(address _newOwner) public onlyOwner {\n" +
            "        require(_newOwner != address(0));\n" +
            "        OwnershipTransferred(owner, _newOwner);\n" +
            "        newOwner = _newOwner;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * @dev 새로운 관리자가 승인해야만 소유권이 이전된다\n" +
            "     */\n" +
            "    function acceptOwnership() public {\n" +
            "        require(msg.sender == newOwner);\n" +
            "        \n" +
            "        OwnershipTransferred(owner, newOwner);\n" +
            "        owner = newOwner;\n" +
            "        newOwner = address(0);\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "\n" +
            "/**\n" +
            " * @title APIS Token\n" +
            " * @dev APIS 토큰을 생성한다\n" +
            " */\n" +
            "contract ApisToken is StandardToken, Ownable {\n" +
            "    // 토큰의 이름 (Advanced Property Investment System)\n" +
            "    string public constant name = \"Advanced Property Investment System\";\n" +
            "    \n" +
            "    // 토큰의 단위 (APIS)\n" +
            "    string public constant symbol = \"APIS\";\n" +
            "    \n" +
            "    // 소수점 자리수. ETH 18자리에 맞춘다\n" +
            "    uint8 public constant decimals = 18;\n" +
            "    \n" +
            "    // 지갑별로 송금/수금 기능의 잠긴 여부를 저장\n" +
            "    mapping (address => LockedInfo) public lockedWalletInfo;\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 플랫폼에서 운영하는 마스터노드 스마트 컨트렉트 주소\n" +
            "     */\n" +
            "    mapping (address => bool) public manoContracts;\n" +
            "    \n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 토큰 지갑의 잠김 속성을 정의\n" +
            "     * \n" +
            "     * @param timeLockUpEnd timeLockUpEnd 시간까지 송/수금에 대한 제한이 적용된다. 이후에는 제한이 풀린다\n" +
            "     * @param sendLock 출금 잠김 여부(true : 잠김, false : 풀림)\n" +
            "     * @param receiveLock 입금 잠김 여부 (true : 잠김, false : 풀림)\n" +
            "     */\n" +
            "    struct LockedInfo {\n" +
            "        uint timeLockUpEnd;\n" +
            "        bool sendLock;\n" +
            "        bool receiveLock;\n" +
            "    } \n" +
            "    \n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 토큰이 송금됐을 때 발생하는 이벤트\n" +
            "     * @param from 토큰을 보내는 지갑 주소\n" +
            "     * @param to 토큰을 받는 지갑 주소\n" +
            "     * @param value 전달되는 토큰의 양 (Satoshi)\n" +
            "     */\n" +
            "    event Transfer (address indexed from, address indexed to, uint256 value);\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 토큰 지갑의 송금/입금 기능이 제한되었을 때 발생하는 이벤트\n" +
            "     * @param target 제한 대상 지갑 주소\n" +
            "     * @param timeLockUpEnd 제한이 종료되는 시간(UnixTimestamp)\n" +
            "     * @param sendLock 지갑에서의 송금을 제한하는지 여부(true : 제한, false : 해제)\n" +
            "     * @param receiveLock 지갑으로의 입금을 제한하는지 여부 (true : 제한, false : 해제)\n" +
            "     */\n" +
            "    event Locked (address indexed target, uint timeLockUpEnd, bool sendLock, bool receiveLock);\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 지갑에 대한 송금/입금 제한을 해제했을 때 발생하는 이벤트\n" +
            "     * @param target 해제 대상 지갑 주소\n" +
            "     */\n" +
            "    event Unlocked (address indexed target);\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 송금 받는 지갑의 입금이 제한되어있어서 송금이 거절되었을 때 발생하는 이벤트\n" +
            "     * @param from 토큰을 보내는 지갑 주소\n" +
            "     * @param to (입금이 제한된) 토큰을 받는 지갑 주소\n" +
            "     * @param value 전송하려고 한 토큰의 양(Satoshi)\n" +
            "     */\n" +
            "    event RejectedPaymentToLockedUpWallet (address indexed from, address indexed to, uint256 value);\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 송금하는 지갑의 출금이 제한되어있어서 송금이 거절되었을 때 발생하는 이벤트\n" +
            "     * @param from (출금이 제한된) 토큰을 보내는 지갑 주소\n" +
            "     * @param to 토큰을 받는 지갑 주소\n" +
            "     * @param value 전송하려고 한 토큰의 양(Satoshi)\n" +
            "     */\n" +
            "    event RejectedPaymentFromLockedUpWallet (address indexed from, address indexed to, uint256 value);\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 토큰을 소각한다. \n" +
            "     * @param burner 토큰을 소각하는 지갑 주소\n" +
            "     * @param value 소각하는 토큰의 양(Satoshi)\n" +
            "     */\n" +
            "    event Burn (address indexed burner, uint256 value);\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 아피스 플랫폼에 마스터노드 스마트 컨트렉트가 등록되거나 해제될 때 발생하는 이벤트\n" +
            "     */\n" +
            "    event ManoContractRegistered (address manoContract, bool registered);\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 컨트랙트가 생성될 때 실행. 컨트렉트 소유자 지갑에 모든 토큰을 할당한다.\n" +
            "     * 발행량이나 이름은 소스코드에서 확인할 수 있도록 변경하였음\n" +
            "     */\n" +
            "    function ApisToken() public {\n" +
            "        // 총 APIS 발행량 (95억 2천만)\n" +
            "        uint256 supplyApis = 9520000000;\n" +
            "        \n" +
            "        // wei 단위로 토큰 총량을 생성한다.\n" +
            "        totalSupply = supplyApis * 10 ** uint256(decimals);\n" +
            "        \n" +
            "        balances[msg.sender] = totalSupply;\n" +
            "        \n" +
            "        Transfer(0x0, msg.sender, totalSupply);\n" +
            "    }\n" +
            "    \n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 지갑을 지정된 시간까지 제한시키거나 해제시킨다. 제한 시간이 경과하면 모든 제한이 해제된다.\n" +
            "     * @param _targetWallet 제한을 적용할 지갑 주소\n" +
            "     * @param _timeLockEnd 제한이 종료되는 시간(UnixTimestamp)\n" +
            "     * @param _sendLock (true : 지갑에서 토큰을 출금하는 기능을 제한한다.) (false : 제한을 해제한다)\n" +
            "     * @param _receiveLock (true : 지갑으로 토큰을 입금받는 기능을 제한한다.) (false : 제한을 해제한다)\n" +
            "     */\n" +
            "    function walletLock(address _targetWallet, uint _timeLockEnd, bool _sendLock, bool _receiveLock) onlyOwner public {\n" +
            "        require(_targetWallet != 0x0);\n" +
            "        \n" +
            "        // If all locks are unlocked, set the _timeLockEnd to zero.\n" +
            "        if(_sendLock == false && _receiveLock == false) {\n" +
            "            _timeLockEnd = 0;\n" +
            "        }\n" +
            "        \n" +
            "        lockedWalletInfo[_targetWallet].timeLockUpEnd = _timeLockEnd;\n" +
            "        lockedWalletInfo[_targetWallet].sendLock = _sendLock;\n" +
            "        lockedWalletInfo[_targetWallet].receiveLock = _receiveLock;\n" +
            "        \n" +
            "        if(_timeLockEnd > 0) {\n" +
            "            Locked(_targetWallet, _timeLockEnd, _sendLock, _receiveLock);\n" +
            "        } else {\n" +
            "            Unlocked(_targetWallet);\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 지갑의 입급/출금을 지정된 시간까지 제한시킨다. 제한 시간이 경과하면 모든 제한이 해제된다.\n" +
            "     * @param _targetWallet 제한을 적용할 지갑 주소\n" +
            "     * @param _timeLockUpEnd 제한이 종료되는 시간(UnixTimestamp)\n" +
            "     */\n" +
            "    function walletLockBoth(address _targetWallet, uint _timeLockUpEnd) onlyOwner public {\n" +
            "        walletLock(_targetWallet, _timeLockUpEnd, true, true);\n" +
            "    }\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 지갑의 입급/출금을 영원히(33658-9-27 01:46:39+00) 제한시킨다.\n" +
            "     * @param _targetWallet 제한을 적용할 지갑 주소\n" +
            "     */\n" +
            "    function walletLockBothForever(address _targetWallet) onlyOwner public {\n" +
            "        walletLock(_targetWallet, 999999999999, true, true);\n" +
            "    }\n" +
            "    \n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 지갑에 설정된 입출금 제한을 해제한다\n" +
            "     * @param _targetWallet 제한을 해제하고자 하는 지갑 주소\n" +
            "     */\n" +
            "    function walletUnlock(address _targetWallet) onlyOwner public {\n" +
            "        walletLock(_targetWallet, 0, false, false);\n" +
            "    }\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 지갑의 송금 기능이 제한되어있는지 확인한다.\n" +
            "     * @param _addr 송금 제한 여부를 확인하려는 지갑의 주소\n" +
            "     * @return isSendLocked (true : 제한되어 있음, 토큰을 보낼 수 없음) (false : 제한 없음, 토큰을 보낼 수 있음)\n" +
            "     * @return until 잠겨있는 시간, UnixTimestamp\n" +
            "     */\n" +
            "    function isWalletLocked_Send(address _addr) public constant returns (bool isSendLocked, uint until) {\n" +
            "        require(_addr != 0x0);\n" +
            "        \n" +
            "        isSendLocked = (lockedWalletInfo[_addr].timeLockUpEnd > now && lockedWalletInfo[_addr].sendLock == true);\n" +
            "        \n" +
            "        if(isSendLocked) {\n" +
            "            until = lockedWalletInfo[_addr].timeLockUpEnd;\n" +
            "        } else {\n" +
            "            until = 0;\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 지갑의 입금 기능이 제한되어있는지 확인한다.\n" +
            "     * @param _addr 입금 제한 여부를 확인하려는 지갑의 주소\n" +
            "     * @return (true : 제한되어 있음, 토큰을 받을 수 없음) (false : 제한 없음, 토큰을 받을 수 있음)\n" +
            "     */\n" +
            "    function isWalletLocked_Receive(address _addr) public constant returns (bool isReceiveLocked, uint until) {\n" +
            "        require(_addr != 0x0);\n" +
            "        \n" +
            "        isReceiveLocked = (lockedWalletInfo[_addr].timeLockUpEnd > now && lockedWalletInfo[_addr].receiveLock == true);\n" +
            "        \n" +
            "        if(isReceiveLocked) {\n" +
            "            until = lockedWalletInfo[_addr].timeLockUpEnd;\n" +
            "        } else {\n" +
            "            until = 0;\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 요청자의 지갑에 송금 기능이 제한되어있는지 확인한다.\n" +
            "     * @return (true : 제한되어 있음, 토큰을 보낼 수 없음) (false : 제한 없음, 토큰을 보낼 수 있음)\n" +
            "     */\n" +
            "    function isMyWalletLocked_Send() public constant returns (bool isSendLocked, uint until) {\n" +
            "        return isWalletLocked_Send(msg.sender);\n" +
            "    }\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 요청자의 지갑에 입금 기능이 제한되어있는지 확인한다.\n" +
            "     * @return (true : 제한되어 있음, 토큰을 보낼 수 없음) (false : 제한 없음, 토큰을 보낼 수 있음)\n" +
            "     */\n" +
            "    function isMyWalletLocked_Receive() public constant returns (bool isReceiveLocked, uint until) {\n" +
            "        return isWalletLocked_Receive(msg.sender);\n" +
            "    }\n" +
            "    \n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 아피스 플랫폼에서 운영하는 스마트 컨트렉트 주소를 등록하거나 해제한다.\n" +
            "     * @param manoAddr 마스터노드 스마트 컨트렉컨트렉트\n" +
            "     * @param registered true : 등록, false : 해제\n" +
            "     */\n" +
            "    function registerManoContract(address manoAddr, bool registered) onlyOwner public {\n" +
            "        manoContracts[manoAddr] = registered;\n" +
            "        \n" +
            "        ManoContractRegistered(manoAddr, registered);\n" +
            "    }\n" +
            "    \n" +
            "    \n" +
            "    /**\n" +
            "     * @dev _to 지갑으로 _apisWei 만큼의 토큰을 송금한다.\n" +
            "     * @param _to 토큰을 받는 지갑 주소\n" +
            "     * @param _apisWei 전송되는 토큰의 양\n" +
            "     */\n" +
            "    function transfer(address _to, uint256 _apisWei) public returns (bool) {\n" +
            "        // 자신에게 송금하는 것을 방지한다\n" +
            "        require(_to != address(this));\n" +
            "        \n" +
            "        // 마스터노드 컨트렉트일 경우, APIS 송수신에 제한을 두지 않는다\n" +
            "        if(manoContracts[msg.sender] || manoContracts[_to]) {\n" +
            "            return super.transfer(_to, _apisWei);\n" +
            "        }\n" +
            "        \n" +
            "        // 송금 기능이 잠긴 지갑인지 확인한다.\n" +
            "        if(lockedWalletInfo[msg.sender].timeLockUpEnd > now && lockedWalletInfo[msg.sender].sendLock == true) {\n" +
            "            RejectedPaymentFromLockedUpWallet(msg.sender, _to, _apisWei);\n" +
            "            return false;\n" +
            "        } \n" +
            "        // 입금 받는 기능이 잠긴 지갑인지 확인한다\n" +
            "        else if(lockedWalletInfo[_to].timeLockUpEnd > now && lockedWalletInfo[_to].receiveLock == true) {\n" +
            "            RejectedPaymentToLockedUpWallet(msg.sender, _to, _apisWei);\n" +
            "            return false;\n" +
            "        } \n" +
            "        // 제한이 없는 경우, 송금을 진행한다.\n" +
            "        else {\n" +
            "            return super.transfer(_to, _apisWei);\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev _to 지갑으로 _apisWei 만큼의 APIS를 송금하고 _timeLockUpEnd 시간만큼 지갑을 잠근다\n" +
            "     * @param _to 토큰을 받는 지갑 주소\n" +
            "     * @param _apisWei 전송되는 토큰의 양(wei)\n" +
            "     * @param _timeLockUpEnd 잠금이 해제되는 시간\n" +
            "     */\n" +
            "    function transferAndLockUntil(address _to, uint256 _apisWei, uint _timeLockUpEnd) onlyOwner public {\n" +
            "        require(transfer(_to, _apisWei));\n" +
            "        \n" +
            "        walletLockBoth(_to, _timeLockUpEnd);\n" +
            "    }\n" +
            "    \n" +
            "    /**\n" +
            "     * @dev _to 지갑으로 _apisWei 만큼의 APIS를 송금하고영원히 지갑을 잠근다\n" +
            "     * @param _to 토큰을 받는 지갑 주소\n" +
            "     * @param _apisWei 전송되는 토큰의 양(wei)\n" +
            "     */\n" +
            "    function transferAndLockForever(address _to, uint256 _apisWei) onlyOwner public {\n" +
            "        require(transfer(_to, _apisWei));\n" +
            "        \n" +
            "        walletLockBothForever(_to);\n" +
            "    }\n" +
            "    \n" +
            "    \n" +
            "    /**\n" +
            "     * @dev 함수를 호출하는 지갑의 토큰을 소각한다.\n" +
            "     * \n" +
            "     * zeppelin-solidity/contracts/token/BurnableToken.sol 참조\n" +
            "     * @param _value 소각하려는 토큰의 양(Satoshi)\n" +
            "     */\n" +
            "    function burn(uint256 _value) public {\n" +
            "        require(_value <= balances[msg.sender]);\n" +
            "        require(_value <= totalSupply);\n" +
            "        \n" +
            "        address burner = msg.sender;\n" +
            "        balances[burner] -= _value;\n" +
            "        totalSupply -= _value;\n" +
            "        \n" +
            "        Burn(burner, _value);\n" +
            "    }\n" +
            "    \n" +
            "    \n" +
            "    /**\n" +
            "     * @dev Eth은 받을 수 없도록 한다.\n" +
            "     */\n" +
            "    function () public payable {\n" +
            "        revert();\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n";
}
