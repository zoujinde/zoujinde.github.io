package com.quiz.web.view;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebUtil;

@WebFilter(urlPatterns = "/*")
public class UserFilter implements Filter {

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest r1, ServletResponse r2, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) r1;
        HttpServletResponse resp = (HttpServletResponse) r2;
        String reqId = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals(WebUtil.REQ_ID)) {
                    // reqId like : user_id#user_type#user_name#host#port
                    reqId = LogUtil.decrypt(c.getValue());
                    String[] array = reqId.split("#");
                    if (array.length >= 5) {
                        req.setAttribute(WebUtil.REQ_ARRAY, array);
                        req.setAttribute(WebUtil.REQ_USER, array[2]);
                    }
                    break;
                }
            }
        }
        String uri = req.getRequestURI();
        LogUtil.log("UserFilter", uri + " = " + reqId);
        // Check URI and user
        if (uri.endsWith("/sign-in.jsp") ||
            uri.endsWith("/sign-up.jsp") ||
            uri.endsWith("/sign_form.jsp") ||
            uri.endsWith("/favicon.ico") ||
            uri.endsWith(".png") ||
            uri.endsWith(".jpg") ||
            uri.endsWith("/user")) {
            chain.doFilter(r1, r2);
        } else if (req.getAttribute(WebUtil.REQ_USER) != null){
            chain.doFilter(r1, r2);
        } else {
            resp.sendRedirect("sign-in.jsp");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

}
