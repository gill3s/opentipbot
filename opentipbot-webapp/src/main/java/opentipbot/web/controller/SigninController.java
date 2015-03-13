package opentipbot.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import opentipbot.persistence.model.OpenTipBotCommand;
import opentipbot.persistence.model.OpenTipBotUser;
import opentipbot.service.OpenTipBotService;
import opentipbot.service.OpenTipBotUserService;
import opentipbot.web.dto.LastTip;
import opentipbot.web.exception.OpenTipBotWebappException;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Gilles Cadignan
 */
@Controller
public class SigninController {

    @Autowired
    private OpenTipBotService opentipbotService;

    @Autowired
    private OpenTipBotUserService opentipbotUserService;

    @RequestMapping(value = "/signin", method = RequestMethod.GET)
    public String signin(Model model, Locale locale) throws OpenTipBotWebappException {
        List<OpenTipBotCommand> commands = opentipbotService.getLastTips();
        List<LastTip> lastTips = new ArrayList<>();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        for (OpenTipBotCommand c : commands){
            OpenTipBotUser u =  opentipbotUserService.findByUserName(c.getFromUserName());
            lastTips.add(new LastTip(u.getProfileImageUrl(), c.getOriginaleTweet(), dateFormat.format(c.getModificationTime().toDate())));
        }

        model.addAttribute("lastTips",lastTips);
        return "signin";
    }


}