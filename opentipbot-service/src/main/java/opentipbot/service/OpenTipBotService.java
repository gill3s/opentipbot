package opentipbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.social.ResourceNotFoundException;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TweetData;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Service;
import opentipbot.persistence.model.OpenTipBotCommand;
import opentipbot.persistence.model.OpenTipBotCommandEnum;
import opentipbot.persistence.model.OpenTipBotCommandStatus;
import opentipbot.persistence.model.OpenTipBotUser;
import opentipbot.persistence.repository.OpenTipBotCommandRepository;
import opentipbot.persistence.repository.OpenTipBotUserRepository;
import opentipbot.service.exception.BitcoinServiceException;
import opentipbot.service.exception.OpenTipBotServiceException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gilles Cadignan
 */
@Service
public class OpenTipBotService {


    private static final Logger logger = LoggerFactory.getLogger(OpenTipBotService.class.getCanonicalName());

    private String TIP_TWEET_REGEX;
    private String WITHDRAW_TWEET_REGEX;
    private String TIP_RAIN_TWEET_REGEX;
    private String TIP_RANDOM_TWEET_REGEX;
    private Pattern TIP_COMMAND_PATTERN;
    private Pattern WITHDRAW_COMMAND_PATTERN;
    private Pattern TIP_RAIN_COMMAND_PATTERN;
    private Pattern TIP_RANDOM_COMMAND_PATTERN;



    @Autowired
    private TwitterTemplate twitterTemplate;

    public TwitterTemplate getTwitterTemplate() {
        return twitterTemplate;
    }

    @Autowired
    private OpenTipBotCommandRepository opentipbotCommandRepository;

    @Autowired
    private BitcoinService bitcoinService;

    @Autowired
    private OpenTipBotUserRepository opentipbotUserRepository;

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        String BOT_NAME = env.getProperty("opentipbot.bot.name");
        TIP_TWEET_REGEX = "@"+BOT_NAME+"\\s+tip\\s+@(\\w+)\\s+(\\d*\\.?\\d*).*";
        WITHDRAW_TWEET_REGEX = "@"+BOT_NAME+"\\s+withdraw\\s+(Z[a-zA-Z0-9]+)\\s*.*";
        TIP_RAIN_TWEET_REGEX = "@"+BOT_NAME+"\\s+tiprain\\s+((@\\w+\\s*)+)(\\d*\\.?\\d*).*";
        TIP_RANDOM_TWEET_REGEX = "@"+BOT_NAME+"\\s+tiprandom\\s+(\\d*\\.?\\d*).*";
        TIP_COMMAND_PATTERN = Pattern.compile(TIP_TWEET_REGEX,  Pattern.CASE_INSENSITIVE);
        WITHDRAW_COMMAND_PATTERN = Pattern.compile(WITHDRAW_TWEET_REGEX,  Pattern.CASE_INSENSITIVE);
        TIP_RAIN_COMMAND_PATTERN = Pattern.compile(TIP_RAIN_TWEET_REGEX,  Pattern.CASE_INSENSITIVE);
        TIP_RANDOM_COMMAND_PATTERN = Pattern.compile(TIP_RANDOM_TWEET_REGEX,  Pattern.CASE_INSENSITIVE);
    }

    /**
     * Get new mentions from timeline and persist new commands
     * @throws OpenTipBotServiceException
     */
    public void handleNewTweets() throws OpenTipBotServiceException {

        List<Tweet> incomingTweets = twitterTemplate.timelineOperations().getMentions();

        logger.debug("Found "+incomingTweets.size()+" new tweets");

        incomingTweets = filterTweets(incomingTweets);


        if (!incomingTweets.isEmpty()) {
            logger.info("Found "+incomingTweets.size()+" valid tweets");
            logger.info("Persisting new opentipbot commands...");

            persistOpenTipBotCommands(incomingTweets);

            logger.info(incomingTweets.size()+" commands saved successfully.");

        }

        logger.debug("Operation Complete");

    }

    public void handleNewTweet(Tweet tweet) throws OpenTipBotServiceException {
        List<Tweet> incomingTweets = new ArrayList<Tweet>();
        incomingTweets.add(tweet);

        logger.debug("New tweet detected");

        incomingTweets = filterTweets(incomingTweets);

        if (!incomingTweets.isEmpty()) {

            logger.info("Persisting new opentipbot command...");

            persistOpenTipBotCommands(incomingTweets);

            logger.info("command saved successfully.");

        }

        logger.debug("Operation Complete");
    }


    /**
     * process new commands found in DB
     * @throws OpenTipBotServiceException
     */
    public void processNewOpenTipBotCommands() throws OpenTipBotServiceException {
        logger.debug("Start command processing");

        processTips(opentipbotCommandRepository.findByOpenTipBotCommandStatusAndOpenTipBotCommandEnumOrderByCreationTimeAsc(OpenTipBotCommandStatus.NEW, OpenTipBotCommandEnum.TIP));

        processTips(opentipbotCommandRepository.findByOpenTipBotCommandStatusAndOpenTipBotCommandEnumOrderByCreationTimeAsc(OpenTipBotCommandStatus.NEW, OpenTipBotCommandEnum.TIP_RAIN));

        processTips(opentipbotCommandRepository.findByOpenTipBotCommandStatusAndOpenTipBotCommandEnumOrderByCreationTimeAsc(OpenTipBotCommandStatus.NEW, OpenTipBotCommandEnum.TIP_RANDOM));

        processWithdrawals(opentipbotCommandRepository.findByOpenTipBotCommandStatusAndOpenTipBotCommandEnumOrderByCreationTimeAsc(OpenTipBotCommandStatus.NEW, OpenTipBotCommandEnum.WITHDRAW));

        processReceiverNotifications(opentipbotCommandRepository.findByOpenTipBotCommandStatusAndOpenTipBotCommandEnumOrderByCreationTimeAsc(OpenTipBotCommandStatus.NEW, OpenTipBotCommandEnum.NOTIFY_RECEIVER));

        processErrorNotifications(opentipbotCommandRepository.findByOpenTipBotCommandStatusAndOpenTipBotCommandEnumOrderByCreationTimeAsc(OpenTipBotCommandStatus.NEW, OpenTipBotCommandEnum.NOTIFY_ERROR));

        processFavs(opentipbotCommandRepository.findByOpenTipBotCommandStatusAndOpenTipBotCommandEnumOrderByCreationTimeAsc(OpenTipBotCommandStatus.NEW, OpenTipBotCommandEnum.FAV));

        logger.debug("Operation Complete");

    }

    private void processFavs(List<OpenTipBotCommand> commands) {
        if (!commands.isEmpty())
            logger.debug("Processing " + commands.size() + " fav commands");
        for (OpenTipBotCommand opentipbotCommand : commands){
            favOriginalMessage(opentipbotCommand);
        }
    }

    private void processErrorNotifications(List<OpenTipBotCommand> commands) {
        if (!commands.isEmpty())
            logger.debug("Processing " + commands.size() + " error notification commands");
        for (OpenTipBotCommand opentipbotCommand : commands){
            notifyValidationError(opentipbotCommand, opentipbotCommand.getErrorMessage());
        }
    }

    private void processReceiverNotifications(List<OpenTipBotCommand> commands) {
        if (!commands.isEmpty())
            logger.debug("Processing " + commands.size() + " receiver notification commands");
        for (OpenTipBotCommand opentipbotCommand : commands){
            notifyReceiver(opentipbotCommand);
        }

    }

    /**
     * process withdrawal commands
     * @param commands
     * @throws OpenTipBotServiceException
     */
    private void processWithdrawals(List<OpenTipBotCommand> commands) throws OpenTipBotServiceException {

        //Local variables
        String txId;

        for (OpenTipBotCommand opentipbotCommand : commands){

            if (validateWithdrawalCommand(opentipbotCommand)){
                try {

                    logger.debug("Processing withdrawal command #" + opentipbotCommand.getId());
                    OpenTipBotUser opentipbotUser = opentipbotUserRepository.findByUserName(opentipbotCommand.getFromUserName());

                    double amount = bitcoinService.getBalance(opentipbotCommand.getFromUserName()) -  - Double.parseDouble(env.getProperty("opentipbot.withdrawalFee"));
                    txId = bitcoinService.withdrawToAddress(opentipbotUser.getUserName(), opentipbotCommand.getCoinAddress());

                    logger.debug("withdrawal successful processed tx id : " + txId);
                    opentipbotCommand.setTxId(txId);
                    opentipbotCommand.setAmout(amount);
                    opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.PROCESSED);
                    opentipbotCommandRepository.saveAndFlush(opentipbotCommand);

                } catch (BitcoinServiceException e) {

                    logger.error("OpenTipBot command failed with error : " + e.getMessage());
                    e.printStackTrace();

                }

                //fav original message
                favOriginalMessage(opentipbotCommand);

            } else {
                logger.error("OpenTipBot withdrawal command validation failed");
                //change command status
                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.ERROR);
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);

            }
        }
    }
    /**
     * Process tip commands
     * @param commands
     * @throws BitcoinServiceException
     */
    private void processTips(List<OpenTipBotCommand> commands) throws BitcoinServiceException {
        if (!commands.isEmpty()) {
            logger.debug("Processing " + commands.size() + " new Tip commands");

            //local variables
            TweetData recipientNotification;
            String toAccountIdentifier;
            String txId;
            double fee;

            for (OpenTipBotCommand opentipbotCommand : commands) {

                if (validateTipCommand(opentipbotCommand)) {
                    //send tip to receiver
                    try {
                        toAccountIdentifier = opentipbotCommand.getToUserName();

                        txId = bitcoinService.sendTip(opentipbotCommand.getFromUserName(), toAccountIdentifier,
                                opentipbotCommand.getAmout());

                        logger.info("coins succesfull sent, txId : " + txId);

                        opentipbotCommand.setTxId(txId);
                        opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.PROCESSED);
                        opentipbotCommandRepository.saveAndFlush(opentipbotCommand);

                        //Twitter notifiactions
                        //fav original message
                        favOriginalMessage(opentipbotCommand);
                        //build a message to warn the receiver
                        notifyReceiver(opentipbotCommand);

                    } catch (BitcoinServiceException e) {

                        logger.error("tip failed with error : " + e.getMessage());
                        opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.ERROR);
                        opentipbotCommand.setErrorMessage(e.getMessage());
                        opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                        e.printStackTrace();

                    }



                } else {
                    logger.error("tip validation failed");

                }
            }
        }
    }

    /**
     * Send a notification to a tip receiver
     * @param opentipbotCommand
     */
    private void notifyReceiver(OpenTipBotCommand opentipbotCommand) {
        try {

            TweetData recipientNotification;
            recipientNotification = new TweetData(getRandomTipNotificationMessage(opentipbotCommand.getFromUserName(), opentipbotCommand.getToUserName(), opentipbotCommand.getAmout()));
            recipientNotification.inReplyToStatus(opentipbotCommand.getTweetIdentifier());

            //tweet messge to receiver
            twitterTemplate.timelineOperations().updateStatus(recipientNotification);

            //set command as PROCESSED if it is a NOTIFY_RECEIVER command
            if (opentipbotCommand.getCommand() == OpenTipBotCommandEnum.NOTIFY_RECEIVER){

                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.PROCESSED);
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);

            }

        } catch (RuntimeException e){
            logger.error("receiver notification failed : " + e.getMessage());

        }
    }

    /**
     * Favorite the message source of a command
     * @param opentipbotCommand
     */
    private void favOriginalMessage(OpenTipBotCommand opentipbotCommand) {
        try {

            twitterTemplate.timelineOperations().addToFavorites(opentipbotCommand.getTweetIdentifier());

            //set command as PROCESSED if it is a FAV command
            if (opentipbotCommand.getCommand() == OpenTipBotCommandEnum.FAV){

                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.PROCESSED);
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);

            }
        } catch (RuntimeException e){

            logger.error("tweet fav failed : " + e.getMessage());

        }
    }


    /**
     * CHeck if a withdrawal command is valid
     * @param opentipbotCommand
     * @return
     * @throws OpenTipBotServiceException
     */
    boolean validateWithdrawalCommand(OpenTipBotCommand opentipbotCommand) throws OpenTipBotServiceException {

        logger.debug("Start withdrawal command validation");

        try {

            //check if user is registered
            if (!validateUserRegistration(opentipbotCommand.getFromUserName())) {
                logger.error("withdrawal failed, user not registered");
                //change command status and save it
                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.ERROR);
                opentipbotCommand.setErrorMessage(env.getProperty("opentipbot.notifications.pleaseRegisterFirst"));
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                notifyValidationError(opentipbotCommand, "@" + opentipbotCommand.getFromUserName() + " " + env.getProperty("opentipbot.notifications.pleaseRegisterFirst"));
                return false;
            }

            //check whether the user has sufficient funds in his balance
            if (!validateSufficientBalanceToWIthdraw(opentipbotCommand.getFromUserName())) {
                logger.error("withdrawal failed, balance too low");
                //change command status and save it
                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.ERROR);
                opentipbotCommand.setErrorMessage(env.getProperty("opentipbot.notifications.withdraw.insufficientFunds"));
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                notifyValidationError(opentipbotCommand, "@" + opentipbotCommand.getFromUserName() + " " + env.getProperty("opentipbot.notifications.withdraw.insufficientFunds"));
                return false;
            }

            //check whether the user had provided a valid coin address
            if (!bitcoinService.isValidCoinAddress(opentipbotCommand.getCoinAddress())) {
                logger.error("withdrawal failed, invalid coin address");
                //change command status and save it
                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.ERROR);
                opentipbotCommand.setErrorMessage(env.getProperty("opentipbot.errors.bitcoin.invalid.address"));
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                notifyValidationError(opentipbotCommand, "@" + opentipbotCommand.getFromUserName() + " " + env.getProperty("opentipbot.errors.bitcoin.invalid.address"));
                return false;
            }

            return true;

        } catch (BitcoinServiceException e) {
            //attach command to exception before throwing it
            e.setCommand(opentipbotCommand);
            throw e;
        }
    }

    /**
     * Notify sender that a validation error occured
     * @param opentipbotCommand
     * @param errorMessage
     */
    private void notifyValidationError(OpenTipBotCommand opentipbotCommand, String errorMessage) {
        try {
            TweetData errorReply;
            errorReply = new TweetData(errorMessage);
            errorReply.inReplyToStatus(opentipbotCommand.getTweetIdentifier());

            //tweet messge to sender
            twitterTemplate.timelineOperations().updateStatus(errorReply);


            //set command as PROCESSED if it is a NOTIFY_RECEIVER command
            if (opentipbotCommand.getCommand() == OpenTipBotCommandEnum.NOTIFY_ERROR){

                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.PROCESSED);
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);

            }
        } catch (RuntimeException e){

            logger.error("notification failed " + e.getMessage());


        }
    }

    /**
     * Choose randomly a notification message
     * @param fromUserName
     * @param amount
     * @return
     */
    String getRandomTipNotificationMessage(String fromUserName, String toUserName, double amount) {
        logger.debug("choosing a random receiver notification message");
        String[] tipNotificationMessages = env.getProperty("opentipbot.notifications.tipReceived").split("//") ;
        Random generator = new Random();
        return tipNotificationMessages[generator.nextInt(tipNotificationMessages.length)].replace("RECEIVER", toUserName).
                replace("SENDER", fromUserName).replace("AMOUNT", formatDouble(amount));

    }

    /**
     * Process tip command validation
     * @param opentipbotCommand
     * @return
     * @throws BitcoinServiceException
     */
    boolean validateTipCommand(OpenTipBotCommand opentipbotCommand) throws BitcoinServiceException {
        logger.debug("Start opentipbot Tip command valitation ");

        String errorMessage = "";
        TweetData errorReply;

        try {

            //check if user is registered
            if (!validateUserRegistration(opentipbotCommand.getFromUserName())) {
                logger.error("Tip validation failed, user is not registered");
                //change command status and save it
                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.ERROR);
                opentipbotCommand.setErrorMessage(env.getProperty("opentipbot.notifications.pleaseRegisterFirst"));
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                notifyValidationError(opentipbotCommand, "@" + opentipbotCommand.getFromUserName() + " " + env.getProperty("opentipbot.notifications.pleaseRegisterFirst"));
                return false;
            }

            //check if user exists
            String userScreenName =  validateUserExists(opentipbotCommand.getToUserName());
            if (userScreenName == null) {
                logger.error("Tip validation failed, user does not exist");
                //change command status and save it
                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.ERROR);
                opentipbotCommand.setErrorMessage(env.getProperty("opentipbot.notifications.userDoesNotExist"));
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                notifyValidationError(opentipbotCommand, "@" + opentipbotCommand.getFromUserName() + " " + env.getProperty("opentipbot.notifications.userDoesNotExist"));
                return false;
            } else opentipbotCommand.setToUserName(userScreenName);

            //check if user does not tip himself
            if (opentipbotCommand.getFromUserName().equalsIgnoreCase(opentipbotCommand.getToUserName())) {
                logger.error("Tip validation failed, user cannot tip himself");
                //change command status and save it
                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.ERROR);
                opentipbotCommand.setErrorMessage(env.getProperty("opentipbot.notifications.cannotTipYourself"));
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                notifyValidationError(opentipbotCommand, "@" + opentipbotCommand.getFromUserName() + " " + env.getProperty("opentipbot.notifications.cannotTipYourself"));
                return false;
            }
            //check whether the user has sufficient funds in his balance
            if (!validateSufficientBalance(opentipbotCommand.getAmout(), opentipbotCommand.getFromUserName())) {
                logger.error("Tip validation failed, insufficient funds");
                //change command status and save it
                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.ERROR);
                opentipbotCommand.setErrorMessage(env.getProperty("opentipbot.notifications.tip.insufficientFunds"));
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                notifyValidationError(opentipbotCommand, "@" + opentipbotCommand.getFromUserName() + " " + env.getProperty("opentipbot.notifications.tip.insufficientFunds"));
                return false;
            }

            //verify minimum transaction size
            if (!validateMinimumAmount(opentipbotCommand.getAmout())) {
                logger.error("Tip validation failed, amount too low");
                //change command status and save it
                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.ERROR);
                opentipbotCommand.setErrorMessage(env.getProperty("opentipbot.notifications.tip.insufficientFunds"));
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                notifyValidationError(opentipbotCommand,  "@" + opentipbotCommand.getFromUserName() + " " + env.getProperty("opentipbot.notifications.tip.insufficientFunds"));
                return false;
            }

            return true;

        } catch (BitcoinServiceException e) {
            //attach command to exception before throwing it
            e.setCommand(opentipbotCommand);
            throw e;
        }
    }

    private String validateUserExists(String toUserName) {
        try {
            TwitterProfile profile = twitterTemplate.userOperations().getUserProfile(toUserName);
            if (profile != null) {
                logger.info("user found : "+ profile.getScreenName());
                return profile.getScreenName();

            }
            else return null;
        } catch (ResourceNotFoundException e){
            return null;
        }
    }

    /**
     * return true if the amount is above or equal to the
     * minimum tip amount
     * @param amount
     * @return
     */
    boolean validateMinimumAmount(double amount) {
        logger.debug("Parsing coin amount");
        return amount >= Double.parseDouble(env.getProperty("opentipbot.bitcoin.minimumTip"));
    }

    /**
     * return true if the user is already registered
     * @param userName
     * @return
     */
    boolean validateUserRegistration(String userName) {
        return opentipbotUserRepository.findByUserName(userName) != null;
    }

    /**
     * return true if the user have sufficient funds to tip the
     * given amount
     * @param amount
     * @param userName
     * @return
     * @throws BitcoinServiceException
     */
    boolean validateSufficientBalance(double amount, String userName) throws BitcoinServiceException {
        return bitcoinService.getBalance(userName) >=
                amount /*+ Double.parseDouble(env.getProperty("opentipbot.bitcoin.txFee"))*/;
    }

    /**
     * return true if the user have sufficient funds to
     * process withdrawal
     * @param userName
     * @return
     * @throws BitcoinServiceException
     */
    boolean validateSufficientBalanceToWIthdraw(String userName) throws BitcoinServiceException {
        return  bitcoinService.getBalance(userName) >=
                Double.parseDouble(env.getProperty("opentipbot.bitcoin.minimumWithrawAmount"));
    }

    /**
     * Persist new opentipbot command from valid new tweets
     * @param incomingTweets
     * @throws OpenTipBotServiceException
     */
    void persistOpenTipBotCommands(List<Tweet> incomingTweets) throws OpenTipBotServiceException {
        for (Tweet incomingTweet : incomingTweets){
            Matcher matcher = TIP_COMMAND_PATTERN.matcher(removeLineSeparators(incomingTweet.getText()));
            if (matcher.matches()){
                logger.debug("Persisting new tip command");
                //Create a new opentipbotCommand
                OpenTipBotCommand opentipbotCommand = new OpenTipBotCommand();
                opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.NEW);
                opentipbotCommand.setOpenTipBotCommandEnum(OpenTipBotCommandEnum.TIP);
                opentipbotCommand.setTweetIdentifier(incomingTweet.getId());
                opentipbotCommand.setFromUserName(incomingTweet.getFromUser());
                opentipbotCommand.setAmout(getCoinAmout(matcher.group(2)));
                opentipbotCommand.setToUserName(matcher.group(1));
                opentipbotCommand.setOriginaleTweet(incomingTweet.getText());
                opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
            } else {
                matcher = WITHDRAW_COMMAND_PATTERN.matcher(removeLineSeparators(incomingTweet.getText()));
                if (matcher.matches()) {
                    logger.debug("Persisting new withdraw command");
                    OpenTipBotCommand opentipbotCommand = new OpenTipBotCommand();
                    opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.NEW);
                    opentipbotCommand.setOpenTipBotCommandEnum(OpenTipBotCommandEnum.WITHDRAW);
                    opentipbotCommand.setTweetIdentifier(incomingTweet.getId());
                    opentipbotCommand.setFromUserName(incomingTweet.getFromUser());
                    opentipbotCommand.setCoinAddress(matcher.group(1));
                    opentipbotCommand.setOriginaleTweet(incomingTweet.getText());
                    opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                } else {
                    matcher = TIP_RAIN_COMMAND_PATTERN.matcher(removeLineSeparators(incomingTweet.getText()));
                    if (matcher.matches()) {

                        logger.debug("Persisting new tip rain command");
                        for (String receiver : getRainUsernames(matcher.group(1))) {
                            OpenTipBotCommand opentipbotCommand = new OpenTipBotCommand();
                            opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.NEW);
                            opentipbotCommand.setOpenTipBotCommandEnum(OpenTipBotCommandEnum.TIP_RAIN);
                            opentipbotCommand.setTweetIdentifier(incomingTweet.getId());
                            opentipbotCommand.setFromUserName(incomingTweet.getFromUser());
                            opentipbotCommand.setAmout(getCoinAmout(matcher.group(matcher.groupCount())));
                            opentipbotCommand.setToUserName(receiver);
                            opentipbotCommand.setOriginaleTweet(incomingTweet.getText());
                            opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                        }
                    } else {
                        matcher = TIP_RANDOM_COMMAND_PATTERN.matcher(removeLineSeparators(incomingTweet.getText()));
                        if (matcher.matches()) {
                            logger.debug("Persisting new tip random command");
                            OpenTipBotCommand opentipbotCommand = new OpenTipBotCommand();
                            opentipbotCommand.setOpenTipBotCommandStatus(OpenTipBotCommandStatus.NEW);
                            opentipbotCommand.setOpenTipBotCommandEnum(OpenTipBotCommandEnum.TIP_RANDOM);
                            opentipbotCommand.setTweetIdentifier(incomingTweet.getId());
                            opentipbotCommand.setFromUserName(incomingTweet.getFromUser());
                            opentipbotCommand.setToUserName(findRandomFollower(incomingTweet.getFromUserId()));
                            opentipbotCommand.setAmout(getCoinAmout(matcher.group(1)));
                            opentipbotCommand.setOriginaleTweet(incomingTweet.getText());
                            opentipbotCommandRepository.saveAndFlush(opentipbotCommand);
                        }
                    }
                }
            }
        }

    }

    /**
     * select a random follower of a given twitterTemplate user
     * @param fromUser
     * @return
     */
    String findRandomFollower(long fromUser) {
        logger.debug("Searching a random follower of " + fromUser);
        List<TwitterProfile> followers = twitterTemplate.friendOperations().getFollowers(fromUser);

        Random generator = new Random();
        int choseOne = generator.nextInt(followers.size());

        TwitterProfile chosenProfile = followers.get(choseOne);
        logger.debug("Found :  @" + chosenProfile.getScreenName());
        return chosenProfile.getScreenName();
    }

    /**
     * parse user names in a tip rain command
     * @param userNames
     * @return
     */
    List<String> getRainUsernames(String userNames){
        String[] splitted = userNames.split("@");
        String userName;
        List<String> result = new ArrayList<String>();
        for (String part : splitted){
            if (part.length() > 0){
                userName = part.trim();
                //verify if user exist
                TwitterProfile profile = twitterTemplate.userOperations().getUserProfile(userName);
                if (profile!=null){
                    result.add(userName);
                }
            }
        }
        return result;
    }

    /**
     * filter tweet to be transformed in opentipbot commands
     * @param incomingTweets
     * @return
     */
    List<Tweet> filterTweets(List<Tweet> incomingTweets) {
        return filterNotProcessedTweets(filterValidTweets(incomingTweets));
    }

    /**
     * filter tweet that have not been alrdeady processed
     * @param incomingTweets
     * @return
     */
    List<Tweet> filterNotProcessedTweets(List<Tweet> incomingTweets) {
        List<Tweet> notProcessed = new ArrayList();
        for (Tweet incomingTweet : incomingTweets){
            List<OpenTipBotCommand> opentipbotCommands = opentipbotCommandRepository.findByTweetIdentifier(incomingTweet.getId());
            if (opentipbotCommands.isEmpty()){
                notProcessed.add(incomingTweet);
            }
        }
        return notProcessed;
    }

    /**
     * return coin amount
     * @param amount
     * @return
     */
    Double getCoinAmout(String amount) {
        return Double.parseDouble(amount);
    }

    /**
     * Filter valid tweet given reg exp
     * @param incomingTweets
     * @return
     */
    List<Tweet> filterValidTweets(List<Tweet> incomingTweets) {
        List<Tweet> validTweets = new ArrayList();
        Matcher matcher = null;
        for (Tweet incomingTweet : incomingTweets) {

            matcher = TIP_COMMAND_PATTERN.matcher(removeLineSeparators(incomingTweet.getText()));

            if (matcher.matches()) {
                validTweets.add(incomingTweet);
            } else {
                matcher = TIP_RANDOM_COMMAND_PATTERN.matcher(removeLineSeparators(incomingTweet.getText()));
                if (matcher.matches()) {
                    validTweets.add(incomingTweet);
                } else {
                    matcher = WITHDRAW_COMMAND_PATTERN.matcher(removeLineSeparators(incomingTweet.getText()));
                    if (matcher.matches()) {
                        validTweets.add(incomingTweet);
                    } else {
                        matcher = TIP_RAIN_COMMAND_PATTERN.matcher(removeLineSeparators(incomingTweet.getText()));
                        if (matcher.matches()) {
                            validTweets.add(incomingTweet);
                        } else logger.debug("Invalid incoming tweet : \"" + incomingTweet.getText() + "\" : skipped");

                    }
                }
            }
        }
        return validTweets;
    }

    private String removeLineSeparators(String str) {
        str = str.replaceAll("[\\r\\n]", "");
        return str;
    }

    /**
     * return last succesful tip commands (including tip random and tip rain)
     * @return
     */
    public List<OpenTipBotCommand> getLastTips()  {

        Page<OpenTipBotCommand> commands = opentipbotCommandRepository.getLastTips(new PageRequest(0,5));

        return commands.getContent();

    }

    /**
     * format amount
     * @param d
     * @return
     */
    private static String formatDouble(double d)
    {
        if(d == (long) d)
            return String.format("%d",(long)d);
        else
            return String.format("%s",d);
    }
}
