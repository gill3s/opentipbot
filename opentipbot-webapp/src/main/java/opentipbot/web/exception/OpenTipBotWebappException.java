package opentipbot.web.exception;

/**
 * Created by gilles on 05/09/2014.
 */
public class OpenTipBotWebappException extends Exception {

    public OpenTipBotWebappException(String message) {
        super(message);
    }

    public OpenTipBotWebappException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenTipBotWebappException(Throwable cause) {
        super(cause);
    }
}
