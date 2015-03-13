package opentipbot.web.dto;


/**
 * @author Gilles Cadignan
 */
public class TipSentDTO {
    private String when;
    private String toUserName;
    private String type;
    private double amount;
    private String status;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public TipSentDTO(String when, String toUserName, String type, double amount, String status, String errorMessage) {
        this.errorMessage = errorMessage;
        this.when = when;
        this.toUserName = toUserName;
        this.type = type;
        this.amount = amount;
        this.status = status;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
