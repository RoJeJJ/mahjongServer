package com.buding.common.util;

import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by huangbp on 2017/12/2.
 * desc:
 */
public class MapDistance {

    private static final String BaiduMapAk = "QCFCduEDxfDUv3W3CyHhv7qctWUFhfhz";

    /**
     * @param long1 经度1
     * @param lat1  维度1
     * @param long2 经度2
     * @param lat2  纬度2
     * @return
     */
    public static double getDistance(double long1, double lat1, double long2,
                                     double lat2) {
        double a, b, R;
        R = 6378137; // 地球半径
        lat1 = lat1 * Math.PI / 180.0;
        lat2 = lat2 * Math.PI / 180.0;
        a = lat1 - lat2;
        b = (long1 - long2) * Math.PI / 180.0;
        double d;
        double sa2, sb2;
        sa2 = Math.sin(a / 2.0);
        sb2 = Math.sin(b / 2.0);
        d = 2 * R * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));
        return d;
    }

    public static String getAddrByBaidu(String location) {

        String[] template = location.split(",");
        if (template.length < 2) {
            return null;
        }
        String lng = template[0], lat = template[1];
        String result = null;
        try {
            URL url = new URL("http://api.map.baidu.com/geocoder/v2/?ak=" + BaiduMapAk + "&callback=renderReverse&location=" + lat + "," + lng + "&output=json&pois=1");
            HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
            ucon.connect();

            InputStream in = ucon.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String str = reader.readLine();

            str = str.substring(str.indexOf("(") + 1, str.length() - 1);
            JSONObject data = JSONObject.fromObject(str);
            if ((int) data.get("status") == 0) {
                result = data.getJSONObject("result").getString("formatted_address");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String[] getLocationArray(String location) {
        String [] result = null;
        if (StringUtils.isNotBlank(location)){
            result = location.split(",");
            if (result.length < 2){
                result = null;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        // 海大大厦经纬度 113.357659,23.016958
        // 万达广场经纬度 113.357268,23.013187
//        String address = getAddrByBaidu("113.357659", "23.016958");
//        System.out.println(address);
//        System.out.println(getDistance(113.357659, 23.016958, 113.357268, 23.013187));
    }
}