package com.webserver.core;

import com.webserver.servlet.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务端会重用的内容都放在这里
 */
public class ServerContext {
    private static Map<String, HttpServlet> servletMapping = new HashMap<>();

    static {
        initServletMapping();
    }

    private static void initServletMapping() {
        servletMapping.put("/myweb/regUser", new RegServlet());
        servletMapping.put("/myweb/loginUser", new LoginServlet());
        servletMapping.put("/myweb/updatePwd", new UpdatePwdServlet());
        servletMapping.put("/myweb/showAllUser", new ShowAllUserServlet());


    }

    public static HttpServlet getServlet(String path) {
        return servletMapping.get(path);
    }
}
