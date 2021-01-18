package com.webserver.servlet;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * 用于处理用户注册业务
 */
public class RegServlet extends HttpServlet {
    public void service(HttpRequest request, HttpResponse response) {
        System.out.println("RegServlet:开始处理用户注册...");
        //1通过request获取用户在页面上表单中输入的信息
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nickname = request.getParameter("nickname");
        String ageStr = request.getParameter("age");
        System.out.println(username + "," + password + "," + nickname + "," + ageStr);
        /*
            验证数据,如果上述四项存在null,或者年龄的字符串表示的不是一个整数时,直接
            响应一个错误页面:reg_info_error.html.上面居中显示一行字:注册失败,输入信息有误!
            此时不应当再执行下面的注册操作了!
         */
        if (username == null || password == null || nickname == null || ageStr == null ||
                !ageStr.matches("[0-9]+")) {
            File file = new File("./webapps/myweb/reg_info_error.html");
            response.setEntity(file);
            return;
        }
        int age = Integer.parseInt(ageStr);
        /*
            2将信息写入文件user.dat中
            每个用户的信息都占用100字节,其中用户名,密码,昵称为字符串,个占用32字节.
            年龄为int值占用4字节.
         */
        try (
                RandomAccessFile raf = new RandomAccessFile("user.dat", "rw");
        ) {
            /*
                判定是否为重复用户.
                先读取user.dat文件中现有的所有用户名.如果与当前注册的用户名一致则直接响应
                页面:have_user.html 提示该用户名已存在,请重新注册.
                否则才将该用户信息写入文件user.dat中完成注册.
             */
            for (int i = 0; i < raf.length() / 100; i++) {
                raf.seek(i * 100);
                byte[] data = new byte[32];
                raf.read(data);
                String name = new String(data, "UTF-8").trim();
                if (name.equals(username)) {
                    File file = new File("./webapps/myweb/have_user.html");
                    response.setEntity(file);
                    return;
                }
            }


            raf.seek(raf.length());//先将指针移动到文件末尾
            byte[] data = username.getBytes("UTF-8");
            data = Arrays.copyOf(data, 32);
            raf.write(data);
            data = password.getBytes("UTF-8");
            data = Arrays.copyOf(data, 32);
            raf.write(data);
            data = nickname.getBytes("UTF-8");
            data = Arrays.copyOf(data, 32);
            raf.write(data);
            raf.writeInt(age);

            //3响应客户端注册结果页面
            //注册成功,设置response响应注册成功页面
            File file = new File("./webapps/myweb/reg_success.html");
            response.setEntity(file);

        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("RegServlet:处理用户注册完毕!");

    }
}




