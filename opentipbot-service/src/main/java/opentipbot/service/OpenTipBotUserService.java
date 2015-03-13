package opentipbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Service;
import opentipbot.persistence.model.OpenTipBotCommand;
import opentipbot.persistence.model.OpenTipBotUser;
import opentipbot.persistence.repository.OpenTipBotCommandRepository;
import opentipbot.persistence.repository.OpenTipBotUserRepository;
import opentipbot.service.exception.OpenTipBotServiceException;

import java.util.List;

@Service
public class OpenTipBotUserService {

    @Autowired
    private OpenTipBotUserRepository opentipbotUserRepository;

    @Autowired
    private OpenTipBotCommandRepository opentipbotCommandRepository;

    @Autowired
    private BitcoinService bitcoinService;

    @Autowired
    Environment env;

    @Autowired
    TwitterTemplate twitterTemplate;

    public OpenTipBotUser findByTwitterIdentifier(String twitterIndentifier){
        return opentipbotUserRepository.findByTwitterIdentifier(twitterIndentifier);
    }

    public OpenTipBotUser findByUserName(String userName){
        return opentipbotUserRepository.findByUserName(userName);
    }

    public void createNewOpenTipBotUser(OpenTipBotUser opentipbotUser) throws OpenTipBotServiceException {

        //generate new coin address and corresponding QR code pic
        opentipbotUser.setCoinAddress(bitcoinService.getNewAdress(opentipbotUser.getUserName()));
        opentipbotUser.setCoinAddressQRCode(bitcoinService.generateAdressQRCodeImage(opentipbotUser.getCoinAddress()));

        //check if user already has pending tips
        double pendingTips = bitcoinService.getPendingBalance(opentipbotUser.getUserName());
        if (pendingTips > 0){
            bitcoinService.sendTip(opentipbotUser.getUserName() + env.getProperty("opentipbot.bitcoin.pendingAccountSuffix"),
                    opentipbotUser.getUserName(), pendingTips);
        }
        //follow new user
        twitterTemplate.friendOperations().follow(opentipbotUser.getUserName());


        opentipbotUserRepository.save(opentipbotUser);
    }

    public void updateOpenTipBotUser(OpenTipBotUser opentipbotUser) {
        opentipbotUserRepository.saveAndFlush(opentipbotUser);
    }

    public List<OpenTipBotCommand> getTipsSent(String userName){
        return opentipbotCommandRepository.getTipsSent(userName);
    }

    public List<OpenTipBotCommand> getTipsReceived(String userName){
        return opentipbotCommandRepository.getTipsReceived(userName);
    }

    public List<OpenTipBotCommand> getWithdrawals(String userName){
        return opentipbotCommandRepository.getWithDrawals(userName);
    }

}
