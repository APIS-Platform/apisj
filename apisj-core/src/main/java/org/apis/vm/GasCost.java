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
package org.apis.vm;

/**
 * The fundamental network cost unit. Paid for exclusively by APIS, which is converted
 * freely to and from Gas as required. Gas does not exist outside of the internal Ethereum
 * computation engine; its price is set by the Transaction and miners are free to
 * ignore Transactions whose Gas price is too low.
 */
public class GasCost {

    /* backwards compatibility, remove eventually */
    private final int STEP = 7;
    private final int SSTORE = 2000;
    /* backwards compatibility, remove eventually */
    private final int ZEROSTEP = 0;
    private final int QUICKSTEP = 14;
    private final int FASTESTSTEP = 20;
    private final int FASTSTEP = 35;
    private final int MIDSTEP = 56;
    private final int SLOWSTEP = 70;
    private final int EXTSTEP = 150;

    private final int GENESISGASLIMIT = 10000000;
    private final int MINGASLIMIT = 1000000;
    private final long BLOCK_GAS_LIMIT = 50_000_000;

    private final int BALANCE = 2500;
    private final int SHA3 = 200;
    private final int SHA3_WORD = 40;
    private final int SLOAD = 1400;
    private final int STOP = 0;
    private final int SUICIDE = 35000;
    private final int CLEAR_SSTORE = 35000;
    private final int SET_SSTORE = 150000;
    private final int RESET_SSTORE = 35000;
    private final int REFUND_SSTORE = 100000;
    private final int CREATE = 200000;

    private final int JUMPDEST = 7;
    private final int CREATE_DATA_BYTE = 35;
    private final int CALL = 5000;
    private final int STIPEND_CALL = 17000;
    private final int VT_CALL = 70000;  //value transfer call
    private final int NEW_ACCT_CALL = 170000;  //new account call
    private final int MEMORY = 20;
    private final int SUICIDE_REFUND = 200000;
    private final int QUAD_COEFF_DIV = 4000;
    private final int CREATE_DATA = 2000;
    private final int TX_NO_ZERO_DATA = 650;
    private final int TX_ZERO_DATA = 40;
    private final int TRANSACTION = 200_000;
    private final int TRANSACTION_CREATE_CONTRACT = 500_000;
    private final int LOG_GAS = 2500;
    private final int LOG_DATA_GAS = 50;
    private final int LOG_TOPIC_GAS = 2500;
    private final int COPY_GAS = 20;
    private final int EXP_GAS = 70;
    private final int EXP_BYTE_GAS = 70;
    private final int IDENTITY = 100;
    private final int IDENTITY_WORD = 20;
    private final int RIPEMD160 = 4200;
    private final int RIPEMD160_WORD = 800;
    private final int SHA256 = 400;
    private final int SHA256_WORD = 80;
    private final int EC_RECOVER = 20000;
    private final int EXT_CODE_SIZE = 4500;
    private final int EXT_CODE_COPY = 4500;
    private final int NEW_ACCT_SUICIDE = 210000;

    public int getSTEP() {
        return STEP;
    }

    public int getSSTORE() {
        return SSTORE;
    }

    public int getZEROSTEP() {
        return ZEROSTEP;
    }

    public int getQUICKSTEP() {
        return QUICKSTEP;
    }

    public int getFASTESTSTEP() {
        return FASTESTSTEP;
    }

    public int getFASTSTEP() {
        return FASTSTEP;
    }

    public int getMIDSTEP() {
        return MIDSTEP;
    }

    public int getSLOWSTEP() {
        return SLOWSTEP;
    }

    public int getEXTSTEP() {
        return EXTSTEP;
    }

    public int getGENESISGASLIMIT() {
        return GENESISGASLIMIT;
    }

    public int getMINGASLIMIT() {
        return MINGASLIMIT;
    }

    public int getBALANCE() {
        return BALANCE;
    }

    public int getSHA3() {
        return SHA3;
    }

    public int getSHA3_WORD() {
        return SHA3_WORD;
    }

    public int getSLOAD() {
        return SLOAD;
    }

    public int getSTOP() {
        return STOP;
    }

    public int getSUICIDE() {
        return SUICIDE;
    }

    public int getCLEAR_SSTORE() {
        return CLEAR_SSTORE;
    }

    public int getSET_SSTORE() {
        return SET_SSTORE;
    }

    public int getRESET_SSTORE() {
        return RESET_SSTORE;
    }

    public int getREFUND_SSTORE() {
        return REFUND_SSTORE;
    }

    public int getCREATE() {
        return CREATE;
    }

    public int getJUMPDEST() {
        return JUMPDEST;
    }

    public int getCREATE_DATA_BYTE() {
        return CREATE_DATA_BYTE;
    }

    public int getCALL() {
        return CALL;
    }

    public int getSTIPEND_CALL() {
        return STIPEND_CALL;
    }

    public int getVT_CALL() {
        return VT_CALL;
    }

    public int getNEW_ACCT_CALL() {
        return NEW_ACCT_CALL;
    }

    public int getNEW_ACCT_SUICIDE() {
        return NEW_ACCT_SUICIDE;
    }

    public int getMEMORY() {
        return MEMORY;
    }

    public int getSUICIDE_REFUND() {
        return SUICIDE_REFUND;
    }

    public int getQUAD_COEFF_DIV() {
        return QUAD_COEFF_DIV;
    }

    public int getCREATE_DATA() {
        return CREATE_DATA;
    }

    public int getTX_NO_ZERO_DATA() {
        return TX_NO_ZERO_DATA;
    }

    public int getTX_ZERO_DATA() {
        return TX_ZERO_DATA;
    }

    public int getTRANSACTION() {
        return TRANSACTION;
    }

    public int getTRANSACTION_CREATE_CONTRACT() {
        return TRANSACTION_CREATE_CONTRACT;
    }

    public int getLOG_GAS() {
        return LOG_GAS;
    }

    public int getLOG_DATA_GAS() {
        return LOG_DATA_GAS;
    }

    public int getLOG_TOPIC_GAS() {
        return LOG_TOPIC_GAS;
    }

    public int getCOPY_GAS() {
        return COPY_GAS;
    }

    public int getEXP_GAS() {
        return EXP_GAS;
    }

    public int getEXP_BYTE_GAS() {
        return EXP_BYTE_GAS;
    }

    public int getIDENTITY() {
        return IDENTITY;
    }

    public int getIDENTITY_WORD() {
        return IDENTITY_WORD;
    }

    public int getRIPEMD160() {
        return RIPEMD160;
    }

    public int getRIPEMD160_WORD() {
        return RIPEMD160_WORD;
    }

    public int getSHA256() {
        return SHA256;
    }

    public int getSHA256_WORD() {
        return SHA256_WORD;
    }

    public int getEC_RECOVER() {
        return EC_RECOVER;
    }

    public int getEXT_CODE_SIZE() {
        return EXT_CODE_SIZE;
    }

    public int getEXT_CODE_COPY() {
        return EXT_CODE_COPY;
    }

    public long getBLOCK_GAS_LIMIT() {
        return BLOCK_GAS_LIMIT;
    }
}
