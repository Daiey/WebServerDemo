package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象
 * 该类的每一个实例用于表示HTTP的一个请求
 * 每个请求由三个部分构成：请求行，消息头，消息正文
 */
public class HttpRequest {
    String line = null;
    //请求行相关信息
    //请求行中的请求方式
    private String method = "";
    //抽象路径部分
    private String uri = "";
    //协议版本
    private String protocol = "";

    //用来保存uri中的请求部分(?左侧内容)
    private String requestURI;
    //用来保存uri中的参数部分(?右侧内容)
    private String queryString;
    //用来保存每一组参数的。key:存参数名 value:存参数值
    private Map<String, String> parameters = new HashMap<>();


    //消息头相关信息
    Map<String, String> headers = new HashMap<>();

    //消息正文相关信息

    //和连接相关的信息

    private Socket socket;
    private InputStream in;

    public HttpRequest(Socket socket) throws IOException, EmptyRequestException {
        this.socket = socket;
        //解析请求行
        parseRequest();
        //解析消息头
        parseHeaders();
        //解析消息正文
        parseContent();


    }

    /**
     * 解析请求行
     *
     * @throws IOException
     */
    private void parseRequest() throws IOException, EmptyRequestException {
        System.out.println("HttpRequest:解析请求行...");

        in = socket.getInputStream();
        line = readLine();
        //如果返回请求行返回是空串，说明本次为空请求！
        if (line.isEmpty()) {
            throw new EmptyRequestException();
        }
        System.out.println("请求行：" + line);
        String[] data = line.split("\\s");
        method = data[0];
        uri = data[1];
        protocol = data[2];

        //进一步解析uri
        parseUri(uri);

        System.out.println("method:" + method);
        System.out.println("uri:" + uri);
        System.out.println("protocol:" + protocol);

        System.out.println("HttpReuqest:解析请求行完毕！");

    }

    /**
     * 进一步对uri进行拆分解析，因为uri可能包含参数。
     */
    private void parseUri(String uri) {
        System.out.println("HttpRequest:进一步解析uri...");
        /*
            对抽象路径解码（解决传递中文问题，将%XX的内容还愿对应的文字）
         */
        try {
            /*
                URKDecoder提供的静态方法：
                static String decode(String str,String csn)
                将给定的字符串（第一个参数）中%XX这样的内容按照给定的字符集（第二个参数）还原为
                对应的文字并替换原有的%XX部分。将替换后的字符串返回
                例如：
                uri：
                username=%E8%8C%83%E4%BC%A0%E5%A5%87&password=123456&nickname=chuanqi&age=22
                经过下面代码处理后，decode方法返回的字符为：
                username=范传奇&password=123456&nickname=chuanqi&age=22
             */
            uri = URLDecoder.decode(uri, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        /*
            uri可能存在两种情况:含有参数，不含有参数
            含有参数(uri中包含"?"):
            首先按照"?"将uri拆分成两部分,第一部分赋值给requestURI这个属性,第二部分赋值给
            queryString这个属性
            然后再讲queryString(uri中的参数部分)进行进一步拆分,来得到每一组参数.
            首先将queryString按照"&"拆分出每一组参数,然后按照每组参数再按照"="拆分为参数名与参数值
            之后将参数名作为key,参数值作为value保存到parameters这个Map中保存即可。

            不含参数(uri中不包含"?")
            则直接将uri的值赋值给requestURI即可
         */
        //根据?将uri分成两个部分
//        int index = uri.indexOf("?");
//        if (index == -1) {
//            requestURI = uri;
//        } else {
//            requestURI = uri.substring(0, index);
//            if (index + 1 != uri.length()) {
//                queryString = uri.substring(index + 1);
//                String[] data = queryString.split("&");
//                for (String str1 : data) {
//                    String[] str = str1.split("=");
//                    if (str.length == 1) {
//                        str = Arrays.copyOf(str, str.length + 1);
//                        str[1] = null;
//                    }
//                    parameters.put(str[0], str[1]);
//                }
//            }
//        }
        //*********************************************************************
        if (uri.contains("?")) {
            String[] data = uri.split("\\?");
            requestURI = data[0];
            if (data.length > 1) {//确定是否有参数部分
                queryString = data[1];
                //拆分出每一组参数
                data = queryString.split("&");
                //遍历每组参数再进行拆分
                for (String para : data) {
                    String[] arr = para.split("=");
                    if (arr.length > 1) {
                        parameters.put(arr[0], arr[1]);
                    } else {
                        parameters.put(arr[0], null);
                    }
                }
            }
        } else {
            requestURI = uri;
        }
        System.out.println("requestURI:" + requestURI);
        System.out.println("queryString:" + queryString);
        System.out.println("parameters:" + parameters);
        System.out.println("HttpRequest:进一步解析uri完毕！");
    }

    /**
     * 解析消息头
     *
     * @throws IOException
     */
    private void parseHeaders() throws IOException {
        System.out.println("HttpRequest:解析消息头...");
        String line = null;
        while (true) {
            line = readLine();
            if (line.isEmpty()) {//如果是空字符串就停止
                break;
            }
            System.out.println("消息头：" + line);
            //将消息头按照“： ”拆分，将名字和值以key，value形式存储在header中
            String[] data1 = line.split(":\\s");
            headers.put(data1[0], data1[1]);
        }
        System.out.println(headers);
        System.out.println("HttpReuqest:解析消息头完毕！");
    }

    /**
     * 解析消息正文
     */
    private void parseContent() {
        System.out.println("HttpRequest:解析消息正文...");
        System.out.println("HttpReuqest:解析消息正文完毕！");
    }

    private String readLine() throws IOException {
         /*
            同一个socket对象，无论调用多少次getInputStream方法
            获取到的输出流都是同一个
         */
        InputStream in = socket.getInputStream();
        int d;
        char cur = 'a', pre = 'a';//cur表示本次读取的字符，pre表示上次读到的字符
        StringBuilder builder = new StringBuilder();
        while ((d = in.read()) != -1) {
            cur = (char) d;
            //如果上次读取的是回车符并且本次读到的是换行符就停止
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

    public String getParameters(String key) {
        return parameters.get(key);
    }
}
