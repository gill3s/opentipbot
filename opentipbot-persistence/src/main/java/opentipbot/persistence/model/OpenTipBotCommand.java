package opentipbot.persistence.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Gilles Cadignan
 */
@Entity
@Table(name = "opentipbot_command")
public class OpenTipBotCommand extends BaseEntity<Long> {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private OpenTipBotCommandEnum opentipbotCommandEnum;

    private String fromUserName;

    private String toUserName;

    private String toUsernames;

    private String txId;

    private Double amout;

    private String errorMessage;

    private String notificationText;

    private Long tweetIdentifier;

    private String coinAddress;

    private String originaleTweet;

    @NotNull
    private OpenTipBotCommandStatus opentipbotCommandStatus;

    public OpenTipBotCommandEnum getOpenTipBotCommandEnum() {
        return opentipbotCommandEnum;
    }

    public OpenTipBotCommandStatus getOpenTipBotCommandStatus() {
        return opentipbotCommandStatus;
    }

    public void setOpenTipBotCommandStatus(OpenTipBotCommandStatus opentipbotCommandStatus) {
        this.opentipbotCommandStatus = opentipbotCommandStatus;
    }

    public Long getTweetIdentifier() {
        return tweetIdentifier;
    }

    public void setTweetIdentifier(Long tweetId) {
        this.tweetIdentifier = tweetId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public OpenTipBotCommandEnum getCommand() {
        return this.opentipbotCommandEnum;
    }

    public void setOpenTipBotCommandEnum(OpenTipBotCommandEnum opentipbotCommandEnum) {
        this.opentipbotCommandEnum = opentipbotCommandEnum;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public Double getAmout() {
        return amout;
    }

    public void setAmout(Double amout) {
        this.amout = amout;
    }

    public String getCoinAddress() {
        return coinAddress;
    }

    public void setCoinAddress(String coinAddress) {
        this.coinAddress = coinAddress;
    }

    public String getToUsernames() {
        return toUsernames;
    }

    public void setToUsernames(String toUsernames) {
        this.toUsernames = toUsernames;
    }

    @Transient
    public String[] getToUsernamesList(){
        return this.toUsernames.split(",");
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    public String getOriginaleTweet() {
        return originaleTweet;
    }

    public void setOriginaleTweet(String originaleTweet) {
        this.originaleTweet = originaleTweet;
    }
}
