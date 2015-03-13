/*
 * Copyright (c) 2013, Mikhail Yevchenko. All rights reserved. PROPRIETARY/CONFIDENTIAL.
 */
package opentipbot.jsonrpcclient;

import opentipbot.jsonrpcclient.Bitcoin.Transaction;

/**
 *
 * @author Gilles Cadignan
 */
public class SimpleBitcoinPaymentListener implements BitcoinPaymentListener {

    @Override
    public void block(String blockHash) {
    }

    @Override
    public void transaction(Transaction transaction) {
    }
    
}
