package com.buding.test;

import com.buding.common.util.IOUtil;
import com.google.common.collect.Maps;
import net.sf.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by huangbp on 2017/11/27.
 * desc:
 */
public class FileTest {
    public static Map<Long,String> test = Maps.newHashMap();
    public static int count = 0;
    public static void main(String[] age) {
        long nanoTime = System.nanoTime();
        long currentTimeMillis = System.currentTimeMillis();
        long endTime = System.nanoTime();
//        System.out.println(endTime - nanoTime);
//        System.out.println(nanoTime);
//        System.out.println(endTime);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                    for (int i = 0; i < 100; i++) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                long time = System.nanoTime();
                                if (time < 0){
                                    System.out.println("负数");
                                }
                                if (test.containsKey(time)){
                                    count ++;
                                    System.out.println("重复");
                                    System.out.println(time);
                                }else {
                                    test.put(time,"1");
                                }
                            }
                        }).start();
                    }
                    System.out.println(test.size());
                }

            }
        }).start();


        try {
//            //当前日期
//            Date date = new Date();
//格式化并转换String类型
//            String path = "C:/Users/Administrator/Desktop/" + new SimpleDateFormat("yyyy/MM/dd").format(date);
////创建文件夹
//            File f = new File(path);
//            if (!f.exists())
//                f.mkdirs();
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("test", "test");
//            IOUtil.writeFileContent(path  + "/" + id + ".json", jsonObject.toString());
//            byte data[] = IOUtil.tryGetFileData(path);
//            String json = new String(data, "UTF-8");
//            JSONObject test = JSONObject.fromObject(json);
//            String testStr = test.get("test").toString();
//            System.out.println(testStr);
//            for (File file : IOUtil.listFiles(path)) {
//                String fileTamplate = IOUtil.getFileResourceAsString(file, "utf-8");
//                System.out.println(JSONObject.fromObject(fileTamplate).get("test").toString());
//            }
        } catch (Exception e) {

        }


    }
}