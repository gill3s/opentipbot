package opentipbot.web.dto;

/**
 * @author Gilles Cadignan
 */
public class LastTip {
    private String profilePicUrl;
    private String tweet;
    private String tipDate;


    public LastTip(String profilePicUrl, String tweet, String tipDate) {
        this.profilePicUrl = profilePicUrl;
        this.tweet = tweet;
        this.tipDate = tipDate;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public String getTipDate() {
        return tipDate;
    }

    public void setTipDate(String tipDate) {
        this.tipDate = tipDate;
    }
}
