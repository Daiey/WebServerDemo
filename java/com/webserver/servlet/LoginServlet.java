package com.webserver.servlet;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 处理用户登录
 */
public class LoginServlet extends HttpServlet {
    public void service(HttpRequest request, HttpResponse response) {
        System.out.println("LoginServlet:开始处理登录...");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null) {
            response.setEntity(new File("./webapps/myweb/login_fail.html"));
            return;
        }

        try (
                RandomAccessFile raf = new RandomAccessFile("user.dat", "r");
        ) {
            byte[] data = new byte[32];
            for (int i = 0; i < raf.length() / 100; i++) {
                raf.seek(i * 100);
                raf.read(data);
                String name = new String(data, "UTF-8").trim();

                if (name.equals(username)) {
                    raf.read(data);
                    String pwd = new String(data, "UTF-8").trim();

                    if (pwd.equals(password)) {
                        response.setEntity(new File("./webapps/myweb/login_success.html"));
                        return;
                    }
                    break;
                }
            }
            response.setEntity(new File("./webapps/myweb/login_fail.html"));

        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("LoginServlet:处理登录完毕!");
    }
}
