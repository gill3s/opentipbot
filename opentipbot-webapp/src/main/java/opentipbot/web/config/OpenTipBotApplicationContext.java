package opentipbot.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import opentipbot.persistence.config.PersistenceContext;
import opentipbot.service.config.ServiceSocialConfig;

/**
 * @author Gilles Cadignan
 *
 * Opentipbot application conctext declaring message source ressource bundle
 * and Property Placeholder
 */
@Configuration
@ComponentScan(basePackages = {
        "opentipbot.service"
})
@Import({WebAppContext.class, PersistenceContext.class,  SocialContext.class, ServiceSocialConfig.class})
@PropertySource("classpath:application.properties")
public class OpenTipBotApplicationContext {

    private static final String MESSAGE_SOURCE_BASE_NAME = "i18n/messages";

    @Autowired
    private Environment env;

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

        messageSource.setBasename(MESSAGE_SOURCE_BASE_NAME);
        messageSource.setUseCodeAsDefaultMessage(true);

        return messageSource;
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer propertyPlaceHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


}
