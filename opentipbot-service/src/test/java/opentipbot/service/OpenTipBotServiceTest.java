package opentipbot.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import opentipbot.service.config.ServiceTestConfig;
import opentipbot.service.exception.OpenTipBotServiceException;

/**
 * @author Gilles Cadignan
 */
@ContextConfiguration(classes = ServiceTestConfig.class)
@Transactional
@TransactionConfiguration(transactionManager="transactionManager", defaultRollback=true)
@RunWith(SpringJUnit4ClassRunner.class)
public class OpenTipBotServiceTest extends AbstractTransactionalJUnit4SpringContextTests
{
    @Autowired
    protected OpenTipBotService opentipbotService;

    @Test
    public void handleNewTweetsTest()
    {
        try {
            opentipbotService.handleNewTweets();
        } catch (OpenTipBotServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void processNewOpenTipBotCommandsTest()
    {
        try {
            opentipbotService.processNewOpenTipBotCommands();
        } catch (OpenTipBotServiceException e) {
            e.printStackTrace();
        }
    }
}