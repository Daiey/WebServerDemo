package com.webserver.servlet;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

/**
 * 所有Servlet的超类
 */
public abstract class HttpServlet {
    public abstract void service(HttpRequest request, HttpResponse response);
}
