package opentipbot.service.exception;

import opentipbot.persistence.model.OpenTipBotCommand;

/**
 * OpenTipBot Exception at service level
 * @author Gilles Cadignan
 */
public class OpenTipBotServiceException extends Exception {

    //the opentipbotCommand object which has generated this exception
    private OpenTipBotCommand command;

    /**
     * Build BitcoinServiceException with a message only
     * @param message
     */
    public OpenTipBotServiceException(String message) {
        super(message);
    }

    /**
     * Build BitcoinServiceException with message and cause
     * @param message
     * @param cause
     */
    public OpenTipBotServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Build BitcoinServiceException with message and specific BitcoinServiceException fields
     * @param message
     * @param command
     */
    public OpenTipBotServiceException(String message, OpenTipBotCommand command) {
        super(message);
        this.command = command;
    }

    /**
     * Build BitcoinServiceException with message, cause and specific BitcoinServiceException fields
     * @param message
     * @param cause
     * @param command
     */
    public OpenTipBotServiceException(String message, Throwable cause, OpenTipBotCommand command) {
        super(message, cause);
        this.command = command;
    }

    // Getters and setters
    //********************


    public OpenTipBotCommand getCommand() {
        return command;
    }

    public void setCommand(OpenTipBotCommand command) {
        this.command = command;
    }
}
