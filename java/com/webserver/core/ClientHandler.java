package com.webserver.core;

import com.webserver.http.EmptyRequestException;
import com.webserver.http.HttpContext;
import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;
import com.webserver.servlet.*;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理与某个客户端的HTTP交互
 * 由于HTTP要求客户端与服务端的交互采取一问一答,因此当前处理流程分为三步:
 * 1:解析请求(读取客户端发送过来的HTTP请求内容)
 * 2:处理请求
 * 3:响应客户端(发送一个HTTP响应给客户端)
 */
public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            //1解析请求
            HttpRequest request = new HttpRequest(socket);
            HttpResponse response = new HttpResponse(socket);

            //2处理请求
            //通过request获取抽象路径
            String path = request.getRequestURI();
            HttpServlet servlet = ServerContext.getServlet(path);
            //首先判断该请求是否为一些特殊的值,用于判定是否为处理业务
            if (servlet != null) {
                servlet.service(request, response);
            } else {
                //根据抽象路径去webapps下找到对应的资源
                File file = new File("./webapps" + path);
                //检查该资源是否真实存在
                if (file.exists() && file.isFile()) {
                    System.out.println("该资源已找到!");
                    //响应该资源
                    response.setEntity(file);
                } else {
                    System.out.println("该资源不存在!");
                    //响应404
                    File notFoundPage = new File("./webapps/root/404.html");
                    response.setStatusCode(404);
                    response.setStatusReason("NotFound");
                    response.setEntity(notFoundPage);
                }
            }

            response.putHeader("Server", "WebServer");

            //3响应客户端
            response.flush();


        } catch (EmptyRequestException e) {
            //单独捕获空请求异常，但是不需要做任何处理，这个异常抛出仅为了忽略处理操作
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //响应客户端后断开连接
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
