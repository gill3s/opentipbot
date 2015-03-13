package opentipbot.service.exception;

import opentipbot.persistence.model.OpenTipBotCommand;

/**
 * Created by gilles on 05/09/2014.
 */
public class BitcoinServiceException extends OpenTipBotServiceException {
    public BitcoinServiceException(String message) {
        super(message);
    }

    public BitcoinServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public BitcoinServiceException(String message, OpenTipBotCommand command) {
        super(message,command);
    }

    public BitcoinServiceException(String message, Throwable cause, OpenTipBotCommand command) {
        super(message, cause, command);
    }
}
