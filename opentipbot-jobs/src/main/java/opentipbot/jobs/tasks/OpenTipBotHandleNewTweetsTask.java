package opentipbot.jobs.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.social.twitter.api.*;
import opentipbot.service.OpenTipBotService;
import opentipbot.service.exception.OpenTipBotServiceException;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gilles Cadignan
 * Job task handling new tweet and processing commands
 */
public class OpenTipBotHandleNewTweetsTask {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private OpenTipBotService opentipbotService;

    private Stream tweetStream;

    public OpenTipBotHandleNewTweetsTask(final OpenTipBotService opentipbotService) {
        this.opentipbotService = opentipbotService;
        //Open twitter stream
        List<StreamListener> listeners = new ArrayList<StreamListener>();
        StreamListener listener = new StreamListener() {
            public void onTweet(Tweet tweet) {
                try {
                    opentipbotService.handleNewTweet(tweet);
                } catch (OpenTipBotServiceException e) {
                    log.error("An error occurred while handling new tweet : " + e.getMessage() );
                }
            }

            public void onLimit(int numberOfLimitedTweets) {
                System.out.println("LIMIT:  " + numberOfLimitedTweets);
            }

            public void onDelete(StreamDeleteEvent deleteEvent) {
                System.out.println("DELETE:  " + deleteEvent.getTweetId());
            }

            public void onWarning(StreamWarningEvent warnEvent) {
                System.out.println("WARNING:  " + warnEvent.getCode());
            }
        };
        listeners.add(listener);
        UserStreamParameters params = new UserStreamParameters().with(UserStreamParameters.WithOptions.USER).includeReplies(true);
        this.tweetStream = opentipbotService.getTwitterTemplate().streamingOperations().user(params, listeners);

    }

    //process task every 500 seconds
    @Scheduled(fixedDelay = 500000)
    @Async
    public void handleNewTweets() throws OpenTipBotServiceException {
        log.debug("Searching for new tweets...");
        this.opentipbotService.handleNewTweets();
    }

    @PreDestroy
    public void destroy(){
        this.tweetStream.close();
    }



}
