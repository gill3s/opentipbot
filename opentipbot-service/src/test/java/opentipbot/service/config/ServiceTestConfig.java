package opentipbot.service.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import opentipbot.persistence.config.PersistenceContext;

/**
 * @author Gilles Cadignan
 */
@Configuration
@ComponentScan(basePackages = {
        "opentipbot.service"
})
@Import({PersistenceContext.class,  ServiceSocialConfig.class})
@PropertySource("classpath:application.properties")
public class ServiceTestConfig {
}
