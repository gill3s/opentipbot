package opentipbot.web.dto;

/**
 * @author Gilles Cadignan
 */
public class TipReceivedDTO {
    private String fromUserName;
    private double amount;
    private String when;
    private String type;
    private String status;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public TipReceivedDTO(String fromUserName, double amount, String when, String type, String status, String errorMessage) {
        this.errorMessage = errorMessage;
        this.fromUserName = fromUserName;
        this.amount = amount;
        this.when = when;
        this.type = type;
        this.status = status;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
