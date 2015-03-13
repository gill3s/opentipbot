package opentipbot.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import opentipbot.persistence.model.OpenTipBotUser;
import opentipbot.service.config.ServiceTestConfig;
import opentipbot.service.exception.OpenTipBotServiceException;

/**
 * @author Gilles Cadignan
 */
@ContextConfiguration(classes = ServiceTestConfig.class)
@Transactional
@TransactionConfiguration(transactionManager="transactionManager", defaultRollback=true)
@RunWith(SpringJUnit4ClassRunner.class)
public class OpenTipBotUserServiceTest extends AbstractTransactionalJUnit4SpringContextTests
{

    @Autowired
    protected OpenTipBotUserService opentipbotUserService;


    @Test
    public void createNewOpenTipBotUser()
    {
        OpenTipBotUser opentipbotUser = new OpenTipBotUser();
        opentipbotUser.setDisplayName("Gilles Cadignan");
        opentipbotUser.setTwitterIdentifier("1092039809834");
        opentipbotUser.setUserName("gillesCadignan");
        opentipbotUser.setProfileImageUrl("http://slkjslk.com/oiuoi.png");
        opentipbotUser.setProfileUrl("http://slkjslk.com/oiuoi.png");
        try {
            opentipbotUserService.createNewOpenTipBotUser(opentipbotUser);
        } catch (OpenTipBotServiceException e) {
            logger.error("Error while create new OpenTipBotUser");
        }
    }
}