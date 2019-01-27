package com.buding.mj.states;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.text.Position;

import com.buding.api.desk.MJDesk;
import com.buding.game.GameData;
import com.buding.game.GameState;
import com.buding.game.events.DispatchEvent;
import com.buding.game.events.GameLogicEvent;
import com.buding.game.events.NetEvent;
import com.buding.game.events.PlatformEvent;
import com.buding.game.events.PlayerEvent;
import com.buding.mj.constants.MJConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import packet.mj.MJ;

public abstract class MJStateCommon extends GameState<MJDesk<byte[]>> {

    @Override
    public void onPlayer(PlayerEvent event) {
        // TODO Auto-generated method stub

        this.logger.info("onplayer is called playerid is " + event.info.playerId + " position " + event.info.position + " eventid " + event.eventID + " is robot " + event.info.robot);

        switch (event.eventID) {

            case GameLogicEvent.Player_Sit:
            case GameLogicEvent.Player_Agree: {
                int position = event.info.position;
                if (position < MJConstants.MJPlayerCount) {
                    this.mGameData.mPlayers[position] = event.info;
                } else {
                    this.logger.error("player postion is bigger than 3");
                }
            }
            break;

            case GameLogicEvent.Player_MaiPiao: {
                String playerId = String.valueOf(event.gameInfo.getPlayerId());
                this.mGameData.playerGameInfoMap.put(playerId, event.gameInfo);
            }
            break;

            case GameLogicEvent.Player_SeeBestCard: {
                mCardLogic.GoodPlayerSeeCard(event.info.getPlayerID());
            }
            break;

            case GameLogicEvent.Player_ChangeBestCard: {
                mCardLogic.GoodPlayerChangeCard(event.card,event.info,this.mDesk);
            }
            break;

            case GameLogicEvent.Player_HangUp: {
                int position = event.info.position;
                this.mGameData.mPlayerAction[position].autoOperation = 1;
                handlePlayerHangup(position);
                this.mDesk.onPlayerHangup(position);
            }
            break;

            case GameLogicEvent.Player_Cancel_Hangup: {
                int position = event.info.position;
                this.mGameData.mPlayerAction[position].autoOperation = 0;
                this.mDesk.onPlayerCancelHangup(position);
            }
            break;

            case GameLogicEvent.Player_Reconnect: {
                int position = event.info.position;
                logger.info("handle reconnect for {}, state: {}", position, this.getClass().getName());
                this.handleReconnectFor(position);

            }
            break;
            case GameLogicEvent.Player_Exit: {
                logger.info("player {}  exit", event.info.position);
                this.mGameData.mPlayers[event.info.position] = null;
            }
            break;
            case GameLogicEvent.Player_Away: {
                logger.info("ignore player {}  away", event.info.position);
            }
            break;
            case GameLogicEvent.Player_ComeBack: {
                logger.info("player {} comback", event.info.position);
                int position = event.info.position;
                this.handleReconnectFor(position);
            }
            break;
            default:
                break;
        }

    }

    @Override
    public void onPlatform(PlatformEvent event) {
        switch (event.eventID) {

            case GameLogicEvent.Game_Dismiss: {

                this.mGameData.dismissing = true;
                // //1秒后状态跳转
                this.mGameTimer.KillDeskTimer();

                DispatchEvent e = new DispatchEvent();
                e.eventID = MJConstants.MJStateFinish;
                this.mDispatcher.StateDispatch(e);
                break;
            }
            case GameLogicEvent.Game_Pause: {
                this.mGameData.pause = true;
                break;
            }
            case GameLogicEvent.Game_Resume: {
                this.mGameData.pause = false;
                break;
            }
            default: {

            }
            break;
        }
    }

    @Override
    public void setGamingDate(String json) {
        mCardLogic.handleSetGamingData(mCardDealer, this.mGameData, this.mDesk, json);
    }

    @Override
    public void onNet(NetEvent event) {

    }

    /*
     *
     *
     *
     *
     * 处理玩家状态变更
     */
    public abstract void handlePlayerStatusChange(int position);

    /*
     * 处理玩家重连
     */
    public abstract void handleReconnectFor(int position);

    public abstract void handlePlayerHangup(int position);

    public void dumpGameData() {
        try {
            String data = new GsonBuilder().setPrettyPrinting().create().toJson(this.mGameData);
            File file = new File("/home/game/game.json");
            if (file.getParentFile() != null && file.getParentFile().exists() == false) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream fout = new FileOutputStream(file.getAbsolutePath());
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(fout, "UTF-8"));
            writer.println(data);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            logger.error("act=dumpGameDataError;deskId=" + mDesk.getDeskID(), e);
        }
    }

    public void relaodGameData() {
        try {
            FileInputStream fin = new FileInputStream("/home/game/game.json");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                int size = 1024;
                byte buff[] = new byte[size];
                while ((size = fin.read(buff)) != -1) {
                    out.write(buff, 0, size);
                }
            } finally {
                fin.close();
            }

            String json = new String(out.toByteArray(), "UTF8");
            GameData data = new Gson().fromJson(json, GameData.class);
            this.mGameData = data;

            logger.info("reload GameData OK, json:{}", json);
        } catch (Exception e) {
            logger.error("act=dumpGameDataError;deskId=" + mDesk.getDeskID() + ";", e);
        }
    }
}
