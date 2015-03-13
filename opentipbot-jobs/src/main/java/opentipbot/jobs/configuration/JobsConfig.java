package opentipbot.jobs.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import opentipbot.jobs.tasks.OpenTipBotHandleNewTweetsTask;
import opentipbot.jobs.tasks.OpenTipBotProcessCommandsTask;
import opentipbot.persistence.config.PersistenceContext;
import opentipbot.service.OpenTipBotService;
import opentipbot.service.config.ServiceSocialConfig;

/**
 * @author Gilles CADIGNAN
 * Job DSL Configuration
 */
@Configuration
@EnableScheduling
@ComponentScan(basePackages = {
        "opentipbot.service"
})
@Import({PersistenceContext.class, ServiceSocialConfig.class})
@PropertySource("classpath:application.properties")
public class JobsConfig {

    @Autowired
    OpenTipBotService opentipbotService;

    @Autowired
    private Environment env;

    @Bean
    public OpenTipBotHandleNewTweetsTask opentipbotTask(){
        return new OpenTipBotHandleNewTweetsTask(opentipbotService);
    }

    @Bean
    public OpenTipBotProcessCommandsTask opentipbotProcessCommandsTask(){
        return new OpenTipBotProcessCommandsTask(opentipbotService);
    }

}