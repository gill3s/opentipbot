package opentipbot.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import opentipbot.persistence.model.OpenTipBotCommand;
import opentipbot.persistence.model.OpenTipBotUser;
import opentipbot.service.OpenTipBotUserService;
import opentipbot.web.dto.TipReceivedDTO;
import opentipbot.web.dto.TipSentDTO;
import opentipbot.web.dto.WithdrawalDTO;
import opentipbot.web.security.SecurityContext;

import javax.inject.Inject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Gilles Cadignan
 */
@RestController
@RequestMapping("/data")
public class AjaxController {
    public static final String TWITTER_URL = "https://twitter.com/";
    private final Twitter twitter;

    @Autowired
    private OpenTipBotUserService opentipbotUserService;


    @Inject
    public AjaxController(Twitter twitter) {
        this.twitter = twitter;
    }


    @RequestMapping(value = "/tipssent", method = RequestMethod.GET, headers="Accept=application/json")
    public List<TipSentDTO> tipsSent(Locale locale) {
        OpenTipBotUser currentUser = SecurityContext.getCurrentUser();

        List<OpenTipBotCommand> tipCommands =  opentipbotUserService.getTipsSent(currentUser.getUserName());
        List<TipSentDTO> result = new ArrayList<>();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        for (OpenTipBotCommand command : tipCommands){
            result.add(new TipSentDTO(dateFormat.format(command.getModificationTime().toDate()), getTwitterUserLink(command.getToUserName()),
                    command.getOpenTipBotCommandEnum().name(), command.getAmout(),""+command.getOpenTipBotCommandStatus().ordinal(), command.getErrorMessage() == null ? "" : command.getErrorMessage() ));
        }

        return result;

    }

    @RequestMapping(value = "/tipsreceived", method = RequestMethod.GET,headers="Accept=application/json")
    public List<TipReceivedDTO> tipsReceived(Locale locale) {
        OpenTipBotUser currentUser = SecurityContext.getCurrentUser();

        List<OpenTipBotCommand> tipCommands =  opentipbotUserService.getTipsReceived(currentUser.getUserName());
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        List<TipReceivedDTO> result = new ArrayList<>();
        for (OpenTipBotCommand command : tipCommands){
            result.add(new TipReceivedDTO(getTwitterUserLink(command.getFromUserName()), command.getAmout(), dateFormat.format(command.getModificationTime().toDate()),
                    command.getOpenTipBotCommandEnum().name(),""+command.getOpenTipBotCommandStatus().ordinal(), command.getErrorMessage() == null ? "" : command.getErrorMessage()));
        }

        return result;

    }

    @RequestMapping(value = "/withdrawals", method = RequestMethod.GET,headers="Accept=application/json")
    public List<WithdrawalDTO> withdrawals(Locale locale) {
        OpenTipBotUser currentUser = SecurityContext.getCurrentUser();

        List<OpenTipBotCommand> tipCommands =  opentipbotUserService.getWithdrawals(currentUser.getUserName());
        List<WithdrawalDTO> result = new ArrayList<>();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        for (OpenTipBotCommand command : tipCommands){
            result.add(new WithdrawalDTO(command.getAmout(), command.getCoinAddress(), dateFormat.format(command.getModificationTime().toDate()),
                    command.getTxId(),""+command.getOpenTipBotCommandStatus().ordinal(), command.getErrorMessage() == null ? "" : command.getErrorMessage()));
        }

        return result;

    }

    private static String getTwitterUserLink(String userName){
        String url =  TWITTER_URL + userName;
        return "<a href='" + url +"' target='_blank'>@"+userName+"</a>";
    }

}