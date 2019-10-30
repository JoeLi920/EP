package com.oracle.ebp_16.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.oracle.ebp_16.domain.User;
import com.oracle.ebp_16.util.Constant;


public class UserLoginFilter implements Filter {

    public void init(FilterConfig filterConfig) {}
    
    public void doFilter(ServletRequest request, ServletResponse response, 
            FilterChain chain) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpSession session = req.getSession();
        
        String requestURI = req.getRequestURI();
        //System.out.println("请求requestURI为:     "+requestURI);
        //以下尝试截取用户拟请求的目标路径
        String uri = requestURI.substring(requestURI.indexOf(Constant.LOGIN_PREFIX_MY));
        //用户既不是请求登录，也不是请求注册时，进行拦截
        if (uri.indexOf(Constant.LOGIN_KEYWORD_MY) == -1 && uri.indexOf(Constant.REGISTER_KEYWORD_MY)==-1) {
            User user = (User)session.getAttribute("user");
            if (user == null) {
                session.setAttribute(Constant.ATTR_NEXTURL, uri);
//                String loginUri = req.getContextPath() + Constant.LOGIN_PREFIX + Constant.LOGIN_PAGE;
                String loginUri = req.getContextPath() + "/index";
                ((HttpServletResponse)response).sendRedirect(loginUri);
                return;               
            } 
        }
        chain.doFilter(request, response);
    }

    public void destroy() {}
}