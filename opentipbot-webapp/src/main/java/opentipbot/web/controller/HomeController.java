package opentipbot.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import opentipbot.persistence.model.OpenTipBotUser;
import opentipbot.service.BitcoinService;
import opentipbot.service.exception.BitcoinServiceException;
import opentipbot.web.exception.OpenTipBotWebappException;
import opentipbot.web.security.SecurityContext;

/**
 * Simple little @Controller that invokes Facebook and renders the result.
 * The injected {@link Twitter} reference is configured with the required authorization credentials for the current user behind the scenes.
 * @author Gilles Cadignan
 */
@Controller
public class HomeController {

    @Autowired
    private BitcoinService bitcoinService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) throws OpenTipBotWebappException {
        OpenTipBotUser currentUser = SecurityContext.getCurrentUser();
        try {
            double balance = bitcoinService.getBalance(currentUser.getUserName());

            if (balance <= 0)
                balance = bitcoinService.getPendingBalance(currentUser.getUserName());

            currentUser.setBalance(balance);


            model.addAttribute("user",currentUser);

        } catch (BitcoinServiceException e) {
            throw new OpenTipBotWebappException(e);
        }

        return "home";
    }


}