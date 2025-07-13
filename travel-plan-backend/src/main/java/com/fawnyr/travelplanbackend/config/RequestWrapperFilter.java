package com.fawnyr.travelplanbackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

// 1.实现Filter接口，此处也可以选择继承HttpFilter
public class RequestWrapperFilter implements Filter {
    // 2. 重写或实现doFilter方法
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 3.此处判断是为了缩小影响范围，本身CustomRequestWrapper只是针对HttpServletRequest，不进行判断可能会影响其他类型的请求
        if (request instanceof HttpServletRequest) {
            // 4.将默认的HttpServletRequest转换为自定义的CustomRequestWrapper
            CustomRequestWrapper requestWrapper = new CustomRequestWrapper((HttpServletRequest) request);
            // 5.将转换后的request传递至调用链中
            chain.doFilter(requestWrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
