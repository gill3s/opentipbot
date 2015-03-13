package opentipbot.jobs.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import opentipbot.service.OpenTipBotService;
import opentipbot.service.exception.OpenTipBotServiceException;

/**
 * @author Gilles Cadignan
 * Job task handling new tweet and processing commands
 */
public class OpenTipBotProcessCommandsTask {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private OpenTipBotService opentipbotService;

    public OpenTipBotProcessCommandsTask(OpenTipBotService opentipbotService) {
        this.opentipbotService = opentipbotService;
    }


    //process task every 10 seconds
    @Scheduled(initialDelay = 15000, fixedDelay = 10000)
    @Async
    public void processNewOpenTipBotCommands() throws OpenTipBotServiceException {
        log.debug("Processing new commands...");
        this.opentipbotService.processNewOpenTipBotCommands();
    }
}
