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
package org.apis.jsontestsuite.suite.builder;

import org.apis.core.Transaction;
import org.apis.jsontestsuite.suite.Utils;
import org.apis.jsontestsuite.suite.model.TransactionTck;

public class TransactionBuilder {

    public static Transaction build(TransactionTck transactionTck) {

        Transaction transaction;
        if (transactionTck.getSecretKey() != null){

            transaction = new Transaction(
                    Utils.parseVarData(transactionTck.getNonce()),
                    Utils.parseVarData(transactionTck.getGasPrice()),
                    Utils.parseVarData(transactionTck.getGasLimit()),
                    Utils.parseData(transactionTck.getTo()),
                    Utils.parseVarData(transactionTck.getValue()),
                    Utils.parseData(transactionTck.getData()));
            transaction.sign(Utils.parseData(transactionTck.getSecretKey()));

        } else {

            transaction = new Transaction(
                    Utils.parseNumericData(transactionTck.getNonce()),
                    Utils.parseVarData(transactionTck.getGasPrice()),
                    Utils.parseVarData(transactionTck.getGasLimit()),
                    Utils.parseData(transactionTck.getTo()),
                    Utils.parseNumericData(transactionTck.getValue()),
                    Utils.parseData(transactionTck.getData()),
                    Utils.parseData(transactionTck.getR()),
                    Utils.parseData(transactionTck.getS()),
                    Utils.parseByte(transactionTck.getV())
            );
        }

        return transaction;
    }
}
