package com.buding.hall.network.cmd;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import com.buding.common.util.IOUtil;
import com.buding.hall.config.StatusConf;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall;
import packet.game.Hall.PlayerScoreModel;
import packet.game.Hall.RoomResultModel;
import packet.game.Hall.RecorderModel;
import packet.game.Hall.RoomResultRequest;
import packet.game.Hall.RoomResultResponse;
import packet.mj.MJ;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.db.model.UserRoomResult;
import com.buding.db.model.UserRoomResultDetail;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.vip.dao.UserRoomDao;

/**
 * @author tiny qq_381360993
 * @Description:
 */
@Component
public class UserRoomResultCmd extends HallCmd {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @Autowired
    HallPushHelper pushHelper;

    @Autowired
    UserRoomDao userRoomDao;

    @Override
    public void execute(CmdData data) throws Exception {
        PacketBase packet = data.packet;
        RoomResultRequest ur = RoomResultRequest.parseFrom(packet.getData());
        long roomId = ur.getRoomId();
        long userId = data.session.userId;
        if (roomId == 0) { //查看总战绩
            List<UserRoomResult> list = userRoomDao.getUserRoomResultList(userId);
            RoomResultResponse.Builder rb = RoomResultResponse.newBuilder();
            for (UserRoomResult model : list) {
                RoomResultModel.Builder bb = RoomResultModel.newBuilder();
                bb.setPlayerTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(model.getEndTime()));
                bb.setRoomCode(model.getRoomName());
                bb.setRoomName(model.getRoomName());
                bb.setRoomId(model.getRoomId());
                JSONArray ja = JSONArray.fromObject(model.getDetail());
                for (int i = 0; i < ja.size(); i++) {
                    JSONObject obj = ja.getJSONObject(i);
                    PlayerScoreModel.Builder score = PlayerScoreModel.newBuilder();
                    score.setPlayerId(obj.getLong("playerId"));
                    score.setPlayerName(obj.getString("playerName"));
                    score.setScore(obj.getInt("score"));
                    bb.addPlayerScore(score);
                }
                rb.addList(bb);
            }

            pushHelper.pushRoomResultResponse(data.session, rb.build());
        } else { //查看某个房间的
            List<UserRoomResultDetail> list = userRoomDao.getUserRoomResultDetailList(roomId);
            RoomResultResponse.Builder rb = RoomResultResponse.newBuilder();
            for (UserRoomResultDetail model : list) {
                RoomResultModel.Builder bb = RoomResultModel.newBuilder();
                bb.setPlayerTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(model.getEndTime()));
                bb.setRoomCode(model.getRoomName());
                bb.setRoomName(model.getRoomName());
                bb.setRoomId(model.getRoomId());
                if (StringUtils.isNotEmpty(model.getPlayBackPath())) {
                    RecorderModel.Builder rm = getRecorderModel(model.getPlayBackPath());
                    if (rm != null) {
                        bb.setRecorder(rm);
                    }
                }
                JSONArray ja = JSONArray.fromObject(model.getDetail());
                for (int i = 0; i < ja.size(); i++) {
                    JSONObject obj = ja.getJSONObject(i);
                    PlayerScoreModel.Builder score = PlayerScoreModel.newBuilder();
                    score.setPlayerId(obj.getLong("playerId"));
                    score.setPlayerName(obj.getString("playerName"));
                    score.setScore(obj.getInt("score"));
                    bb.addPlayerScore(score);
                }
                rb.addList(bb);
            }
            pushHelper.pushRoomResultResponse(data.session, rb.build());
        }
    }

    private RecorderModel.Builder getRecorderModel(String playBackPath) {
        RecorderModel.Builder result = RecorderModel.newBuilder();
        String json = null;
        try {
            json = new String(IOUtil.getFileData("/home/game/data/" + playBackPath));
        } catch (Exception e) {
            return null;
        }
        JSONObject jsonObject = JSONObject.fromObject(json);
        setPlayerInfoModel(result, jsonObject);
        setPlayerInitCardModel(result, jsonObject);
        setPlayerActionModel(result, jsonObject);
        setResultInfo(result, jsonObject);
        result.setBaoCard(jsonObject.getInt("baoCard"));
        result.setIsContinueBanker(jsonObject.getBoolean("isContinueBanker"));
        result.setCurrentHuPlayerIndex(jsonObject.getInt("currentHuPlayerIndex"));
        result.setBankerPos(jsonObject.getInt("bankerPos"));
        result.setBankerUserId(jsonObject.getLong("bankerUserId"));
        result.setDeskId(jsonObject.getString("deskId"));
        result.setDice1(jsonObject.getInt("dice1"));
        result.setDice2(jsonObject.getInt("dice2"));
        return result;
    }

    private void setResultInfo(RecorderModel.Builder result, JSONObject jsonObject) {
        Hall.PlayerHuModel.Builder items = Hall.PlayerHuModel.newBuilder();
        JSONObject info = jsonObject.getJSONObject("playerResultDetail");
        if (info != null) {
            items.setPosition(info.getInt("position_"));
            items.setCard(info.getInt("card_"));
            items.setBao(info.getInt("bao_"));
            items.setResultType(info.getInt("resultType_"));
            items.setPaoPosition(info.getInt("paoPosition_"));
            items.setSkipHuSettle(info.getBoolean("skipHuSettle_"));
            items.setWinType(info.getInt("winType_"));
        }

		JSONArray resultDetail = info.getJSONArray("detail_");

		for (Object o : resultDetail) {
			Hall.PlayerSettleModel.Builder settle = Hall.PlayerSettleModel.newBuilder();
			JSONObject template = JSONObject.fromObject(o);

            JSONArray cardArray = template.getJSONArray("handcard_");
            for (Object card : cardArray) {
                settle.addHandcard((int)card);
            }

            JSONArray fanDetail = template.getJSONArray("fanDetail_");
            for (Object detail : fanDetail) {
                settle.addFanDetail(detail.toString());
            }
            int fanType = template.getInt("fanType_");
            int fanNum = template.getInt("fanNum_");
			int coin = template.getInt("coin_");
			int score = template.getInt("score_");
            settle.setPosition(template.getInt("position_"));
			settle.setFanType(fanType);
			settle.setFanNum(fanNum);
			settle.setCoin(coin);
			settle.setScore(score);
			items.addDetail(settle);
//		}
        }
        result.setResultInfo(items);
    }

    private void setPlayerActionModel(RecorderModel.Builder result, JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("actionList");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject info = JSONObject.fromObject(jsonArray.get(i));
            Hall.PlayerActionModel.Builder pam = Hall.PlayerActionModel.newBuilder();
            if (info.getInt("card1") == 0 && info.getInt("card2") == 0) {
                continue;
            }
            pam.setCard1(info.getInt("card1"));
            pam.setCard2(info.getInt("card2"));
            pam.setCode(info.getString("code"));
            pam.setPosition(info.getInt("position"));

            result.addActionList(pam);
        }
    }

    private void setPlayerInitCardModel(RecorderModel.Builder result, JSONObject jsonObject) {
        Map<String, Object> initCards = (Map<String, Object>) jsonObject.get("playerInitCards");
        for (Map.Entry<String, Object> entry : initCards.entrySet()) {
            Hall.PlayerInitCardModel.Builder picm = Hall.PlayerInitCardModel.newBuilder();
            int position = Integer.parseInt(entry.getKey());
            JSONArray pCards = (JSONArray) entry.getValue();
            for (Object obj : pCards) {
                JSONObject pCard = JSONObject.fromObject(obj);
                picm.addCards(pCard.getInt("code"));
            }
            picm.setPosition(position);
            result.addPlayerInitCards(picm);
        }
    }

    private void setPlayerInfoModel(RecorderModel.Builder result, JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("players");
        for (int i = 0; i < jsonArray.size(); i++) {
            if (StringUtils.equals(jsonArray.get(i).toString(), "null")) {
                continue;
            }
            JSONObject info = JSONObject.fromObject(jsonArray.get(i));
            Hall.PlayerInfoModel.Builder pim = Hall.PlayerInfoModel.newBuilder();
            pim.setPlayerId(info.getLong("playerId"));
            pim.setPosition(info.getInt("position"));
            pim.setName(info.getString("name"));
            pim.setHeadImg(info.getString("headImg"));
            result.addPlayerInfos(pim);
        }
    }


    @Override
    public PacketType getKey() {
        return PacketType.RoomResultRequest;
    }

}
