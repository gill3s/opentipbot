/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opentipbot.jsonrpcclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 *
 * @author Gilles Cadignan
 */
public class BitcoinRawTxBuilder {

    public final Bitcoin bitcoin;

    public BitcoinRawTxBuilder(Bitcoin bitcoin) {
        this.bitcoin = bitcoin;
    }
    public LinkedHashSet<Bitcoin.TxInput> inputs = new LinkedHashSet();
    public List<Bitcoin.TxOutput> outputs = new ArrayList();

    private class Input extends Bitcoin.BasicTxInput {

        public Input(String txid, int vout) {
            super(txid, vout);
        }

        public Input(Bitcoin.TxInput copy) {
            this(copy.txid(), copy.vout());
        }

        @Override
        public int hashCode() {
            return txid.hashCode() + vout;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof Bitcoin.TxInput))
                return false;
            Bitcoin.TxInput other = (Bitcoin.TxInput) obj;
            return vout == other.vout() && txid.equals(other.txid());
        }

    }
    public BitcoinRawTxBuilder in(Bitcoin.TxInput in) {
        inputs.add(new Input(in.txid(), in.vout()));
        return this;
    }

    public BitcoinRawTxBuilder in(String txid, int vout) {
        in(new Bitcoin.BasicTxInput(txid, vout));
        return this;
    }

    public BitcoinRawTxBuilder out(String address, double amount) {
        if (amount <= 0d)
            return this;
        outputs.add(new Bitcoin.BasicTxOutput(address, amount));
        return this;
    }

    public BitcoinRawTxBuilder in(double value) throws BitcoinException {
        return in(value, 6);
    }

    public BitcoinRawTxBuilder in(double value, int minConf) throws BitcoinException {
        List<Bitcoin.Unspent> unspent = bitcoin.listUnspent(minConf);
        double v = value;
        for (Bitcoin.Unspent o : unspent) {
            if (!inputs.contains(new Input(o))) {
                in(o);
                v = BitcoinUtil.normalizeAmount(v - o.amount());
            }
            if (v < 0)
                break;
        }
        if (v > 0)
            throw new BitcoinException("Not enough bitcoins ("+v+"/"+value+")");
        return this;
    }

    private HashMap<String, Bitcoin.RawTransaction> txCache = new HashMap<String, Bitcoin.RawTransaction>();

    private Bitcoin.RawTransaction tx(String txId) throws BitcoinException {
        Bitcoin.RawTransaction tx = txCache.get(txId);
        if (tx != null)
            return tx;
        tx = bitcoin.getRawTransaction(txId);
        txCache.put(txId, tx);
        return tx;
    }

    public BitcoinRawTxBuilder outChange(String address) throws BitcoinException {
        return outChange(address, 0d);
    }

    public BitcoinRawTxBuilder outChange(String address, double fee) throws BitcoinException {
        double is = 0d;
        for (Bitcoin.TxInput i : inputs)
            is = BitcoinUtil.normalizeAmount(is + tx(i.txid()).vOut().get(i.vout()).value());
        double os = fee;
        for (Bitcoin.TxOutput o : outputs)
            os = BitcoinUtil.normalizeAmount(os + o.amount());
        if (os < is)
            out(address, BitcoinUtil.normalizeAmount(is - os));
        return this;
    }

    public String create() throws BitcoinException {
        return bitcoin.createRawTransaction(new ArrayList<Bitcoin.TxInput>(inputs), outputs);
    }
    
    public String sign() throws BitcoinException {
        return bitcoin.signRawTransaction(create());
    }

    public String send() throws BitcoinException {
        return bitcoin.sendRawTransaction(sign());
    }

}
