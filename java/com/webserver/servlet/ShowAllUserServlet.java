package com.webserver.servlet;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;
import com.webserver.vo.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成包含user.dat文件中所有用户信息的动态页面
 */
public class ShowAllUserServlet extends HttpServlet {
    public void service(HttpRequest request, HttpResponse response) {
        System.out.println("ShowAllUserSerlvet:开始处理用户列表页面...");
        //先将user.dat文件中所有记录读取出来
        List<User> list = new ArrayList<>();//保存user.dat文件中所有用户信息
        try (
                RandomAccessFile raf = new RandomAccessFile("user.dat", "r");
        ) {
            for (int i = 0; i < raf.length() / 100; i++) {
                //读取用户名
                byte[] data = new byte[32];
                raf.read(data);
                String username = new String(data, "UTF-8").trim();
                //读取密码
                raf.read(data);
                String password = new String(data, "UTF-8").trim();
                //读取昵称
                raf.read(data);
                String nickname = new String(data, "UTF-8").trim();
                //读取年龄
                int age = raf.readInt();//EOFException  end of file
                User user = new User(username, password, nickname, age);
                list.add(user);
                System.out.println(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
            使用thymeleaf将数据与页面整合生成动态页面
         */
        /*
            Context用于保存所有需要在页面上展示的动态数据
         */
        Context context = new Context();
        context.setVariable("users", list);

        //初始化Thymeleaf模板引擎
        //1初始化模板解释器,用来告知模板引擎有关模板页面的相关信息
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setTemplateMode("html");//模板类型,指定为html格式
        resolver.setCharacterEncoding("UTF-8");//模板使用的字符集(我们的页面都是UTF-8编码的)
        //2实例化模板引擎
        TemplateEngine te = new TemplateEngine();
        te.setTemplateResolver(resolver);//设置模板解释器,使其了解模板相关信息

        /*
            String process(String path,Context ctx)
            模板引擎生成动态页面的方法
            参数1:模板页面的路径
            参数2:需要在页面上显示的数据(数据应当都放在这个Context中)
            返回值为生成好的html代码
         */
        String html = te.process("./webapps/myweb/userlist.html", context);


        try {
            byte[] data = html.getBytes("UTF-8");
            //将生成的页面内容设置到response中
            response.setData(data);
            response.putHeader("Content-Type", "text/html");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("ShowAllUserSerlvet:处理用户列表页面完毕!");
    }
}
