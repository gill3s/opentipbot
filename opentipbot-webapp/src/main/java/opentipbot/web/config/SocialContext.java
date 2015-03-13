package opentipbot.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;
import opentipbot.persistence.repository.OpenTipBotUserRepository;
import opentipbot.service.BitcoinService;
import opentipbot.service.OpenTipBotUserService;
import opentipbot.web.security.SecurityContext;
import opentipbot.web.security.OpenTipBotConnectionSignUp;
import opentipbot.web.security.OpenTipBotSignInAdapter;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Spring Social Configuration.
 * @author Gilles Cadignan
 */
@Configuration
@EnableSocial
public class SocialContext implements SocialConfigurer {

	@Inject
	private DataSource dataSource;

    @Inject
    private OpenTipBotUserService opentipbotUserService;

    @Inject
    private BitcoinService bitcoinService;

    @Inject
    private OpenTipBotUserRepository opentipbotUserRepository;

	//
	// SocialConfigurer implementation methods
	//

	@Override
	public void addConnectionFactories(ConnectionFactoryConfigurer cfConfig, Environment env) {
		cfConfig.addConnectionFactory(new TwitterConnectionFactory(env.getProperty("opentipbot.webapp.twitter.appKey"), env.getProperty("opentipbot.webapp.twitter.appSecret")));
	}


	/**
	 * Singleton data access object providing access to connections across all users.
	 */
	@Override
	public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
		JdbcUsersConnectionRepository repository = new JdbcUsersConnectionRepository(dataSource, connectionFactoryLocator, Encryptors.noOpText());
		repository.setConnectionSignUp(new OpenTipBotConnectionSignUp(opentipbotUserService, bitcoinService));
		return repository;
	}

	public UserIdSource getUserIdSource() {
		return new UserIdSource() {
			@Override
			public String getUserId() {
				return SecurityContext.getCurrentUser().getId().toString();
			}
		};
	}

	@Bean
	@Scope(value="request", proxyMode= ScopedProxyMode.INTERFACES)
	public Twitter twitter(ConnectionRepository repository) {
		Connection<Twitter> connection = repository.findPrimaryConnection(Twitter.class);
		return connection != null ? connection.getApi() : null;
	}

	@Bean
	public ProviderSignInController providerSignInController(ConnectionFactoryLocator connectionFactoryLocator, UsersConnectionRepository usersConnectionRepository) {
		return new ProviderSignInController(connectionFactoryLocator, usersConnectionRepository, new OpenTipBotSignInAdapter(opentipbotUserRepository));
	}


}
