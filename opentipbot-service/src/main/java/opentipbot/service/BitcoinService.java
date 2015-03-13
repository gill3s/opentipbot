package opentipbot.service;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import opentipbot.jsonrpcclient.BitcoinException;
import opentipbot.jsonrpcclient.BitcoinJSONRPCClient;
import opentipbot.service.exception.BitcoinServiceException;
import opentipbot.service.exception.OpenTipBotServiceException;

import java.io.ByteArrayOutputStream;

/**
 * @author Gilles Cadignan
 * Simple wrapper around BitcoinJSONRPCClient
 */
@Service
public class BitcoinService {

    private static final Logger logger = LoggerFactory.getLogger(BitcoinService.class.getCanonicalName());

    @Autowired
    private Environment env;

    @Autowired
    private BitcoinJSONRPCClient bitcoinJSONRPCClient;

    /**
     * Returns new adress for a given account name
     * @param account
     * @return
     * @throws BitcoinServiceException
     */
    public String getNewAdress(String account) throws BitcoinServiceException {
        logger.debug("Getting new adress for " + account);
        try {
            return bitcoinJSONRPCClient.getNewAddress(account);
        } catch (BitcoinException e) {
            logger.error("Failed to get new adress");
            throw new BitcoinServiceException(env.getProperty("opentipbot.errors.bitcoin.address.generation").replace("ACCOUNT_ID",account), e);
        }

    }
    /**
     * Returns new adress for the bot
     * @return
     * @throws BitcoinServiceException
     */
    public String getNewAdress() throws BitcoinServiceException {
        logger.debug("Getting new adress for opentipbot");
        try {
            return bitcoinJSONRPCClient.getNewAddress(env.getProperty("opentipbot.bot.account"));
        } catch (BitcoinException e) {
            logger.error("Failed to get new adress");
            throw new BitcoinServiceException(env.getProperty("opentipbot.errors.bitcoin.address.generation").replace("ACCOUNT_ID","opentipbot"), e);
        }

    }
    /**
     * Return the btc balance of a given user account
     * @param account
     * @return balance as double
     * @throws BitcoinException
     */
    public double getBalance(String account) throws BitcoinServiceException {

        try {
            logger.debug("Getting balance of " + account);
            return bitcoinJSONRPCClient.getBalance(account);

        } catch (BitcoinException e) {
            logger.error("Failed to retrive account balance");
            throw new BitcoinServiceException(env.getProperty("opentipbot.errors.bitcoin.getBalance").replace("ACCOUNT_ID", account), e);

        }

    }

    /**
     * Generates a byte array representation of QRCode image for a given btc address
     * @param coinAddress
     * @return
     */
    public byte[] generateAdressQRCodeImage(String coinAddress) throws OpenTipBotServiceException {
        try {
            logger.debug("Generating QR Code pic for address " + coinAddress);
            ByteArrayOutputStream out = QRCode.from(coinAddress).to(ImageType.PNG).stream();

            return out.toByteArray();

        } catch (Exception e){
            logger.error("Failed to generate QR code pic");
            throw new BitcoinServiceException(env.getProperty("opentipbot.errors.bitcoin.invalid.address"), e);
        }
    }

    /**
     * returns true if the given btc address is valid
     * @param coinAddress
     * @return
     */
    public boolean isValidCoinAddress(String coinAddress) throws OpenTipBotServiceException {
        try {
            logger.debug("CHack if " + coinAddress + " is a valid coin address");
            return bitcoinJSONRPCClient.validateAddress(coinAddress).isValid();

        } catch (BitcoinException e) {
            logger.error("Error while validating address");
            throw new OpenTipBotServiceException(env.getProperty("opentipbot.errors.bitcoin.addressValidation"), e);

        }
    }

    /**
     * send bitcoin tip
     * @param fromUserName
     * @param toUserName
     * @param amount
     * @return tx id
     */
    public String sendTip(String fromUserName, String toUserName, double amount) throws BitcoinServiceException {
        try {
            logger.info("sending " + amount + " btc from " + fromUserName + " to " + toUserName);
            //we add the txFee to the amount
            return bitcoinJSONRPCClient.sendFrom(fromUserName, getNewAdress(toUserName), amount, 5);

        } catch (BitcoinException e) {
            logger.error("Error while sending coins");
            throw new BitcoinServiceException(env.getProperty("opentipbot.errors.bitcoin.sendTip").replace("TX_STR",+amount + "btc from " + fromUserName+" to "+toUserName+ " "+ e.getMessage()), e);

        }
    }

    /**
     * Send coins to bitcoin address
     * @param fromUserName
     * @param coinAdress
     * @return tx id
     * @throws BitcoinServiceException
     */
    public String withdrawToAddress(String fromUserName, String coinAdress) throws BitcoinServiceException {
        try {
            double amount = getBalance(fromUserName) - Double.parseDouble(env.getProperty("opentipbot.withdrawalFee"));
            logger.info("sending " + amount + " btc from " + fromUserName + " to adress " + coinAdress);
            String txId = bitcoinJSONRPCClient.sendFrom(fromUserName, coinAdress, amount, 20);
            logger.info("withdraw succesful txid : " + txId);
            logger.info("sending withdrawal fee to opentipbotAdress");
            String txId2 = bitcoinJSONRPCClient.sendFrom(fromUserName, env.getProperty("opentipbot.bot.coinAddress"),
                    getBalance(fromUserName)- Double.parseDouble(env.getProperty("opentipbot.txFee")), 10);
            logger.info("withdraw opentipbot fee succesful txid : "+txId2);
            return txId;
        } catch (BitcoinException e) {
            logger.error("Error while sending coins");
            throw new BitcoinServiceException(env.getProperty("opentipbot.errors.bitcoin.sendToAddress").replace("TX_STR", coinAdress), e);
        }
    }




    /**
     * Returns user pending balance
     * @param userName
     * @return
     * @throws BitcoinServiceException
     */
    public double getPendingBalance(String userName) throws BitcoinServiceException {
        logger.debug("Get pending balance of  " + userName);
        return getBalance(userName  + env.getProperty("opentipbot.bitcoin.pendingAccountSuffix"));
    }
}
