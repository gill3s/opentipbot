package opentipbot.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import opentipbot.persistence.config.PersistenceContext;
import opentipbot.persistence.model.OpenTipBotCommand;
import opentipbot.persistence.model.OpenTipBotCommandEnum;
import opentipbot.persistence.model.OpenTipBotCommandStatus;
import opentipbot.persistence.repository.OpenTipBotCommandRepository;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * @author Gilles Cadignan
 */
@ContextConfiguration(classes = PersistenceContext.class)
@Transactional
@TransactionConfiguration(transactionManager="transactionManager", defaultRollback=true)
@RunWith(SpringJUnit4ClassRunner.class)
public class OpenTipBotCommandRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests
{

    @Autowired
    protected OpenTipBotCommandRepository opentipbotCommandRepository;


    @Test
    public void createOpenTipBotCommandTest()
    {
        try {
            OpenTipBotCommand opentipbotCommand =  createNewTestCommand();

            assertNotNull("command id is null", opentipbotCommand.getId());


        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void findByTweetIdentifierTest(){
        try {
            createNewTestCommand();
            List<OpenTipBotCommand> opentipbotCommands = opentipbotCommandRepository.findByTweetIdentifier(109820L);

            assertEquals("command not found", 1, opentipbotCommands.size());


        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void findByOpenTipBotCommandStatusAndOpenTipBotCommandEnumOrderByCreationTimeAscTest(){
        try {
            createNewTestCommand();
            List<OpenTipBotCommand> opentipbotCommandList = opentipbotCommandRepository.
                    findByOpenTipBotCommandStatusAndOpenTipBotCommandEnumOrderByCreationTimeAsc(OpenTipBotCommandStatus.NEW, OpenTipBotCommandEnum.TIP);

            assertEquals("command not found", 1, opentipbotCommandList.size());


        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    private OpenTipBotCommand createNewTestCommand() {
        OpenTipBotCommand opentipbotCommand = new OpenTipBotCommand();
        opentipbotCommand.setOpenTipBotCommandEnum(OpenTipBotCommandEnum.TIP);
        opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.NEW);
        opentipbotCommand.setAmout(120.0);
        opentipbotCommand.setTweetIdentifier(109820L);
        opentipbotCommand.setFromUserName("toto");
        opentipbotCommand.setToUsernames("titi");
        opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
        return opentipbotCommand;
    }

}