package com.hujiang.juice.rest.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BaseController {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected HttpServletRequest getRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        return request;
    }

    protected HttpServletResponse getResponse() {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        return response;
    }

    protected HttpSession getSession() {
        HttpServletRequest request = getRequest();

        HttpSession session = request.getSession();

        return session;
    }

    protected String getReqHost() {
        HttpServletRequest request = getRequest();

        String fh = request.getHeader("x-forwarded-for");
        return fh == null ? request.getRemoteHost() : fh;
    }

    protected String getReqAgent() {
        HttpServletRequest request = getRequest();

        return request.getHeader("User-Agent");
    }

    protected String getReqUrl() {
        HttpServletRequest request = getRequest();

        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestURI.substring(contextPath.length());

        return path;
    }

    protected Cookie[] getCookies() {
        HttpServletRequest request = getRequest();

        return request.getCookies();
    }

    protected Object getSessionAttribute(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        HttpSession session = getSession();

        if (null != session) {
            return session.getAttribute(key);
        }

        return null;
    }

    protected void setSessionAttribute(String key, Object value) {
        if (StringUtils.isBlank(key)) {
            return;
        }

        HttpSession session = getSession();

        if (null != session) {
            session.setAttribute(key, value);
        }

    }

    protected void setSessionMaxInactiveInterval(int max) {
        HttpSession session = getSession();

        session.setMaxInactiveInterval(max);
    }

    protected String getCookie(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        Cookie[] cookies = getCookies();

        if ( cookies == null || cookies.length==0){
            return null;
        }

        for (Cookie cookie : cookies) {
            if (key.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    protected void addCookie(HttpServletResponse response, String key, String value, int expiry) {
        if (StringUtils.isBlank(key) || null == response) {
            return;
        }

        Cookie cookie = new Cookie(key, value);

        cookie.setPath("/");
        cookie.setMaxAge(expiry);

        response.addCookie(cookie);
    }

    /**
     * 仅当前会话中有效，关闭浏览器删除Cookie
     *
     * @param response
     * @param key
     * @param value
     */
    protected void addCookie(HttpServletResponse response, String key, String value) {

        addCookie(response, key, value, -1);
    }

    protected void deleteCookie(HttpServletResponse response, String key) {
        addCookie(response, key, null, 0);
    }

}
