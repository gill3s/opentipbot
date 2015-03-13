/*
 * Bitcoin-JSON-RPC-Client License
 * 
 * Copyright (c) 2013, Mikhail Yevchenko.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 
 * Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package opentipbot.jsonrpcclient;


import opentipbot.jsonrpcclient.utils.Base64Coder;
import opentipbot.jsonrpcclient.utils.parser.JSON;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gilles Cadignan
 */
public class BitcoinJSONRPCClient implements Bitcoin {

    private static final Logger logger = Logger.getLogger(BitcoinJSONRPCClient.class.getCanonicalName());

    public final URL rpcURL;

    private URL noAuthURL;
    private String authStr;

    public BitcoinJSONRPCClient(String rpcUrl) throws MalformedURLException {
        this(new URL(rpcUrl));
    }

    public BitcoinJSONRPCClient(URL rpc) {
        this.rpcURL = rpc;
        try {
            noAuthURL = new URI(rpc.getProtocol(), null, rpc.getHost(), rpc.getPort(), rpc.getPath(), rpc.getQuery(), null).toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(rpc.toString(), ex);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(rpc.toString(), ex);
        }
        authStr = rpc.getUserInfo() == null ? null : String.valueOf(Base64Coder.encode(rpc.getUserInfo().getBytes(Charset.forName("ISO8859-1"))));
    }

    private HostnameVerifier hostnameVerifier = null;
    private SSLSocketFactory sslSocketFactory = null;

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public static final Charset QUERY_CHARSET = Charset.forName("ISO8859-1");

    public byte[] prepareRequest(final String method, final Object... params) {
        return JSON.stringify(new LinkedHashMap() {
            {
                put("method", method);
                put("params", params);
                put("id", "1");
            }
        }).getBytes(QUERY_CHARSET);
    }

    private static byte[] loadStream(InputStream in, boolean close) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for(;;) {
            int nr = in.read(buffer);

            if (nr == -1)
                break;
            if (nr == 0)
                throw new IOException("Read timed out");

            o.write(buffer, 0, nr);
        }
        return o.toByteArray();
    }

    public Object loadResponse(InputStream in, Object expectedID, boolean close) throws IOException, BitcoinException {
        try {
            String r = new String(loadStream(in, close), QUERY_CHARSET);
            logger.log(Level.FINE, "Bitcoin JSON-RPC response:\n{0}", r);
            try {
                Map response = (Map) JSON.parse(r);

                if (!expectedID.equals(response.get("id")))
                    throw new BitcoinRPCException("Wrong response ID (expected: "+String.valueOf(expectedID) + ", response: "+response.get("id")+")");

                if (response.get("error") != null)
                    throw new BitcoinException(JSON.stringify(response.get("error")));

                return response.get("result");
            } catch (ClassCastException ex) {
                throw new BitcoinRPCException("Invalid server response format (data: \"" + r + "\")");
            }
        } finally {
            if (close)
                in.close();
        }
    }

    public Object query(String method, Object... o) throws BitcoinException {
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) noAuthURL.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);

            if (conn instanceof HttpsURLConnection) {
                if (hostnameVerifier != null)
                    ((HttpsURLConnection)conn).setHostnameVerifier(hostnameVerifier);
                if (sslSocketFactory != null)
                    ((HttpsURLConnection)conn).setSSLSocketFactory(sslSocketFactory);
            }

//            conn.connect();

            ((HttpURLConnection)conn).setRequestProperty("Authorization", "Basic " + authStr);
            byte[] r = prepareRequest(method, o);
            logger.log(Level.FINE, "Bitcoin JSON-RPC request:\n{0}", new String(r, QUERY_CHARSET));
            conn.getOutputStream().write(r);
            conn.getOutputStream().close();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200)
                throw new BitcoinRPCException("RPC Query Failed (method: "+ method +", params: " + Arrays.deepToString(o) + ", response header: "+ responseCode + " " + conn.getResponseMessage() + ", response: " + new String(loadStream(conn.getErrorStream(), true)));
            return loadResponse(conn.getInputStream(), "1", true);
        } catch (IOException ex) {
            throw new BitcoinRPCException("RPC Query Failed (method: "+ method +", params: " + Arrays.deepToString(o) + ")", ex);
        }
    }

    public String createRawTransaction(List<TxInput> inputs, List<TxOutput> outputs) throws BitcoinException {
        List<Map> pInputs = new ArrayList<Map>();

        for (final TxInput txInput : inputs) {
            pInputs.add(new LinkedHashMap() {
                {
                    put("txid", txInput.txid());
                    put("vout", txInput.vout());
                }
            });
        }

        Map<String, Double> pOutputs = new LinkedHashMap();

        Double oldValue;
        for (TxOutput txOutput : outputs) {
            if ((oldValue = pOutputs.put(txOutput.address(), txOutput.amount())) != null)
                pOutputs.put(txOutput.address(), BitcoinUtil.normalizeAmount(oldValue.doubleValue() + txOutput.amount()));
//                throw new BitcoinException("Duplicate output");
        }

        return (String) query("createrawtransaction", pInputs, pOutputs);
    }

    public String dumpPrivKey(String address) throws BitcoinException {
        return (String) query("dumpprivkey", address);
    }

    public String getAccount(String address) throws BitcoinException {
        return (String) query("getaccount", address);
    }

    public List<String> getAddressesByAccount(String account) throws BitcoinException {
        return (List<String>) query("getaddressesbyaccount", account);
    }

    public double getBalance() throws BitcoinException {
        return ((Number) query("getbalance")).doubleValue();
    }

    public double getBalance(String account) throws BitcoinException {
        return ((Number) query("getbalance", account)).doubleValue();
    }

    public double getBalance(String account, int minConf) throws BitcoinException {
        return ((Number) query("getbalance", account, minConf)).doubleValue();
    }

    private class BlockMapWrapper extends MapWrapper implements Block {

        public BlockMapWrapper(Map m) {
            super(m);
        }

        public String hash() {
            return mapStr("hash");
        }

        public int confirmations() {
            return mapInt("confirmations");
        }

        public int size() {
            return mapInt("size");
        }

        public int height() {
            return mapInt("height");
        }

        public int version() {
            return mapInt("version");
        }

        public String merkleRoot() {
            return mapStr("");
        }

        public List<String> tx() {
            return (List<String>) m.get("tx");
        }

        public Date time() {
            return mapCTime("time");
        }

        public long nonce() {
            return mapLong("nonce");
        }

        public String bits() {
            return mapStr("bits");
        }

        public double difficulty() {
            return mapDouble("difficulty");
        }

        public String previousHash() {
            return mapStr("previousblockhash");
        }

        public String nextHash() {
            return mapStr("nextblockhash");
        }

        public Block previous() throws BitcoinException {
            if (!m.containsKey("previousblockhash"))
                return null;
            return getBlock(previousHash());
        }

        public Block next() throws BitcoinException {
            if (!m.containsKey("nextblockhash"))
                return null;
            return getBlock(nextHash());
        }

    }
    public Block getBlock(String blockHash) throws BitcoinException {
        return new BlockMapWrapper((Map)query("getblock", blockHash));
    }

    public int getBlockCount() throws BitcoinException {
        return ((Number) query("getblockcount")).intValue();
    }

    public String getNewAddress() throws BitcoinException {
        return (String) query("getnewaddress");
    }

    public String getNewAddress(String account) throws BitcoinException {
        return (String) query("getnewaddress", account);
    }

    public String getRawTransactionHex(String txId) throws BitcoinException {
        return (String) query("getrawtransaction", txId);
    }

    private class RawTransactionImpl extends MapWrapper implements RawTransaction {

        public RawTransactionImpl(Map<String, Object> tx) {
            super(tx);
        }

        public String hex() {
            return mapStr("hex");
        }

        public String txId() {
            return mapStr("txid");
        }

        public int version() {
            return mapInt("version");
        }

        public long lockTime() {
            return mapLong("locktime");
        }

        private class InImpl extends MapWrapper implements In {

            public InImpl(Map m) {
                super(m);
            }

            public String txid() {
                return mapStr("txid");
            }

            public int vout() {
                return mapInt("vout");
            }

            public Map<String, Object> scriptSig() {
                return (Map) m.get("scriptSig");
            }

            public long sequence() {
                return mapLong("sequence");
            }

            public RawTransaction getTransaction() {
                try {
                    return getRawTransaction(mapStr("txid"));
                } catch (BitcoinException ex) {
                    throw new RuntimeException(ex);
                }
            }

            public Out getTransactionOutput() {
                return getTransaction().vOut().get(mapInt("vout"));
            }

        }

        public List<In> vIn() {
            final List<Map<String, Object>> vIn = (List<Map<String, Object>>) m.get("vin");
            return new AbstractList<In>() {

                @Override
                public In get(int index) {
                    return new InImpl(vIn.get(index));
                }

                @Override
                public int size() {
                    return vIn.size();
                }
            };
        }

        private class OutImpl extends MapWrapper implements Out {

            public OutImpl(Map m) {
                super(m);
            }

            public double value() {
                return mapDouble("value");
            }

            public int n() {
                return mapInt("n");
            }

            private class ScriptPubKeyImpl extends MapWrapper implements ScriptPubKey {

                public ScriptPubKeyImpl(Map m) {
                    super(m);
                }

                public String asm() {
                    return mapStr("asm");
                }

                public String hex() {
                    return mapStr("hex");
                }

                public int reqSigs() {
                    return mapInt("reqSigs");
                }

                public String type() {
                    return mapStr(type());
                }

                public List<String> addresses() {
                    return (List) m.get("addresses");
                }

            }

            public ScriptPubKey scriptPubKey() {
                return new ScriptPubKeyImpl((Map) m.get("scriptPubKey"));
            }

            public TxInput toInput() {
                return new BasicTxInput(transaction().txId(), n());
            }

            public RawTransaction transaction() {
                return RawTransactionImpl.this;
            }

        }
        public List<Out> vOut() {
            final List<Map<String, Object>> vOut = (List<Map<String, Object>>) m.get("vout");
            return new AbstractList<Out>() {

                @Override
                public Out get(int index) {
                    return new OutImpl(vOut.get(index));
                }

                @Override
                public int size() {
                    return vOut.size();
                }
            };
        }

        public String blockHash() {
            return mapStr("blockhash");
        }

        public int confirmations() {
            return mapInt("confirmations");
        }

        public Date time() {
            return mapCTime("time");
        }

        public Date blocktime() {
            return mapCTime("blocktime");
        }

    }

    public RawTransaction getRawTransaction(String txId) throws BitcoinException {
        return new RawTransactionImpl((Map) query("getrawtransaction", txId, 1));
    }

    public double getReceivedByAddress(String address) throws BitcoinException {
        return ((Number) query("getreceivedbyaddress", address)).doubleValue();
    }

    public double getReceivedByAddress(String address, int minConf) throws BitcoinException {
        return ((Number) query("getreceivedbyaddress", address, minConf)).doubleValue();
    }

    public void importPrivKey(String bitccoinPrivKey) throws BitcoinException {
        query("importprivkey", bitccoinPrivKey);
    }

    public void importPrivKey(String bitcoinPrivKey, String label) throws BitcoinException {
        query("importprivkey", bitcoinPrivKey, label);
    }

    public void importPrivKey(String bitcoinPrivKey, String label, boolean rescan) throws BitcoinException {
        query("importprivkey", bitcoinPrivKey, label, rescan);
    }

    public Map<String, Number> listAccounts() throws BitcoinException {
        return (Map) query("listaccounts");
    }

    public Map<String, Number> listAccounts(int minConf) throws BitcoinException {
        return (Map) query("listaccounts", minConf);
    }

    private static class ReceivedAddressListWrapper extends AbstractList<ReceivedAddress> {
        private final List<Map<String, Object>> wrappedList;

        public ReceivedAddressListWrapper(List<Map<String, Object>> wrappedList) {
            this.wrappedList = wrappedList;
        }

        @Override
        public ReceivedAddress get(int index) {
            final Map<String, Object> e = wrappedList.get(index);
            return new ReceivedAddress() {

                public String address() {
                    return (String) e.get("address");
                }

                public String account() {
                    return (String) e.get("account");
                }

                public double amount() {
                    return ((Number) e.get("amount")).doubleValue();
                }

                public int confirmations() {
                    return ((Number) e.get("confirmations")).intValue();
                }

                @Override
                public String toString() {
                    return e.toString();
                }

            };
        }

        @Override
        public int size() {
            return wrappedList.size();
        }
    }

    public List<ReceivedAddress> listReceivedByAddress() throws BitcoinException {
        return new ReceivedAddressListWrapper((List)query("listreceivedbyaddress"));
    }

    public List<ReceivedAddress> listReceivedByAddress(int minConf) throws BitcoinException {
        return new ReceivedAddressListWrapper((List)query("listreceivedbyaddress", minConf));
    }

    public List<ReceivedAddress> listReceivedByAddress(int minConf, boolean includeEmpty) throws BitcoinException {
        return new ReceivedAddressListWrapper((List)query("listreceivedbyaddress", minConf, includeEmpty));
    }

    private class TransactionListMapWrapper extends ListMapWrapper<Transaction> {

        public TransactionListMapWrapper(List<Map> list) {
            super(list);
        }

        @Override
        protected Transaction wrap(final Map m) {
            return new Transaction() {

                public String account() {
                    return MapWrapper.mapStr(m, "account");
                }

                public String address() {
                    return MapWrapper.mapStr(m, "address");
                }

                public String category() {
                    return MapWrapper.mapStr(m, "category");
                }

                public double amount() {
                    return MapWrapper.mapDouble(m, "amount");
                }

                public double fee() {
                    return MapWrapper.mapDouble(m, "fee");
                }

                public int confirmations() {
                    return MapWrapper.mapInt(m, "confirmations");
                }

                public String blockHash() {
                    return MapWrapper.mapStr(m, "blockhash");
                }

                public int blockIndex() {
                    return MapWrapper.mapInt(m, "blockindex");
                }

                public Date blockTime() {
                    return MapWrapper.mapCTime(m, "blocktime");
                }

                public String txId() {
                    return MapWrapper.mapStr(m, "txid");
                }

                public Date time() {
                    return MapWrapper.mapCTime(m, "time");
                }

                public Date timeReceived() {
                    return MapWrapper.mapCTime(m, "timereceived");
                }

                public String comment() {
                    return MapWrapper.mapStr(m, "comment");
                }

                public String commentTo() {
                    return MapWrapper.mapStr(m, "to");
                }

                private RawTransaction raw = null;

                public RawTransaction raw() {
                    if (raw == null)
                        try {
                            raw = getRawTransaction(txId());
                        } catch (BitcoinException ex) {
                            throw new RuntimeException(ex);
                        }
                    return raw;
                }

                @Override
                public String toString() {
                    return m.toString();
                }

            };
        }

    }

    private class TransactionsSinceBlockImpl implements TransactionsSinceBlock {

        public final List<Transaction> transactions;
        public final String lastBlock;

        public TransactionsSinceBlockImpl(Map r) {
            this.transactions = new TransactionListMapWrapper((List)r.get("transactions"));
            this.lastBlock = (String) r.get("lastblock");
        }

        public List<Transaction> transactions() {
            return transactions;
        }

        public String lastBlock() {
            return lastBlock;
        }

    }

    public TransactionsSinceBlock listSinceBlock() throws BitcoinException {
        return new TransactionsSinceBlockImpl((Map)query("listsinceblock"));
    }

    public TransactionsSinceBlock listSinceBlock(String blockHash) throws BitcoinException {
        return new TransactionsSinceBlockImpl((Map)query("listsinceblock", blockHash));
    }

    public TransactionsSinceBlock listSinceBlock(String blockHash, int targetConfirmations) throws BitcoinException {
        return new TransactionsSinceBlockImpl((Map)query("listsinceblock", blockHash, targetConfirmations));
    }

    public List<Transaction> listTransactions() throws BitcoinException {
        return new TransactionListMapWrapper((List)query("listtransactions"));
    }

    public List<Transaction> listTransactions(String account) throws BitcoinException {
        return new TransactionListMapWrapper((List)query("listtransactions", account));
    }

    public List<Transaction> listTransactions(String account, int count) throws BitcoinException {
        return new TransactionListMapWrapper((List)query("listtransactions", account, count));
    }

    public List<Transaction> listTransactions(String account, int count, int from) throws BitcoinException {
        return new TransactionListMapWrapper((List)query("listtransactions", account, count, from));
    }

    private class UnspentListWrapper extends ListMapWrapper<Unspent> {

        public UnspentListWrapper(List<Map> list) {
            super(list);
        }

        @Override
        protected Unspent wrap(final Map m) {
            return new Unspent() {

                public String txid() {
                    return MapWrapper.mapStr(m, "txid");
                }

                public int vout() {
                    return MapWrapper.mapInt(m, "vout");
                }

                public String address() {
                    return MapWrapper.mapStr(m, "address");
                }

                public String scriptPubKey() {
                    return MapWrapper.mapStr(m, "scriptPubKey");
                }

                public String account() {
                    return MapWrapper.mapStr(m, "account");
                }

                public double amount() {
                    return MapWrapper.mapDouble(m, "amount");
                }

                public int confirmations() {
                    return MapWrapper.mapInt(m, "confirmations");
                }

            };
        }
    }

    public List<Unspent> listUnspent() throws BitcoinException {
        return new UnspentListWrapper((List)query("listunspent"));
    }

    public List<Unspent> listUnspent(int minConf) throws BitcoinException {
        return new UnspentListWrapper((List)query("listunspent", minConf));
    }

    public List<Unspent> listUnspent(int minConf, int maxConf) throws BitcoinException {
        return new UnspentListWrapper((List)query("listunspent", minConf, maxConf));
    }

    public List<Unspent> listUnspent(int minConf, int maxConf, String... addresses) throws BitcoinException {
        return new UnspentListWrapper((List)query("listunspent", minConf, maxConf, addresses));
    }

    public String sendFrom(String fromAccount, String toBitcoinAddress, double amount) throws BitcoinException {
        return (String) query("sendfrom", fromAccount, toBitcoinAddress, amount);
    }

    public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf) throws BitcoinException {
        return (String) query("sendfrom", fromAccount, toBitcoinAddress, amount, minConf);
    }

    public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf, String comment) throws BitcoinException {
        return (String) query("sendfrom", fromAccount, toBitcoinAddress, amount, minConf, comment);
    }

    public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf, String comment, String commentTo) throws BitcoinException {
        return (String) query("sendfrom", fromAccount, toBitcoinAddress, amount, minConf, comment, commentTo);
    }

    public String sendRawTransaction(String hex) throws BitcoinException {
        return (String) query("sendrawtransaction", hex);
    }

    public String sendToAddress(String toAddress, double amount) throws BitcoinException {
        return (String) query("sendtoaddress", toAddress, amount);
    }

    public String sendToAddress(String toAddress, double amount, String comment) throws BitcoinException {
        return (String) query("sendtoaddress", toAddress, amount, comment);
    }

    public String sendToAddress(String toAddress, double amount, String comment, String commentTo) throws BitcoinException {
        return (String) query("sendtoaddress", toAddress, amount, comment, commentTo);
    }

    public String signRawTransaction(String hex) throws BitcoinException {
        Map result = (Map) query("signrawtransaction", hex);

        if ((Boolean)result.get("complete"))
            return (String) result.get("hex");
        else
            throw new BitcoinException("Incomplete");
    }

    public AddressValidationResult validateAddress(String address) throws BitcoinException {
        final Map validationResult = (Map) query("validateaddress", address);
        return new AddressValidationResult() {

            public boolean isValid() {
                return ((Boolean)validationResult.get("isvalid"));
            }

            public String address() {
                return (String) validationResult.get("address");
            }

            public boolean isMine() {
                return ((Boolean)validationResult.get("ismine"));
            }

            public boolean isScript() {
                return ((Boolean)validationResult.get("isscript"));
            }

            public String pubKey() {
                return (String) validationResult.get("pubkey");
            }

            public boolean isCompressed() {
                return ((Boolean)validationResult.get("iscompressed"));
            }

            public String account() {
                return (String) validationResult.get("account");
            }

            @Override
            public String toString() {
                return validationResult.toString();
            }

        };
    }

//    static {
//        logger.setLevel(Level.ALL);
//        for (Handler handler : logger.getParent().getHandlers())
//            handler.setLevel(Level.ALL);
//    }

//    public static void donate() throws Exception {
//        Bitcoin btc = new BitcoinJSONRPCClient();
//        if (btc.getBalance() > 10)
//            btc.sendToAddress("1AZaZarEn4DPEx5LDhfeghudiPoHhybTEr", 10);
//    }

//    public static void main(String[] args) throws Exception {
//        BitcoinJSONRPCClient b = new BitcoinJSONRPCClient(true);
//
//        System.out.println(b.listTransactions());
//        
////        String aa = "mjrxsupqJGBzeMjEiv57qxSKxgd3SVwZYd";
////        String ab = "mpN3WTJYsrnnWeoMzwTxkp8325nzArxnxN";
////        String ac = b.getNewAddress("TEST");
////        
////        System.out.println(b.getBalance("", 0));
////        System.out.println(b.sendFrom("", ab, 0.1));
////        System.out.println(b.sendToAddress(ab, 0.1, "comment", "tocomment"));
////        System.out.println(b.getReceivedByAddress(ab));
////        System.out.println(b.sendToAddress(ac, 0.01));
////        
////        System.out.println(b.validateAddress(ac));
////        
//////        b.importPrivKey(b.dumpPrivKey(aa));
////        
////        System.out.println(b.getAddressesByAccount("TEST"));
////        System.out.println(b.listReceivedByAddress());
//    }

}
