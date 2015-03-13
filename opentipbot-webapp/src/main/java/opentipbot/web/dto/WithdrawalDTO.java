package opentipbot.web.dto;

/**
 * @author Gilles Cadignan
 */
public class WithdrawalDTO {
    private double amount;
    private String toAddress;
    private String when;
    private String txId;
    private String status;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public WithdrawalDTO(double amount, String toAddress, String when, String txId, String status, String errorMessage) {
        this.errorMessage = errorMessage;
        this.amount = amount;
        this.toAddress = toAddress.substring(0,7) + "...";
        this.when = when;
        this.txId = txId.substring(0,7) + "...";
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
