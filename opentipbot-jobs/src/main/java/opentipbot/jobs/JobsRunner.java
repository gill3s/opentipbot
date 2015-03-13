package opentipbot.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import opentipbot.jobs.configuration.JobsConfig;

/**
 * @author Gilles Cadignan
 * Job runner class. Main method to be executed in the packaged jar.
 */
public class JobsRunner {
    private static final Logger log = LoggerFactory.getLogger(JobsRunner.class.getSimpleName());

    public static void main(String[] args) {
        log.info("Starting jobs application context");
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(JobsConfig.class);
    }
}
