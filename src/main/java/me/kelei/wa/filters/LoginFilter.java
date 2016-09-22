package me.kelei.wa.filters;

import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 登录验证
 * Created by kelei on 2016/9/22.
 */
public class LoginFilter implements Filter {

    private String regPath = "/css/*|/js/*|/fonts/*|/login/*";

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        HttpSession session = request.getSession();
        Pattern pattern = Pattern.compile(regPath);
        Object user = session.getAttribute("waUser");
        Cookie[] cookies = request.getCookies();
        String rememberme = null;
        if(!pattern.matcher(request.getRequestURI()).find()){
            if(cookies != null && cookies.length > 0){
                for(Cookie cookie : cookies)
                    if("rememberme".equals(cookie.getName()))
                        rememberme = cookie.getValue();
            }

            if(user == null || !"on".equals(rememberme)){
                request.getRequestDispatcher("/page/login.html").forward(request, response);
                return;
            }
        }


        filterChain.doFilter(request, response);
    }

    public void destroy() {

    }
}
