package com.webserver.servlet;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * 处理用户注册业务
 */
public class RegServlet {

    public void service(HttpRequest request, HttpResponse response) {
        System.out.println("RegServlet：开始处理注册...");
        //1 获取用户在注册的页面上输入的注册信息
        String username = request.getParameters("username");
        String password = request.getParameters("password");
        String nickname = request.getParameters("nickname");
        String ageStr = request.getParameters("age");
        System.out.println(username + "," + password + "," + nickname + "," + ageStr);
        /*
            添加对用户输入的信息的验证工作
            要求:
            如果用户名,密码,昵称和年龄为null,或者年龄输入的不是一个数字(正则表达式验证)
            则直接设置response响应一个注册失败的提示页面.
            该页面放在webapps/myweb目录下,名字为reg_input_error.html
            页面中提示一行字:注册信息输入有误,请重新注册
            然后加一个超链接会发哦注册页面.

            只有验证通过了,才进行下面的注册操作
         */
        String regex = "[0-9]+";
        if (username != null && password != null && nickname != null && ageStr != null && ageStr.matches(regex)) {
            int age = Integer.parseInt(ageStr);

            //2 将该用户信息写入user.dat文件保存
            try (
                    RandomAccessFile raf = new RandomAccessFile("user.dat", "rw");
            ) {
                /*
                    先读取user.dat文件中现有的所有数据，将没跳记录的用户名读取出来并与当前注册用户的
                    用户名比对，如果已经存在，则直接响应界面：have_user.html
                    该页面居中显示一行字：该用户已存在，请重新注册

                    否则财智星下面原有的注册操作
                 */
                for (int i = 0; i < raf.length()/100; i++) {
                    raf.seek(i * 100);
                    byte[] data = new byte[32];
                    raf.read(data);
                    String name = new String(data, "utf-8").trim();
                    if (name.equals(username)) {
                        File file = new File("./webapps/myweb/have_user.html");
                        response.setEntity(file);
                        return;
                    }
                }

                //先将指针移动到文件夹末尾，以便追加记录
                raf.seek(raf.length());

                //写用户名
                byte[] data = setData(username);
                raf.write(data);

                //写密码
                data = setData(password);
                raf.write(data);

                //写昵称
                data = setData(nickname);
                raf.write(data);

                //写年龄
                raf.writeInt(age);
                System.out.println("注册完毕！");


                //将注册成功页面设置到response的正文上
                File file = new File("./webapps/myweb/reg_success.html");
                response.setEntity(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File file = new File("./webapps/myweb/reg_input_error.html");
            response.setEntity(file);
        }

        //3 响应用户注册结果页面(成功或者失败页面)
        System.out.println("RegServlet：处理注册完毕！");

    }

    //信息写入
    private byte[] setData(String str) throws IOException {
        byte[] data = new byte[0];
        data = str.getBytes("utf-8");
        data = Arrays.copyOf(data, 32);
        return data;
    }


}
