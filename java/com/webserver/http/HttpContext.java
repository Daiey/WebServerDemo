package com.webserver.http;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该类维护所有HTTP协议中固定不变的内容
 */
public class HttpContext {
    //保存所有Context-Type头的值与资源后缀的对应关系
    private static Map<String,String> mineMapping = new HashMap<>();
    //初始化所有静态资源
    static {
        initMineMapping();


    }

    private static void initMineMapping(){
        try{
            SAXReader reader = new SAXReader();
            Document doc = reader.read("./config/web.xml");
            Element root =doc.getRootElement();
            String name = root.getName();
            System.out.println(name);

            List<Element> list = root.elements("mime-mapping");

            int i = 1;
            for (Element empEle : list){
                String key = empEle.elementText("extension");
                String value = empEle.elementText("mime-type");
                mineMapping.put(key,value);
            }

        }catch (Exception e){

        }
    }

    /**
     * 根据资源后缀名获取对应的Content-Type的值
     * @param ext       资源的后缀名
     * @return          Content-Type头的值
     */
    public static String getMineType(String ext){
        return mineMapping.get(ext);
    }
}
