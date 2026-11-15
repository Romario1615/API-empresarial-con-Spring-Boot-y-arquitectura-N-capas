package ec.edu.espe.soportetickets.config;

import ec.edu.espe.soportetickets.interceptor.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final RequestLoggingInterceptor loggingInterceptor;
    private final ApiKeyInterceptor apiKeyInterceptor;

    public WebConfig(RequestLoggingInterceptor loggingInterceptor, ApiKeyInterceptor apiKeyInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor).addPathPatterns("/api/**").order(0);
        registry.addInterceptor(apiKeyInterceptor).addPathPatterns("/api/**").order(1);
    }

    @Bean
    public FilterRegistrationBean<ResponseTimeFilter> responseTimeFilter() {
        FilterRegistrationBean<ResponseTimeFilter> registration = new FilterRegistrationBean<>(new ResponseTimeFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}

