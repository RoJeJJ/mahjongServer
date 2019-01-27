package com.buding.common.schedule.playbackFile;

import org.slf4j.Logger;

/**
 * Created by huangbp on 2017/12/6.
 * desc: 过期回放数据文件清理
 */
public class PlaybackFileTask {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
    private static final String defaultPath = "/home/game/data/";
    private static final int dayNum = 10;

    public void clearOldPlaybackFile() throws Exception {
//        DateTime currentDay = DateTime.now();
//        for(int i = 0; i < dayNum; i++) {
//            String datePath = currentDay.minusDays(i+7).toString("yyyy/MM/dd");
//            IOUtil.deletefile(defaultPath + datePath);
//        }
        logger.info("开始执行清除过期回放数据任务");
    }
}