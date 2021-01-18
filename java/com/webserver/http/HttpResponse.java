package com.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 响应对象
 * 当前类的每一个实例表示给客户端发送的一个HTTP响应
 * 一个响应应当包含三部分:状态行,响应头,响应正文
 */
public class HttpResponse {
    //状态行相关信息
    private int statusCode = 200;//状态代码,默认值为200
    private String statusReason = "OK";//状态描述,默认值为OK

    //响应头相关信息
    private Map<String, String> headers = new HashMap<>();

    //响应正文相关信息
    private File entity;//响应正文对应的实体文件
    private byte[] data;//正文的字节数据

    private Socket socket;

    public HttpResponse(Socket socket) {
        this.socket = socket;
    }

    /**
     * 将当前响应对象内容以标准的HTTP响应格式发送给客户端
     */
    public void flush() {
        System.out.println("开始发送响应");
        //发送一个响应
        //1发送状态行
        sendStatusLine();
        //2发送响应头
        sendHeaders();
        //3发送响应正文
        sendContent();
        System.out.println("响应发送完毕");
    }

    private void sendStatusLine() {
        System.out.println("开始发送状态行");
        try {
            String line = "HTTP/1.1" + " " + statusCode + " " + statusReason;
            System.out.println("状态行:" + line);
            println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("状态行发送完毕");
    }

    private void sendHeaders() {
        System.out.println("开始发送响应头");
        try {
//            String line = "Content-Type: text/html";
//            println(line);
//            line = "Content-Length: "+entity.length();
//            println(line);
            //遍历headers这个Map，将所有的响应头发送给客户端
            Set<Map.Entry<String, String>> entrySet = headers.entrySet();
            for (Map.Entry<String, String> e : entrySet) {
                String key = e.getKey();//响应头的名字
                String value = e.getValue();//响应头的值
                String line = key + ": " + value;
                System.out.println("响应头:" + line);
                println(line);
            }

            //单独发送CRLF表示响应头发送完毕!
            println("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("响应头发送完毕");
    }

    private void sendContent() {
        System.out.println("开始发送响应正文");
        if (data != null) {
            try {
                OutputStream out = socket.getOutputStream();
                out.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (entity != null) {
            try (
                    FileInputStream fis = new FileInputStream(entity);
            ) {
                OutputStream out = socket.getOutputStream();
                int len;
                byte[] data = new byte[1024 * 10];
                while ((len = fis.read(data)) != -1) {
                    out.write(data, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("响应正文发送完毕");
    }

    private void println(String line) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write(line.getBytes("ISO8859-1"));
        out.write(13);//发送一个回车符
        out.write(10);//发送一个换行符
    }

    /**
     * 向当前响应对象中添加一个响应头
     *
     * @param name  响应头的名字
     * @param value 响应头的值
     */
    public void putHeader(String name, String value) {
        headers.put(name, value);
    }

    public File getEntity() {
        return entity;
    }

    public void setEntity(File entity) {
        this.entity = entity;
        //根据资源文件名获取后缀名   test.1.1.css
        String ext = entity.getName().substring(entity.getName().lastIndexOf(".") + 1);
        String type = HttpContext.getMimeType(ext);
        //根据正文文件设置响应头
        putHeader("Content-Type", type);
        putHeader("Content-Length", entity.length() + "");
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

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        putHeader("Content-Length", data.length + "");
    }
}
