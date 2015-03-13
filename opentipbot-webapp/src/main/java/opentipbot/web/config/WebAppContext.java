package opentipbot.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles2.TilesConfigurer;
import org.springframework.web.servlet.view.tiles2.TilesView;
import opentipbot.persistence.repository.OpenTipBotUserRepository;
import opentipbot.web.security.UserInterceptor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Petri Kainulainen
 */
@Configuration
@ComponentScan(basePackages = {
        "opentipbot.web.controller"
})
@EnableWebMvc
public class WebAppContext extends WebMvcConfigurerAdapter {

    @Autowired
    OpenTipBotUserRepository opentipbotUserRepository;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public SimpleMappingExceptionResolver exceptionResolver() {
        SimpleMappingExceptionResolver exceptionResolver = new SimpleMappingExceptionResolver();

        Properties exceptionMappings = new Properties();

        exceptionMappings.put("java.lang.Exception", "home");
        exceptionMappings.put("java.lang.RuntimeException", "home");

        exceptionResolver.setExceptionMappings(exceptionMappings);

        Properties statusCodes = new Properties();

        statusCodes.put("error/404", "404");
        statusCodes.put("error/error", "500");

        exceptionResolver.setStatusCodes(statusCodes);

        return exceptionResolver;
    }


    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInterceptor(usersConnectionRepository, opentipbotUserRepository));
    }

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/signout");
    }
    @Bean
    public ViewResolver contentNegotiatingViewResolver(
            ContentNegotiationManager manager) {

        List< ViewResolver > resolvers = new ArrayList<ViewResolver>();

        UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
        viewResolver.setViewClass(TilesView.class);
        resolvers.add(viewResolver);

        JsonViewResolver r2 = new JsonViewResolver();
        resolvers.add(r2);

        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
        resolver.setViewResolvers(resolvers);
        resolver.setContentNegotiationManager(manager);
        return resolver;

    }

    @Bean
    public TilesConfigurer tilesConfigurer() {
        TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions(new String[]{
                "/WEB-INF/tiles.xml"
        });
        tilesConfigurer.setCheckRefresh(true);
        return tilesConfigurer;
    }

    private @Inject
    UsersConnectionRepository usersConnectionRepository;
}
