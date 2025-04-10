package config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<NoCacheFilterConfig> noCacheFilter() {
        FilterRegistrationBean<NoCacheFilterConfig> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new NoCacheFilterConfig());
        registrationBean.addUrlPatterns("/*"); // Protected URLs
        return registrationBean;
    }
}
