package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象
 * 该类的每一个实例用于表示浏览器发送过来的一个HTTP请求
 * HTTP协议要求一个请求由三部分构成:
 * 请求行,消息头,消息正文
 */
public class HttpRequest {
    //请求行相关信息
    private String method;//请求行中的请求方式
    private String uri;//请求行中的抽象路径
    private String protocol;//请求行中的协议版本

    private String requestURI;//抽象路径中的请求部分,uri中"?"左侧的内容
    private String queryString;//抽象路径中的参数部分,uri中"?"右侧的内容
    private Map<String, String> parameters = new HashMap<>();//保存每一组参数

    //消息头相关信息
    private Map<String, String> headers = new HashMap<>();

    //消息正文相关信息

    private Socket socket;

    public HttpRequest(Socket socket) throws EmptyRequestException {
        System.out.println("HttpRequest:开始解析请求...");
        this.socket = socket;
        //1解析请求行
        parseRequestLine();
        //2解析消息头
        parseHeaders();
        //3解析消息正文
        parseContent();
        System.out.println("HttpRequest:请求解析完毕!");
    }

    private void parseRequestLine() throws EmptyRequestException {
        System.out.println("HttpRequest:开始解析请求行...");
        try {
            String line = readLine();
            if (line.isEmpty()) {//如果是空字符串，说明是空请求!!!
                throw new EmptyRequestException();
            }
            System.out.println("请求行:" + line);
            String[] data = line.split("\\s");
            method = data[0];
            uri = data[1];
            protocol = data[2];
            parseUri();//进一步解析uri,因为uri很可能含有参数
            System.out.println("method:" + method);
            System.out.println("uri:" + uri);
            System.out.println("protocol:" + protocol);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("HttpRequest:请求行解析完毕");
    }

    private void parseUri() {
        System.out.println("HttpRequest:进一步解析uri...");
        /*
            对于不含有参数的uri而言则不需要做过多的处理,只需要将uri的值直接赋值给
            requestURI即可.
            因为requestURI专门用来保存uri的请求部分.不含有参数而定uri就是请求部分.

            对于含有参数的uri,我们要进一步拆分.
            首先按照"?"将uri拆分为两部分:请求部分和参数部分
            然后将请求部分赋值给属性requestURI
            将参数部分赋值给属性queryString

            只有再对queryString进一步拆分出每一组参数:
            首先按照"&"拆分出每个参数,然后每个参数再按照"="拆分为参数名和参数值
            将参数名作为key,参数值作为value保存到属性parameters这个Map中即可.
         */
        //先对uri解码,将%XX的16进制所表示的信息解码还原对应的文字
        /*
            uri: /myweb/regUser?username=%E8%8C%83%E4%BC%A0%E5%A5%87&.....
            uri = URLDecoder.decode(uri,"UTF-8");
            uri: /myweb/regUser?username=范传奇&.....
         */
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        if (uri.contains("?")) {//判断uri中是否含有参数
            String[] arr = uri.split("\\?");
            requestURI = arr[0];
            if (arr.length > 1) {
                queryString = arr[1];
                //进一步拆分参数
                arr = queryString.split("&");
                for (String para : arr) {//name=value
                    String[] paras = para.split("=");
                    if (paras.length > 1) {
                        parameters.put(paras[0], paras[1]);
                    } else {
                        parameters.put(paras[0], null);
                    }
                }
            }
        } else {
            requestURI = uri;
        }


        System.out.println("requestURI:" + requestURI);
        System.out.println("queryString:" + queryString);
        System.out.println("parameters:" + parameters);
        System.out.println("HttpRequest:进一步解析uri完毕!");
    }

    private void parseHeaders() {
        System.out.println("HttpRequest:开始解析消息头...");
        try {
            while (true) {
                String line = readLine();
                if (line.isEmpty()) {
                    break;
                }
                String[] arr = line.split(":\\s");
                headers.put(arr[0], arr[1]);
                System.out.println("消息头:" + line);
            }
            System.out.println("headers:" + headers);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("HttpRequest:消息头解析完毕!");
    }

    private void parseContent() {
        System.out.println("HttpRequest:开始解析消息正文...");
        System.out.println("HttpRequest:消息正文解析完毕!");
    }

    private String readLine() throws IOException {
        /*
            socket相同时,无论调用多少次getInputStream()方法,获取的输入流始终是同一个
         */
        InputStream in = socket.getInputStream();
        StringBuilder builder = new StringBuilder();
        int d;
        char cur = 'a';//本次读取到的字符
        char pre = 'a';//上次读取到的字符
        while ((d = in.read()) != -1) {
            cur = (char) d;
            if (pre == 13 && cur == 10) {
                break;
            }
            builder.append(cur);
            pre = cur;
        }
        return builder.toString().trim();
    }


    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    /**
     * 根据参数名获取参数值
     *
     * @param name
     * @return
     */
    public String getParameter(String name) {
        return parameters.get(name);
    }
}






