package com.webserver.http;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 响应对象，当前类的每一个实例用于表示服务端发送给客户端的一个标准的
 * HTTP响应内容
 * 每个响应由三部分构成：状态行、响应头、响应正文
 */
public class HttpResponse {
    private Socket socket;
    private HttpRequest request;
    String line;
    OutputStream out;
    //状态执行相关信息
    private int statusCode = 200;//状态代码，默认值200
    private String statusReason = "ok";//状态描述，默认值ok
    //响应头相关信息
    private Map<String, String> headers = new HashMap<>();
    //响应正文相关信息
    private File entity;
    private byte[] contentData;

    public HttpResponse(Socket socket) throws IOException, EmptyRequestException {
        this.socket = socket;

    }


    /**
     * 将当前响应对象内容以标准的HTTP响应格式发送给客户端
     */
    public void flush() throws IOException {
        /*
                发送一个标准的http响应，将刚才写好的页面：
                ./webapps/myweb/index.html

                响应格式
                HTTP/1.1 200 OK(CRLF)
                Content-Type: text/html(CRLF)
                Content-Length: 2546(CRLF)(CRLF)
                1011101010101010101......
             */
        //状态行相关信息
        sendStatusLine();

        //响应头相关信息
        sendHeaders();

        //响应正文相关信息
        sendContent();

    }

    private void sendStatusLine() throws IOException {
        /**
         * 状态行相关信息
         */
        System.out.println("HttpResponse:开始发送状态行...");
        out = socket.getOutputStream();
        line = "HTTP/1.1 " + statusCode + " " + statusReason;
        out.write(line.getBytes("ISO8859-1"));
        out.write(13);//发送一个回车符
        out.write(10);//发送一个换行符

        System.out.println("HttpResponse:状态行发送完毕！");


    }

    private void sendHeaders() throws IOException {
        /**
         * 响应头相关信息
         */
        Set<Map.Entry<String, String>> entrySet = headers.entrySet();
        for (Map.Entry<String, String> e : entrySet) {
            String name = e.getKey();
            String value = e.getValue();
            String line = name + ": " + value;
            out.write(line.getBytes("ISO8859-1"));
            out.write(13);//发送一个回车符
            out.write(10);//发送一个换行符
            System.out.println("响应头: " + line);
        }

        //单独发送CRLF表示发送完毕
        out.write(13);//发送一个回车符
        out.write(10);//发送一个换行符

        System.out.println("HttpResponse:响应头发送完毕！");

    }

    private void sendContent() {
        /**
         * 响应正文相关信息
         */
        System.out.println("HttpResponse:开始发送正文信息...");
        if (contentData != null) {
            try {
                OutputStream out = socket.getOutputStream();
                out.write(contentData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (entity != null) {
            try (
                    FileInputStream fis = new FileInputStream(entity);
            ) {
                int len;
                byte[] data = new byte[1024 * 10];
                while ((len = fis.read(data)) != -1) {
                    out.write(data, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("HttpResponse:正文信息发送完毕！");

    }

    public File getEntity() {
        return entity;
    }

    /**
     * 设置响应正文的文件，设置的同时会根据该文件添加两个响应头：Contrnr-Type和Content-Length
     *
     * @param entity
     */
    public void setEntity(File entity) {
        this.entity = entity;
        //获取文件名
        String fileName = entity.getName();
        //获取后缀
        int index = fileName.lastIndexOf(".");
        String ext = fileName.substring(index + 1);
        String mine = HttpContext.getMineType(ext);
        putHeaders("Content-Type", mine);
        putHeaders("Content-Length", entity.length() + "");
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    /**
     * 添加一个响应头
     *
     * @param name
     * @param value
     */
    public void putHeaders(String name, String value) {

        this.headers.put(name, value);
    }

    public byte[] getContentData() {
        return contentData;
    }

    /**
     * 将一组字节作为响应正文，设置的同时会自动包含Content-Length
     *
     * @param contentData
     */
    public void setContentData(byte[] contentData) {
        this.contentData = contentData;
        putHeaders("Content-Length", contentData.length + "");
    }
}
