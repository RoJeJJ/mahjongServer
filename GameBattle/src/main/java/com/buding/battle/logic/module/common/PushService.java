package com.buding.battle.logic.module.common;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.Hall.LogoutSyn;
import packet.game.MsgGame.DeskDestorySyn;
import packet.game.MsgGame.DissmissVoteSyn;
import packet.game.MsgGame.EnrollResponse;
import packet.game.MsgGame.GameChatMsgSyn;
import packet.game.MsgGame.GameStartSyn;
import packet.game.MsgGame.HangupSyn;
import packet.game.MsgGame.PlayerComebackSyn;
import packet.game.MsgGame.PlayerExitSyn;
import packet.game.MsgGame.PlayerGamingSyn;
import packet.game.MsgGame.PlayerOfflineSyn;
import packet.game.MsgGame.PlayerReconnectSyn;
import packet.game.MsgGame.PlayerSitSyn;
import packet.game.MsgGame.ReadySyn;
import packet.game.MsgGame.VipRoomListSyn;
import packet.mj.MJ;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User.AuthResponse;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.db.model.User;
import com.buding.hall.packet.player.PlayerInfoSyn;
import com.google.protobuf.ByteString;

@Component
public class PushService implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public static PushService instance;

    @Autowired
    BattleSessionManager sessionManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    /**
     * 推送房间信息
     *
     * @param playerId
     * @param roomId
     * @param errMsg
     */
    public void pushRoomInfo(int playerId, String roomId, String errMsg) {
        // RoomInfo room = new RoomInfo();
        // room.roomId = roomId;
        // room.msg = errMsg;
        //
        // sessionManager.writeTextWebSocketFrame(playerId, "PB_Room_Info_Syn",
        // room);
    }

    /**
     * 推送赛场信息
     *
     * @param playerId
     * @param matchId
     * @param errMsg
     */
    public void pushMatchInfo(int playerId, String matchId, String errMsg) {
        // MatchInfo match = new MatchInfo();
        // match.matchId = matchId;
        // match.msg = errMsg;
        //
        // sessionManager.writeTextWebSocketFrame(playerId, "PB_Match_Info_Syn",
        // match);
    }

    /**
     * 推送桌子信息
     *
     * @param playerId
     * @param deskId
     * @param errMsg
     */
    public void pushDeskInfo(int playerId, String deskId, int playerCount, String errMsg) {
        // DeskInfo desk = new DeskInfo();
        // desk.deskId = deskId+"";
        // desk.msg = errMsg;
        // desk.playerCount = playerCount;
        //
        // sessionManager.writeTextWebSocketFrame(playerId, "PB_Desk_Info_Syn",
        // desk);
    }

    public void pushKickoutSyn(int playerId, String msg) {
        pushPBMsg(playerId, PacketType.KickOutSyn, null);
    }

    public void pushQuickStartRsp(int playerId, int code, String msg) {
        // BaseRsp rsp = new BaseRsp();
        // rsp.result = code;
        // rsp.msg = msg;
        // sessionManager.writeTextWebSocketFrame(playerId,
        // "PB_QuickStart_Response", rsp);
    }

    /**
     * 推送玩家进入信息
     *
     * @param playerId
     * @param toPushPlayerId
     */
    public void pushPlayerSitSyn(int playerId, int toPushPlayerId) {
        logger.info("act=pushPlayerSitSyn;playerId=" + playerId + ";receiverId=" + toPushPlayerId);
        BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
        if (session != null) {
            User user = session.user;
            PlayerInfo p = session.player;
            PlayerSitSyn.Builder pb = PlayerSitSyn.newBuilder();
            pb.setCoin(p.coin);
            pb.setScore(p.score);
            pb.setSex(user.getGender());
            pb.setNickName(user.getNickname());
            pb.setPlayerId(p.playerId);
            pb.setPosition(p.position);
            boolean ready = session.getStatus() == PlayerStatus.READY || session.getStatus() == PlayerStatus.GAMING;
            pb.setState(ready ? 1 : 0); // 未准备
            pb.setOnline(session.onlineStatus == OnlineStatus.ONLINE ? 1 : 0);
            pb.setAway(session.awayStatus == AwayStatus.AWAY ? 1 : 0);
            pb.setHeadImg(user.getHeadImg());
            pushPBMsg(toPushPlayerId, PacketType.PlayerSitSyn, pb.build().toByteString());
        }
    }

    public void pushPlayerExitSyn(int position, int leavePlayerId, int toPushPlayerId) {
        logger.info("act=pushPlayerLeaveSyn;position=" + position + ";receiverId=" + toPushPlayerId);
        BattleSession session = ServiceRepo.sessionManager.getIoSession(toPushPlayerId);
        if (session != null) {
            PlayerExitSyn.Builder pb = PlayerExitSyn.newBuilder();
            // pb.setPosition(position);
            pb.setPlayerId(leavePlayerId);
            pushPBMsg(session, PacketType.PlayerExitSyn, pb.build().toByteString());
        }
    }

    public void pushPlayerAwaySyn(int position, int leavePlayerId, int toPushPlayerId) {
        logger.info("act=pushPlayerAwaySyn;position=" + position + ";receiverId=" + toPushPlayerId);
        BattleSession session = ServiceRepo.sessionManager.getIoSession(toPushPlayerId);
        if (session != null) {
            PlayerExitSyn.Builder pb = PlayerExitSyn.newBuilder();
            // pb.setPosition(position);
            pb.setPlayerId(leavePlayerId);
            pushPBMsg(session, PacketType.PlayerAwaySyn, pb.build().toByteString());
        }
    }

    public void pushDeskDestory(int playerId, String deskId) {
        logger.info("act=pushDeskDestory;playerId={};deskId={}", playerId, deskId);
        DeskDestorySyn.Builder syn = DeskDestorySyn.newBuilder();
        syn.setDeskId(deskId);
        pushPBMsg(playerId, PacketType.DeskDestorySyn, syn.build().toByteString());
    }

    public void pushPlayerOfflineSyn(int position, int offerlinePlayerId, int toPushPlayerId) {
        logger.info("act=pushPlayerOfflineSyn;position=" + position + ";receiverId=" + toPushPlayerId);
        BattleSession session = ServiceRepo.sessionManager.getIoSession(toPushPlayerId);
        if (session != null) {
            PlayerOfflineSyn.Builder pb = PlayerOfflineSyn.newBuilder();
            // pb.setPosition(position);
            pb.setPlayerId(offerlinePlayerId);
            pushPBMsg(session, PacketType.PlayerOfflineSyn, pb.build().toByteString());
        }
    }

    public void pushGamePauseSyn(String deskId, Integer receiver) {
//		logger.info("act=pushGamePauseSyn;deskId={};receiverId={};", deskId, receiver);
//		BattleSession session = ServiceRepo.sessionManager.getIoSession(receiver);
//		if (session != null) {
//			pushPBMsg(session, PacketType.GamePauseSyn, null);
//		}
    }

    public void pushGameResumeSyn(String deskId, Integer receiver) {
//		logger.info("act=pushGameResumeSyn;deskId={};receiverId={};", deskId, receiver);
//		BattleSession session = ServiceRepo.sessionManager.getIoSession(receiver);
//		if (session != null) {
//			pushPBMsg(session, PacketType.GameResumeSyn, null);
//		}
    }

    public void pushGlobalErrorSyn(int playerId, String msg) {
        logger.info("act=pushGlobalErrorSyn;playerId=" + playerId + ";msg=" + msg);
//		GlobalErrorSyn.Builder gb = GlobalErrorSyn.newBuilder();
//		gb.setMsg(msg);
//		BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
//		pushPBMsg(session, PacketType.GlobalErrorSyn, gb.build().toByteString());
        pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
    }

    public void pushPlayerComebackSyn(int position, int combackPlayerId, int toPushPlayerId) {
        logger.info("act=pushPlayerComebackSyn;position=" + position + ";receiverId=" + toPushPlayerId);
        BattleSession session = ServiceRepo.sessionManager.getIoSession(toPushPlayerId);
        if (session != null) {
            PlayerComebackSyn.Builder pb = PlayerComebackSyn.newBuilder();
            // pb.setPosition(position);
            pb.setPlayerId(combackPlayerId);
            pushPBMsg(session, PacketType.PlayerComebackSyn, pb.build().toByteString());
        }
    }

    public void pushPlayerReconnectSyn(int position, int reconnectPlayerId, int toPushPlayerId) {
        logger.info("act=pushPlayerReconnectSyn;position=" + position + ";receiverId=" + toPushPlayerId);
        BattleSession session = ServiceRepo.sessionManager.getIoSession(toPushPlayerId);
        if (session != null) {
            PlayerReconnectSyn.Builder pb = PlayerReconnectSyn.newBuilder();
            // pb.setPosition(position);
            pb.setPlayerId(reconnectPlayerId);
            pushPBMsg(session, PacketType.PlayerReconnectSyn, pb.build().toByteString());
        }
    }

    /**
     * 推送玩家进入信息
     *
     * @param playerId
     * @param player
     */
    // public void pushPlayerEnterInfo(int playerId, PlayerVO player) {
    // // sessionManager.writeTextWebSocketFrame(playerId,
    // "PB_Desk_Player_Enter_Syn", player);
    // }

    /**
     * 推送玩家退出信息
     *
     * @param playerId
     * @param exitPlayerId
     */
    // public void pushPlayerExitMsg(int playerId, int exitPlayerId) {
    // // PlayerId player = new PlayerId();
    // // player.playerId = exitPlayerId;
    // //
    // // sessionManager.writeTextWebSocketFrame(playerId,
    // "PB_Desk_Player_Exit_Syn", player);
    // }

    /**
     * 推送游戏开始消息
     *
     * @param playerId
     * @param deskId
     */
    public void pushGameStartMsg(int playerId, String deskId) {
        GameStartSyn.Builder pb = GameStartSyn.newBuilder();
        pb.setDeskId(deskId);
        pushPBMsg(playerId, PacketType.GameStartSyn, pb.build().toByteString());
    }

    public void pushGameMsgSyn(int playerId, byte[] data) {
        // sessionManager.writeTextWebSocketFrame(playerId, "PB_Game_Msg_Syn",
        // syn);
    }

    /**
     * 推送游戏结束消息
     *
     * @param playerId
     * @param deskId
     */
    public void pushGameStopMsg(int playerId, String deskId) {
        // GameStartStopSyn startMsg = new GameStartStopSyn();
        // startMsg.deskID = deskId+"";
        // sessionManager.writeTextWebSocketFrame(playerId,
        // "PB_Desk_Game_Stop_Syn", startMsg);
    }

    /**
     * 推送报名响应信息
     *
     * @param playerId
     * @param result
     * @param msg
     */
    public void pushEnrollRsp(int playerId, boolean result, String msg) {
        logger.info("act=pushEnrollRsp;playerId=" + playerId + ";result=" + result + ";msg=" + msg);
        if (!result) {
            pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
            return;
        }
        EnrollResponse.Builder pb = EnrollResponse.newBuilder();
        pushPBMsg(playerId, PacketType.EnrollRequest, pb.build().toByteString());
    }

    public void pushPlayerGamingInfo(int playerId, String gameId, String matchId, String roomId, String deskId, int wanfa, int roomType, int totalQuan) {
        logger.info("act=pushPlayerGamingInfo;playerId={};gameId={};matchId={};roomId={};deskId={};", playerId, gameId, matchId, roomId, deskId);
        PlayerGamingSyn.Builder pb = PlayerGamingSyn.newBuilder();
        pb.setDeskId(deskId);
        pb.setRoomId(roomId);
        pb.setMatchId(matchId);
        pb.setGameId(gameId);
        pb.setWanfa(wanfa);
        pb.setRoomType(roomType);
        pb.setTotalQuan(totalQuan);
        pushPBMsg(playerId, PacketType.PlayerGamingSyn, pb.build().toByteString());
    }

    public void pushErrorMsg(BattleSession session, PacketType type, String msg) {
        PacketBase.Builder pb = PacketBase.newBuilder();
        pb.setCode(-1);
        pb.setPacketType(type);
        pb.setMsg(msg);
        sessionManager.write(session, pb.build().toByteArray());
    }

    public void pushErrorMsg(int playerId, PacketType type, String msg) {
        PacketBase.Builder pb = PacketBase.newBuilder();
        pb.setCode(-1);
        pb.setPacketType(type);
        pb.setMsg(msg);
        sessionManager.write(playerId, pb.build());
    }

    public void pushPBMsg(BattleSession session, PacketType type, ByteString data) {
        PacketBase.Builder pb = PacketBase.newBuilder();
        pb.setCode(0);
        pb.setPacketType(type);
        if (data != null) {
            pb.setData(data);
        }
        sessionManager.write(session, pb.build().toByteArray());
    }

    public void pushPBMsg(int playerId, PacketType type, ByteString data) {
        BattleSession session = sessionManager.getIoSession(playerId);
        if (session != null) {
            pushPBMsg(session, type, data);
        }
    }

    public void pushLogoutSyn(int playerId, String msg) {
        LogoutSyn.Builder syn = LogoutSyn.newBuilder();
        syn.setReason(msg);
        pushPBMsg(playerId, PacketType.LogoutSyn, syn.build().toByteString());
    }

    /**
     * 推送登录响应消息
     *
     * @param session
     * @param result
     * @param msg
     */
    public void pushLoginRsp(BattleSession session, boolean result, PlayerInfo player, String msg) {
        logger.info(String.format("act=pushLoginRsp;playerId={};result={}", new Object[]{session.userId, result}));
        if (result) {
            AuthResponse.Builder lb = AuthResponse.newBuilder();
            pushPBMsg(session, PacketType.AuthRequest, lb.build().toByteString());
            return;
        }

        pushErrorMsg(session, PacketType.AuthRequest, msg);
        return;
    }

    /**
     * 推送准备响应消息
     *
     * @param playerId
     * @param result
     * @param msg
     */
    public void pushReadyRsp(int playerId, boolean result, String msg) {
        // if(result) {
//         pushPBMsg(playerId, PacketType.READY, null);
        // return;
        // }
        // pushErrorMsg(playerId, PacketType.READY, msg);
    }

    /**
     * 推送玩家已准消息
     *
     * @param playerId
     * @param readyPosition
     * @param readyPlayerId
     */
    public void pushReadySyn(int playerId, int readyPosition, int readyPlayerId) {
        logger.info("act=pushReadySyn;playerId=" + playerId + ";readyPosition=" + readyPosition);
        ReadySyn.Builder pb = ReadySyn.newBuilder();
        // pb.setPosition(readyPosition);
        pb.setPlayerId(readyPlayerId);
        pb.setState(1); // 已准备
        pushPBMsg(playerId, PacketType.ReadySyn, pb.build().toByteString());
    }

    /**
     * 推送换桌响应信息
     *
     * @param playerId
     * @param result
     * @param msg
     */
    public void pushChangeDeskRsp(int playerId, boolean result, String msg) {
        if (result) {
            pushPBMsg(playerId, PacketType.ChangeDeskRequest, null);
            return;
        }
        pushErrorMsg(playerId, PacketType.ChangeDeskRequest, msg);
    }

    public void pushCreateVipRoomRsp(int playerId, boolean ok, String msg) {
        if (ok) {
//			pushPBMsg(playerId, PacketType.GlobalErrorSyn, null);
            return;
        }
        pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
    }

    public void pushDismissVote(int playerId, int position, boolean agree) {
        DissmissVoteSyn.Builder sb = DissmissVoteSyn.newBuilder();
        sb.setPosition(position);
        sb.setAgree(agree);
        pushPBMsg(playerId, PacketType.DissmissVoteSyn, sb.build().toByteString());
    }

    public void pushBack2HallRsp(int playerId) {
        // sessionManager.writeTextWebSocketFrame(playerId,
        // "PB_Back_To_Hall_Response", rsp);
    }

    public void pushUserAttrChange(int playerId, PlayerInfoSyn syn) {
        // sessionManager.writeTextWebSocketFrame(playerId,
        // "PB_Player_Info_Update", syn);
    }

    public void pushDeskPlayerKickoutRsp(int playerId, boolean result, String msg) {
        if (result) {
//			pushPBMsg(playerId, PacketType.KickPlayerRequest, null);
            return;
        }
        pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
    }

    public void pushDismissVipRoomResponse(int playerId, boolean result, String msg) {
        if (result) {
            pushPBMsg(playerId, PacketType.DismissVipRoomRequest, null);
            return;
        }
        pushErrorMsg(playerId, PacketType.GlobalMsgSyn, msg);
    }

    public void pushHangupSyn(int playerId, int position, int status) {
        logger.info("act=pushHangupSyn;playerId={};position={};status={};", playerId, position, status);
        HangupSyn.Builder gb = HangupSyn.newBuilder();
        gb.setPosition(position);
        gb.setStatus(status);
        pushPBMsg(playerId, PacketType.HangupSyn, gb.build().toByteString());
    }

    public void pushVipRoomListSyn(int playerId, VipRoomListSyn.Builder vb) {
        pushPBMsg(playerId, PacketType.VipRoomListSyn, vb.build().toByteString());
    }

    public void pushChatMsg(PlayerInfo p, String deskId, List<Integer> receiverIds, int contentType, byte[] conetnt) {
        GameChatMsgSyn.Builder gb = GameChatMsgSyn.newBuilder();
        gb.setData(ByteString.copyFrom(conetnt));
        gb.setContentType(contentType);
        gb.setDeskId(deskId);
        gb.setPosition(p.position);
        for (int receiver : receiverIds) {
            if (receiver == p.playerId && contentType == 3) continue; //语音不发自己
            BattleSession session = sessionManager.getIoSession(receiver);
            if (session != null) {
                pushPBMsg(session, PacketType.GameChatMsgSyn, gb.build().toByteString());
            }
        }
    }

    public void pushLocationSyn(packet.user.User.UserLocationSyn.Builder data, int playerId) {
        BattleSession session = sessionManager.getIoSession(playerId);
        pushPBMsg(session, PacketType.PlayerLocation, data.build().toByteString());
    }

    public void pushBeginMaiPiaoSyn(int playerId) {
        BattleSession session = sessionManager.getIoSession(playerId);
        pushPBMsg(session, PacketType.BeginMaiPiao, null);
    }

    public void pushMaiPiaoSyn(MJ.MaiPiaoSyn.Builder data, int playerID) {
        BattleSession session = sessionManager.getIoSession(playerID);
        pushPBMsg(session, PacketType.MaiPiaoSyn, data.build().toByteString());
    }
}
