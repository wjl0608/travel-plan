package com.fawnyr.travelplanbackend.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置，支持第三方过滤器
 */
@Configuration
public class FilterConfigure {
    /**
     * 请求体封装
     * @return
     */
    @Bean
    public FilterRegistrationBean<RequestWrapperFilter> filterRegistrationBean(){
        FilterRegistrationBean<RequestWrapperFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RequestWrapperFilter());
        bean.addUrlPatterns("/*");
        return bean;
    }
}
