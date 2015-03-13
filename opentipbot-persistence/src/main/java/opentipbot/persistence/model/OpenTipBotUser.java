package opentipbot.persistence.model;

import javax.persistence.*;

@Entity
@Table(name = "opentipbot_user")
/**
 * @author Gilles Cadignan
 */
public class OpenTipBotUser extends BaseEntity<Long>{

    @Id
    @GeneratedValue
    private Long id;

    private String email;
    private String displayName;
    private String userName;
    private String twitterIdentifier;
    private String coinAddress;
    private String profileImageUrl;
    private String profileUrl;

    @Basic(fetch = FetchType.LAZY)
    private byte[] coinAddressQRCode;

    @Transient
    private double balance;

    @Transient
    private double pendingBalance;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCoinAddress() {
        return coinAddress;
    }

    public void setCoinAddress(String coinAddress) {
        this.coinAddress = coinAddress;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTwitterIdentifier() {
        return twitterIdentifier;
    }

    public void setTwitterIdentifier(String twitterIdentifier) {
        this.twitterIdentifier = twitterIdentifier;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public byte[] getCoinAddressQRCode() {
        return coinAddressQRCode;
    }

    public void setCoinAddressQRCode(byte[] coinAddressQRCode) {
        this.coinAddressQRCode = coinAddressQRCode;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public double getPendingBalance() {
        return pendingBalance;
    }

    public void setPendingBalance(double pendingBalance) {
        this.pendingBalance = pendingBalance;
    }
}