package com.webserver.core;


import com.webserver.http.HttpRequest;

import java.io.File;
import java.io.IOException;

import com.webserver.http.EmptyRequestException;
import com.webserver.http.HttpResponse;
import com.webserver.servlet.RegServlet;
import com.webserver.servlet.ShowAllUserServlet;

import java.io.OutputStream;
import java.net.Socket;

/**
 * 每个客户端连接后都会起订一个线程来完成与该客户端的交互。
 * 交互过程遵循HTTP协议的一问一答要求，分三步进行处理。
 * 1、解析请求
 * 2、处理请求
 * 3、响应客户端
 */
public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            String line;
            OutputStream out;
            //1  解析请求
            HttpRequest request = new HttpRequest(socket);
            HttpResponse response = new HttpResponse(socket);

            //2  处理请求
            //2.1
            String path = request.getRequestURI();
            System.out.println("抽象路径：" + path);
            //首先根据请求路径判断是否为请求业务
            if ("/myweb/showAllUser".equals(path)) {
                ShowAllUserServlet servlet = new ShowAllUserServlet();
                servlet.service(request,response);
            } else if ("/myweb/regUser".equals(path)) {
                //本次请求为请求注册业务
                System.out.println("处理注册！！");
                RegServlet regServlet = new RegServlet();
                regServlet.service(request, response);

            } else {

                File file = new File("./webapps" + path);

                if (file.exists() && file.isFile()) {
                    System.out.println("资源已找到");
                    response.setEntity(file);
                } else {
                    System.out.println("资源不存在！");
                    File notFoundFile = new File("./webapps/root/404.html");
                    response.setStatusCode(404);
                    response.setStatusReason("Not Found");
                    response.setEntity(notFoundFile);
                }
            }
            //告知浏览器服务端是谁
            response.putHeaders("Server", "WebServer");

            //3  响应客户端

            response.flush();
            System.out.println("响应发送完毕");


        }
        //单独捕获空请求异常，不需要做任何处理，目的仅仅是忽略处理工作
        catch (EmptyRequestException e) {

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //完成交互完毕后与客户端断开连接
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
