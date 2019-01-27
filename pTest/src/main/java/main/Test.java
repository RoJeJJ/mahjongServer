package main;

import com.buding.common.util.IOUtil;
import com.buding.mj.constants.MJConstants;
import com.google.common.collect.Maps;
import main.baidu.jsonPojo.Location;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by huangbp on 2017/12/6.
 * desc:
 */
public class Test {
    private static final String defaultPath = "/home/game/data/";
    private static final int dayNum = 10;

    public static void clearOldPlaybackFile() throws Exception {
        DateTime currentDay = DateTime.now();
        for (int i = 0; i < dayNum; i++) {
            String datePath = currentDay.minusDays(i + 7).toString("yyyy/MM/dd");
            IOUtil.deletefile(defaultPath + datePath);
        }
    }

    public static void main(String[] args) throws Exception {
        byte v1 = (byte)1;
        byte v2 = (byte)2;
        byte v3= (byte)4;
        List<Byte> cards = new ArrayList<Byte>();
        cards.add(v1);
        cards.add(v2);
        cards.add(v2);
        System.out.println("位置" + cards.indexOf(v3));
        Collections.swap(cards,0,cards.indexOf(v2));
        System.out.println("剩余的牌" );
        for (Byte card : cards) {
            System.out.println(card);
        }
    }


}