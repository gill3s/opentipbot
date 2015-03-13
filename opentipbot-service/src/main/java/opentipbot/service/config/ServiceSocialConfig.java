package opentipbot.service.config;

import com.google.common.base.Preconditions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

/**
 * Created by gilles on 09/09/2014.
 */
@Configuration
public class ServiceSocialConfig {
    @Bean
    public TwitterTemplate getTwitterTemplate(Environment env){
        String consumerKey = env.getProperty("opentipbot.notifier.twitter.appKey");
        String consumerSecret = env.getProperty("opentipbot.notifier.twitter.appSecret");
        String accessToken = env.getProperty("opentipbot.notifier.twitter.accessToken");
        String accessTokenSecret = env.getProperty("opentipbot.notifier.twitter.accessTokenSecret");
        Preconditions.checkNotNull(consumerKey);
        Preconditions.checkNotNull(consumerSecret);
        Preconditions.checkNotNull(accessToken);
        Preconditions.checkNotNull(accessTokenSecret);
        return new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret);
    }
}
