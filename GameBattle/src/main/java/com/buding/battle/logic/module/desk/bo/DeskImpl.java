package com.buding.battle.logic.module.desk.bo;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.buding.api.player.vo.PlayerGameInfo;
import com.buding.battle.logic.event.*;
import com.buding.battle.logic.module.desk.bo.vo.MjLocationVo;
import com.buding.common.util.MapDistance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import packet.mj.MJ;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import com.buding.api.context.GameContext;
import com.buding.api.context.PlayHandResult;
import com.buding.api.desk.LogLevel;
import com.buding.api.desk.MJDesk;
import com.buding.api.game.Game;
import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.logic.module.common.AwayStatus;
import com.buding.battle.logic.module.common.BaseParent;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.Constants;
import com.buding.battle.logic.module.common.DeskStatus;
import com.buding.battle.logic.module.common.OnlineStatus;
import com.buding.battle.logic.module.common.PlayerStatus;
import com.buding.battle.logic.module.common.PushService;
import com.buding.battle.logic.module.common.ServiceRepo;
import com.buding.battle.logic.module.contants.StatusChangeReason;
import com.buding.battle.logic.module.desk.DeskGuard;
import com.buding.battle.logic.module.desk.listener.DeskListener;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.battle.logic.util.IDUtil;
import com.buding.common.monitor.Monitorable;
import com.buding.common.schedule.Job;
import com.buding.common.schedule.WorkerPool;
import com.buding.common.util.IOUtil;
import com.buding.db.model.GameLog;
import com.buding.db.model.User;
import com.buding.hall.config.DeskConfig;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.module.game.model.DeskModel;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.task.vo.GamePlayingVo;
import com.buding.hall.module.user.helper.UserHelper;
import com.buding.mj.helper.MJHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import packet.user.User.*;

public class DeskImpl extends BaseParent<Room> implements Monitorable, CommonDesk<byte[]> {
    Logger logger = LoggerFactory.getLogger(getClass());
    protected int ownerId = -1;

    protected transient DeskConfig deskConf;
    protected String id;
    protected transient Game game;
    protected DeskStatus status = DeskStatus.WATING;

    protected int maiPiaoCount = -1;

    protected DeskGuard guard;

    protected transient DeskListener listener;
    protected transient Room room;

    protected AtomicInteger timerId = new AtomicInteger();

    //桌面事件队列
    public BlockingQueue<DeskEvent> otherEventQueue = new LinkedBlockingQueue<>();
    //定时器队列
    public BlockingQueue<TimerEvent> timerEventQueue = new LinkedBlockingQueue<TimerEvent>();

    protected DeskTimer timer;

    private long workTime = 0;
    private long sleepTime = 0;
    private long lastSetTimer = -1;
    private int invokeTimerCount;
    private int delayTimerCount;
    private long lastCycleTime;
    protected long errCount = 0; // 发生错误次数，如果大于x, 则解散桌子
    private String replayData;
    private volatile long playerActiveTime;
    private String matchId;
    private String roomId;
    protected int gameCount = 0;
    protected long waitingGameStartTime = System.currentTimeMillis(); // 开始等待游戏时间
    private long waitingGameStopTime = System.currentTimeMillis(); // 开始等待游戏时间
    private boolean adminDesk = false;// 管理桌,调试用
    public long createTime = System.currentTimeMillis();
    protected Map<Integer, Boolean> voteMap = new HashMap<Integer, Boolean>();
    private long lastVoteTime = System.currentTimeMillis();
    private long pauseTime = System.currentTimeMillis();

    class DeskTimer extends Job {

        @Override
        public void run() {
            logger.info("act=startDeskLooper;deskId={}", getDeskID());
            while (true) {
                long startMills = System.currentTimeMillis();

                if (stop) {
                    logger.info("act=skipDeskLooperAsMarkStop1;deskId={};", getDeskID());
                    break;
                }

                try {
                    checkTimeEvent(); // 检查定时器事件
                } catch (Throwable e) {
                    logger.error("checkTimeEventError;deskId=" + getDeskID(), e);
                }

                if (stop) {
                    logger.info("act=skipDeskLooperAsMarkStop2;deskId={};", getDeskID());
                    break;
                }

                try {
                    if (lastSetTimer != -1 && System.currentTimeMillis() - lastSetTimer >= 180000) {// 3分钟
                        // logger.error("act=exitDeadThread;deskId=" +
                        // DeskImpl.this.getDeskID());
                        // DeskImpl.this.destroy();
                    }
                } catch (Throwable e) {
                    logger.error("TryKillTimeError;deskId=" + getDeskID(), e);
                }

                if (stop) {
                    logger.info("act=skipDeskLooperAsMarkStop3;deskId={};", getDeskID());
                    break;
                }

                try {
                    checkNetEvent();// 检查网络事件
                } catch (Throwable e) {
                    logger.error("checkNetEventError;deskId=" + getDeskID(), e);
                }

                if (stop) {
                    logger.info("act=skipDeskLooperAsMarkStop4;deskId={};", getDeskID());
                    break;
                }

                try {
                    checkPlayerEvent(); // 检测玩家状态
                } catch (Throwable e) {
                    logger.error("checkPlayerEventError;deskId=" + getDeskID(), e);
                }

                if (stop) {
                    logger.info("act=skipDeskLooperAsMarkStop5;deskId={};", getDeskID());
                    break;
                }

                try {
                    checkDeskStatus(); // 检测桌子状态
                } catch (Throwable e) {
                    logger.error("checkDeskStatus;deskId=" + getDeskID(), e);
                }

                if (stop) {
                    logger.info("act=skipDeskLooperAsMarkStop51;deskId={};", getDeskID());
                    break;
                }

                try {
                    tryStartGame(); // 尝试启动游戏
                } catch (Throwable e) {
                    logger.error("tryStartGameError;deskId=" + getDeskID(), e);
                }

                if (stop) {
                    logger.info("act=skipDeskLooperAsMarkStop6;deskId={};", getDeskID());
                    break;
                }

                try {
                    checkOtherEvent(); // 检查其它状态
                } catch (Throwable e) {
                    logger.error("checkOtherEventError;deskId=" + getDeskID(), e);
                }

                long endMills = System.currentTimeMillis();

                workTime += (endMills - startMills);

                lastCycleTime = startMills;

                try {
                    Thread.sleep(50);// 睡眠50毫秒
                } catch (Throwable e) {
                    logger.error("", e);
                }
                sleepTime += 50;

                //如果太慢，打警告
                if (endMills - startMills > 2000) {
                    logger.warn("act=deskLoop;error=slowLoop;deskId={}", id);
                }
            }
            logger.info("act=endDeskLooper;deskId={};", getDeskID());
        }
    }

    protected void checkOtherEvent() throws Exception {

    }

    private synchronized void checkDeskStatus() throws Exception {
        if (destroyDeskAsNoActivePlayer()) {
            return;
        }

        if (isEmpty() && findEmptyDeskAndKill()) {
            return;
        }

        if (findErrorDeskAndKill()) {
            return;
        }

        if (findDeadGameAndKill()) {
            return;
        }

        if (findFailStartAndKill()) {
            return;
        }

        if (findDeskPauseTimeout()) {
            return;
        }
    }

    private synchronized void checkPlayerEvent() throws Exception {
        List<Integer> set = guard.getplayerIdList();
        for (int playerId : set) {
            BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
            if (session == null) {
                logger.error("act=kickoutPlayer;error=sessionMiss;playerId={};deskId={};", playerId, getDeskID());
                guard.playerExit(playerId, "SessionNotFound");
                continue;
            }

            if (session.getStatus() == PlayerStatus.UNREADY) {
                long time = guard.getSitdownTime(playerId);
                if (isTime4Kickout(session, time)) {
                    logger.info("act=kickout;error=playerUnreadyTimeout;playerId={};deskId={};", playerId, getDeskID());
                    kickout(playerId, "你长时间没准备,已被踢出桌子");
                }
            }
        }

        for (int playerId : set) {
            BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
            if (session.onlineStatus == OnlineStatus.ONLINE) {
                playerActiveTime = System.currentTimeMillis();
                return;
            }
        }
    }

    private boolean findDeadGameAndKill() {
        if (status == DeskStatus.GAMING && System.currentTimeMillis() - waitingGameStopTime >= deskConf.secondsWaitingGameStop * 1000) {
            this.destroy(DeskDestoryReason.GAME_DEAD);
            return true;
        }
        return false;
    }

    private boolean findDeskPauseTimeout() {
        if (status == DeskStatus.GAMING_PAUSE && System.currentTimeMillis() - pauseTime >= deskConf.gamePauseTimeout * 1000) {
            this.destroy(DeskDestoryReason.CONTINUE_GAME_FAIL);
            return true;
        }
        return false;
    }

    private boolean findFailStartAndKill() {
        if (status == DeskStatus.WATING && System.currentTimeMillis() - waitingGameStartTime >= deskConf.secondsWaitingGameStart * 1000) {
            this.destroy(DeskDestoryReason.START_GAME_FAIL);
            return true;
        }
        return false;
    }

    private boolean findErrorDeskAndKill() {
        if (errCount > deskConf.errCount4KillDesk) {
            this.destroy(DeskDestoryReason.ERROR);
            return true;
        }
        return false;
    }

    private boolean findEmptyDeskAndKill() {
        if (System.currentTimeMillis() - playerActiveTime > deskConf.emptyDeskTTL * 1000) {
            this.destroy(DeskDestoryReason.EMPTY_DESK);
            return true;
        }
        return false;
    }

    private boolean destroyDeskAsNoActivePlayer() {
        if (System.currentTimeMillis() - playerActiveTime > deskConf.secondsWaitActivePlayer * 1000) {
            this.destroy(DeskDestoryReason.NO_ACTIVIE_PLAYER);
            return true;
        }
        return false;
    }

    private boolean isTime4Kickout(BattleSession session, long sitdowntime) {
        return deskConf.secondsBeforKickout > 0 && System.currentTimeMillis() - sitdowntime > deskConf.secondsBeforKickout * 1000;
    }

    private void checkNetEvent() throws Exception {
        DeskEvent event = otherEventQueue.poll();
        if (event == null) {
            return;
        }

        switch (event.key) {
            case GAME_MSG: {
                GameMsgEvent e = (GameMsgEvent) event;
                gameMsgRecieve(e.playerId, e.content);
            }
            break;
            case PLAYER_READY: {
                PlayerReadyEvent e = (PlayerReadyEvent) event;
                playerReady(e.playerId);
            }
            break;
            case CHANGE_DESK: {
                ChangeDeskEvent e = (ChangeDeskEvent) event;
                changeDesk(e);
            }
            break;
            case PLAYER_OFFLINE: {
                PlayerOfflineEvent e = (PlayerOfflineEvent) event;
                playerOffline(e.playerId);
                break;
            }
            case PLAYER_RECONNECT: {
                PlayReconnectEvent e = (PlayReconnectEvent) event;
                playerReconnect(e.playerId);
                break;
            }
            case PLAYER_EXIT: {
                PlayExitEvent e = (PlayExitEvent) event;
                playerTryExit(e.playerId, PlayerExitType.REQUEST_EXIT);
                break;
            }
            case PLAYER_AWAY: {
                PlayAwayEvent e = (PlayAwayEvent) event;
                playerTryAway(e.playerId);
                break;
            }
            case PLAYER_COMBACK: {
                PlayComBackEvent e = (PlayComBackEvent) event;
                playerComeBack(e.playerId);
                break;
            }
            case KICKOUT_PLAYER: {
                KickoutEvent e = (KickoutEvent) event;
                requestKickout(e.playerId, e.targetPlayerId);
                break;
            }
            case PLAYER_HANGUP: {
                PlayerHangupEvent e = (PlayerHangupEvent) event;
                playerHangeup(e.playerId);
                break;
            }
            case PLAYER_CANCELUP: {
                PlayerCancelHangupEvent e = (PlayerCancelHangupEvent) event;
                playerCancelHangup(e.playerId);
                break;
            }
            case DISMISS: {
                dissmiss();
                break;
            }
            case VOTE_DISSMISS: {
                PlayerVoteDissmissEvent e = (PlayerVoteDissmissEvent) event;
                playerVoteDissmiss(e.playerId, e.agree);
                break;
            }
            default:
                logger.error("act=checkNetEvent;error=keyMismatch;key={};", event.key);
                break;
        }
    }

    private void dissmiss() {
        logger.info("act=dismiss;deskId={};", this.getDeskID());
        destroy(DeskDestoryReason.REQUEST_DIMISS);
    }

    protected void changeDesk(ChangeDeskEvent e) throws Exception {
        logger.error("act=changeDesk;playerId={};deskId={};", e.playerId, getDeskID());

        BattleSession session = ServiceRepo.sessionManager.getIoSession(e.playerId);
        if (session == null) {
            logger.error("act=changeDeskError;error=sessionMiss;playerId={};deskId={};", e.playerId, getDeskID());
            return;
        }

        PlayerInfo player = guard.getPlayerById(e.playerId);
        if (player == null) {// 并发情况下，会出现这种情况
            logger.error("act=changeDeskError;error=playerMiss;playerId={};deskId={};", e.playerId, id);
        }

        long t = guard.getSitdownTime(e.playerId);

        if (System.currentTimeMillis() - t <= 2000) {
            logger.error("act=changeDeskError;error=frequent;playerId={};deskId={};", e.playerId, id);
            PushService.instance.pushChangeDeskRsp(session.userId, false, "换桌过于频繁");
            return;
        }

        room.changeDesk(e.playerId);
    }

    private void checkTimeEvent() throws Exception {
        TimerEvent waitingEvent = null;

        while (true) {
            TimerEvent tmpEvent = timerEventQueue.peek();

            if (tmpEvent == null) { // 队列为空
                return;
            }

            if (tmpEvent == waitingEvent) {// 队列检查已经循环了一次
                return;
            }

            tmpEvent = timerEventQueue.poll();

            //尝试启动定时器
            tmpEvent = tryTriggerTimer(tmpEvent);
            //时间未到重新放到队列中
            if (tmpEvent != null) {
                timerEventQueue.put(tmpEvent);
                if (waitingEvent == null) {
                    waitingEvent = tmpEvent;
                }
            }
        }
    }

    private TimerEvent tryTriggerTimer(TimerEvent event) {
        if (event.killed) {
            return null;
        }

        long mills = event.triggerTime;
        long now = System.currentTimeMillis();
        if (mills > now) {
            return event; // 时间未到
        }
        invokeTimerCount++;
        if (now - mills >= deskConf.timerDelayThreadShold) {
            delayTimerCount++;
        }
        game.onTimer(event.timerId);
        return null; // 定时器已经执行
    }

    public DeskImpl(DeskListener listener, Room room, DeskConfig deskConf, String deskId) {
        super(room);

        this.id = deskId;
        if (this.id == null) {
            this.id = IDUtil.instance.genIntId("Desk");
        }
        this.listener = listener;
        this.room = room;
        this.playerActiveTime = System.currentTimeMillis();
        this.matchId = room.getParent().getId();
        this.roomId = room.getRoomId();

        logger.info("act=deskInit;deskId={};", id);

        try {
            guard = new DeskGuard(deskConf);
            this.deskConf = deskConf;
            Class<?> cls = getClass().getClassLoader().loadClass(deskConf.gameClassFullName);
            game = (Game) cls.newInstance();
            game.setDesk(this, deskConf.gameParam);
            timer = new DeskTimer();
            WorkerPool.instances.submitJob(timer);

            if (this.listener != null) {
                listener.onDeskCreate(this);
            }

            markDeskAsWaitingGame();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPlayerHangupPacketReceived(int playerID) {
        PlayerHangupEvent event = new PlayerHangupEvent();
        event.key = DeskEventKey.PLAYER_HANGUP;
        event.playerId = playerID;
        otherEventQueue.add(event);
    }

    private void playerHangeup(int playerID) {
        PlayerInfo p = this.guard.getPlayerById(playerID);
        this.game.hangeUp(p);
    }

    private void playerVoteDissmiss(int playerId, boolean agree) {
        PlayerInfo votePlayer = this.guard.getPlayerById(playerId);
        if (System.currentTimeMillis() - lastVoteTime > 10 * 1000) { // 距离上一次发起投票10秒了，需要重新发起
            voteMap.clear();
            for (PlayerInfo p : guard.getPlayerList()) {
                if (p.playerId != playerId) {
                    PushService.instance.pushDismissVote(p.playerId, votePlayer.position, agree);
                }
            }
            lastVoteTime = System.currentTimeMillis();
        }
        voteMap.put(playerId, agree);
        afterPlayerVote();

        if (isVotingDismissAndPass()) {
            this.game.gameDismiss();
        }

        // 如果全部玩家回复了，则也重置计时器
        if (voteMap.size() >= guard.getPlayerCount()) {
            lastVoteTime = 0;
            voteMap.clear();
        }
    }

    protected void afterPlayerVote() {

    }

    private boolean isVotingDismissAndPass() {
        int i = 0;
        for (boolean e : voteMap.values()) {
            if (e)
                i++;
        }
        // 多半数同意即解散桌子
        if (i * 2 > guard.getPlayerCount()) {
            return true;
        }
        return false;
    }

    @Override
    public void onPlayerHangup(int position) {
        PlayerInfo p = this.guard.getPlayerByPos(position);
        if (p == null) {
            return;
        }
        PushService.instance.pushHangupSyn(p.playerId, p.position, Constants.PLAYER_HANGUP);
    }

    @Override
    public void onPlayerCancelHangupPacketReceived(int playerID) {
        PlayerCancelHangupEvent event = new PlayerCancelHangupEvent();
        event.key = DeskEventKey.PLAYER_CANCELUP;
        event.playerId = playerID;
        otherEventQueue.add(event);
    }

    @Override
    public void onPlayerDissVotePacketReceived(int playerId, boolean agree) {
        PlayerVoteDissmissEvent event = new PlayerVoteDissmissEvent();
        event.playerId = playerId;
        event.agree = agree;
        event.key = DeskEventKey.VOTE_DISSMISS;
        otherEventQueue.add(event);
    }

    private void playerCancelHangup(int playerID) {
        PlayerInfo p = this.guard.getPlayerById(playerID);
        this.game.cancelHangeUp(p);
    }

    @Override
    public void onPlayerCancelHangup(int position) {
        PlayerInfo p = this.guard.getPlayerByPos(position);
        if (p == null) {
            return;
        }
        PushService.instance.pushHangupSyn(p.playerId, p.position, Constants.PLAYER_UNHANGUP);
    }

    @Override
    public byte getGunBaoCard(byte baocard) {
        if (baocard == 64)
            return baocard;
        if (baocard == 9 || baocard == 25 || baocard == 41)
            return (byte) (baocard - 8);
        return (byte) (baocard + 1);
    }

    @Override
    public void sendMsgToGoodPlayer(int playerId, byte[] bytes) {
        BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
        PacketBase.Builder pb = PacketBase.newBuilder();
        pb.setPacketType(PacketType.ShowBestCards);
        pb.setData(ByteString.copyFrom(bytes));
        ServiceRepo.sessionManager.write(session, pb.build().toByteArray());
    }

    @Override
    public void sendMsgChangeCardSyn(int playerId, byte[] bytes) {
        BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
        PacketBase.Builder pb = PacketBase.newBuilder();
        pb.setPacketType(PacketType.ChangeBestCard);
        pb.setData(ByteString.copyFrom(bytes));
        ServiceRepo.sessionManager.write(session, pb.build().toByteArray());
    }

    @Override
    public PlayerInfo getDeskPlayer(int nDeskPos) {
        return guard.getPlayerByPos(nDeskPos);
    }

    // @Override
    // public <T> void sendMsg2Player(int position, T content) {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public <T> void onGameMsgPacketReceived(int playerID, T content) {
    // // TODO Auto-generated method stub
    //
    // }

    @Override
    public void sendMsg2Player(int position, byte[] content) {
        try {
            PlayerInfo player = guard.getPlayerByPos(position);
            if (player.robot == 1) {
                return;
            }
            BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
            if (session.awayStatus == AwayStatus.AWAY) {
                return;
            }
            PacketBase.Builder pb = PacketBase.newBuilder();
            pb.setPacketType(PacketType.GameOperation);
            pb.setData(ByteString.copyFrom(content));
            ServiceRepo.sessionManager.write(session, pb.build().toByteArray());
        } catch (Exception e) {
            logger.info("act=sendMsg2Player;error=exception;", e);
        }
    }

    @Override
    public void sendMsg2Desk(byte[] content) {
        for (PlayerInfo player : guard.getPlayerList()) {
            sendMsg2Player(player.position, content);
        }
    }

    @Override
    public void sendMsg2DeskExceptPosition(byte[] content, int excludePosition) {
        for (PlayerInfo player : guard.getPlayerList()) {
            if (player.position != excludePosition) {
                sendMsg2Player(player.position, content);
            }
        }
    }

    @Override
    public int setTimer(long mills) {
        TimerEvent tv = new TimerEvent();
        tv.setTime = System.currentTimeMillis();
        tv.triggerTime = tv.setTime + mills;
        tv.timerId = timerId.addAndGet(1);
        timerEventQueue.add(tv);
        lastSetTimer = tv.setTime;

        return tv.timerId;
    }

    @Override
    public void killTimer(int timerID) {
        for (TimerEvent tv : timerEventQueue) {
            if (tv.timerId == timerID) {
                tv.killed = true;
            }
        }
    }

    @Override
    public String getDeskID() {
        return id;
    }

    private void onGameBegin() {
        this.gameCount++;
        this.waitingGameStopTime = System.currentTimeMillis();
        logger.info("act=onGameBegin;deskId={};", getDeskID());

        for (PlayerInfo player : guard.getPlayerList()) {
            BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
            if (session != null) {
                session.setStatus(PlayerStatus.GAMING, StatusChangeReason.GAME_BEGIN);
                session.currentModule = ServiceRepo.gameModule;
                ServiceRepo.clusterStubService.notifyUserPlaying(session.userId, session.getGame().getId(), ServiceRepo.serverConfig.instanceId);
            }

            subUerviceFee(player);
        }

        this.status = DeskStatus.GAMING;

        if (listener != null) {
            listener.onDeskGameStart(this, game);
        }
    }

    public void addGameLog(GameContext ctx) {
        GameLog log = new GameLog();
        log.setDeskId(getDeskID());
        log.setRoomId(getParent().getRoomId());
        log.setMatchId(getParent().getParent().getId());
        log.setGameId(getParent().getParent().getParent().getId());
        log.setGameStartTime(new Date(ctx.playerHandResults.startTime));
        log.setGameEndTime(new Date());
        if (ctx.playerHandResults.playDetail.length > 0) {
            log.setUser1Id(ctx.playerHandResults.playDetail[0].playerId);
            log.setUser1Score(ctx.playerHandResults.playDetail[0].score);
            log.setUser1FanNum(ctx.playerHandResults.playDetail[0].fanNum);
            log.setUser1FanDesc(MJHelper.getFanDescList2String(ctx.playerHandResults.playDetail[0].fanType));
        }
        if (ctx.playerHandResults.playDetail.length > 1) {
            log.setUser2Id(ctx.playerHandResults.playDetail[1].playerId);
            log.setUser2Score(ctx.playerHandResults.playDetail[1].score);
            log.setUser2FanNum(ctx.playerHandResults.playDetail[1].fanNum);
            log.setUser2FanDesc(MJHelper.getFanDescList2String(ctx.playerHandResults.playDetail[1].fanType));
        }
        if (ctx.playerHandResults.playDetail.length > 2) {
            log.setUser3Id(ctx.playerHandResults.playDetail[2].playerId);
            log.setUser3Score(ctx.playerHandResults.playDetail[2].score);
            log.setUser3FanNum(ctx.playerHandResults.playDetail[2].fanNum);
            log.setUser3FanDesc(MJHelper.getFanDescList2String(ctx.playerHandResults.playDetail[2].fanType));
        }
        if (ctx.playerHandResults.playDetail.length > 3) {
            log.setUser4Id(ctx.playerHandResults.playDetail[3].playerId);
            log.setUser4Score(ctx.playerHandResults.playDetail[3].score);
            log.setUser4FanNum(ctx.playerHandResults.playDetail[3].fanNum);
            log.setUser4FanDesc(MJHelper.getFanDescList2String(ctx.playerHandResults.playDetail[3].fanType));
        }
        ServiceRepo.hallPortalService.addGameLog(log);
    }

    protected void subUerviceFee(PlayerInfo player) {
        if (getFee() > 0) {
            // 扣除台费
            ServiceRepo.hallPortalService.changeCoin(player.playerId, -1 * (int) getFee(), false, ItemChangeReason.ENROLL);
        }
    }

    @Override
    public synchronized void onGameOver() {
        try {
            logger.info("act=onGameOver;deskId={};", getDeskID());

            List<PlayerInfo> players = guard.getPlayerList();
            for (PlayerInfo player : players) {
                BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
                if (session != null) {
                    session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.GAME_FINISH);
                    session.currentModule = ServiceRepo.matchModule;
                }
                PushService.instance.pushGameStopMsg(player.playerId, id);// 推送游戏结束消息
                ServiceRepo.clusterStubService.removeUserPlaying(session.userId, session.getGame().getId(), ServiceRepo.serverConfig.instanceId);
            }

            this.status = DeskStatus.WATING;

            // ServiceRepo.configManager.getMatchConfig(this.getParent().getParent().getMatchConfig().gameID,
            // player);

            if (this.isAutoChangeDesk() == false) {
                guard.ready4NextGame();
                this.reset();
            }

            if (listener != null) {
                listener.onDeskGameFinish(this, game);
            }

            // 检查是否符合入场条件 TODO
            // for (PlayerInfo player : players) {
            // BattleSession session =
            // ServiceRepo.sessionManager.getIoSession(player.playerId);
            // if (session != null) {
            // ServiceRepo.matchService.checkCoinInMath(session,
            // session.getMatch());
            // }
            // }
        } catch (Exception e) {
            logger.error("act=onGameFinishError;deskId=" + getDeskID(), e);
        }
    }

    protected synchronized void playerReady(int playerId) {
        logger.info("act=playerReady;playerId={};deskId={};", playerId, getDeskID());
        PlayerInfo player = guard.getPlayerById(playerId);
        if (player == null) {
            logger.error("act=playerReady;error=playerMiss;playerId={};deskId={};", playerId, getDeskID());
            return;
        }
        BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
        if (session == null) {
            logger.error("act=playerReady;error=sessionMiss;playerId={};deskId={};", playerId, getDeskID());
            return;
        }
        if (session.getStatus() == PlayerStatus.READY || session.getStatus() == PlayerStatus.GAMING) {
            logger.error("act=playerReady;error=alreadySo;playerId={};deskId={};", playerId, getDeskID());
            return;
        }

        game.playerAgree(player);

        session.setStatus(PlayerStatus.READY, StatusChangeReason.READY);
        session.currentModule = ServiceRepo.gameModule;

        if (listener != null) {
            listener.onPlayerReady(this, player);
        }

        for (int deskPlayer : guard.getplayerIdList()) {
            PushService.instance.pushReadySyn(deskPlayer, player.position, player.playerId);
        }

        tryStartGame();
    }

    @Override
    public void tryStartGame() {
        // 桌子不是组队状态,返回
        if (status != DeskStatus.WATING) {
            return;
        }

        // 玩家人数未达到开赛要求,返回
        if (guard.getPlayerCount() < deskConf.seatSizeLower) {
            return;
        }

        if (maiPiaoCount == -1) {
            //选择买漂
            for (PlayerInfo p : guard.getPlayerList()) {
                PushService.instance.pushBeginMaiPiaoSyn(p.getPlayerID());
            }
            maiPiaoCount++;
        }

        // 需要所有玩家都买漂,否则返回
        if (guard.getPlayerCount() > maiPiaoCount) {
            return;
        }

        // 不是全部玩家处于就绪状态,返回
        List<PlayerInfo> playerList = guard.getPlayerList();
        for (PlayerInfo p : playerList) {
            BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
            if (session != null) {
                if (session.getStatus() != PlayerStatus.READY) {
                    return;
                }
            }
        }

        maiPiaoCount = -1;
        // 推送游戏开始消息给玩家
        for (PlayerInfo p : playerList) {
            PushService.instance.pushGameStartMsg(p.playerId, id);
        }

        // 管理员查看回放数据
        if (isAdminUse()) {
            try {
                byte data[] = IOUtil.tryGetFileData("/home/game/data/replay.json");
                if (data != null) {
                    String json = new String(data, "UTF-8");
                    this.setReplayData(json);
                }
            } catch (Exception e) {
                logger.error("loadReplayDataError", e);
            }
        }

        // 告知游戏模块开始事件
        game.gameBegin();

        // 更新内部信息
        onGameBegin();
    }

    @Override
    public boolean isEmpty() {
        return guard.isEmpty();
    }

    @Override
    public boolean isFull() {
        return guard.isFull();
    }

    @Override
    public synchronized int playerSit(BattleContext ctx) {
        BattleSession session = ServiceRepo.sessionManager.getIoSession(ctx.playerId);

        // if(session != null && session.user != null && session.user.getRole()
        // != null && (session.user.getRole() & 1) ==1) {
        // this.markAsAdminUse();
        // }

        PlayerInfo player = session.player;
        int ind = guard.getEmptySeat(this);

        if (ind == -1) {
            logger.error("act=playerSit;error=invalidPos;playerId={};deskId={};", ctx.playerId, getDeskID());
            return ind;
        }

        if (ind != -1) {
            session.enterDesk(this, ind);
            session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.SIT);
            session.awayStatus = AwayStatus.BACK;
            guard.playerSit(player, ind);

            if (listener != null) {
                listener.onPlayerSit(this, player);
            }
            game.playerSit(player);
            logger.info("act=playerSit;position={};deskId={};roomId={};matchId={};", ctx.playerId, ind, this.getDeskID(), this.getParent().getRoomId(), this.getParent().getParent().getId());
        }

        // 推送正在游戏的消息
        pushGamingInfo(ctx);

        // 推送桌子信息
        PushService.instance.pushDeskInfo(session.userId, this.id, this.getPlayerCount(), null);

        {
            // 向其它人推送入场者的信息
            for (PlayerInfo p : guard.getPlayerList()) {
                PushService.instance.pushPlayerSitSyn(player.playerId, p.playerId);
            }

            // 向入场者推送所有人的信息
            for (PlayerInfo p : guard.getPlayerList()) {
                if (p != player) {
                    PushService.instance.pushPlayerSitSyn(p.playerId, player.playerId);
                }
            }

            // 向入场者推送已准备的玩家消息
            for (PlayerInfo p : guard.getPlayerList()) {
                BattleSession s = ServiceRepo.sessionManager.getIoSession(p.playerId);
                if (s.getStatus() == PlayerStatus.READY) {
                    PushService.instance.pushReadySyn(ctx.playerId, p.position, p.playerId);
                }
            }
            //推送定位数据
            pushLocationSyn();
        }

        onPlayerAfterSit(player);

        return ind;
    }

    private void pushLocationSyn() {
        Map<Integer, String> playerLocation = Maps.newHashMap();
        Map<Integer, String> playerAddress = Maps.newHashMap();
        Map<Integer, String> playerIp = Maps.newHashMap();
        for (PlayerInfo p : guard.getPlayerList()) {
            BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
            String ip = "127.0.0.1";
            if (session != null) {
                if (session.getChannel() != null) {
                    SocketAddress remoteAddress = session.getChannel().remoteAddress();
                    ip = ((InetSocketAddress) remoteAddress).getAddress().getHostAddress();
                }
            }
            playerIp.put(p.playerId, ip);
            if (session.getLocation() == null) {
                session.setLocation("0,0");
            }
            playerLocation.put(p.playerId, session.getLocation());
            String address = MapDistance.getAddrByBaidu(session.getLocation());
            if (StringUtils.isBlank(address)) {
                address = "当前用户未开启定位~";
            }
            playerAddress.put(p.playerId, address);

        }


        UserLocationSyn.Builder uls = UserLocationSyn.newBuilder();
        Map<String, Map<String, MjLocationVo>> data = Maps.newHashMap();
        for (Map.Entry<Integer, String> entry : playerLocation.entrySet()) {
            int playerId = entry.getKey();
            String location = entry.getValue();
            String[] locationArray = MapDistance.getLocationArray(location);
            if (locationArray == null) {
                continue;
            }
            Map<String, MjLocationVo> template = Maps.newHashMap();
            MjLocationVo myVo = new MjLocationVo();
            myVo.setDistance("0");
            String addr = playerAddress.get(playerId);
            if (StringUtils.isBlank(addr)) {
                addr = "当前用户未开启定位~";
            }
            myVo.setAddress(addr);
            myVo.setIp(playerIp.get(playerId));

            template.put(String.valueOf(playerId), myVo);
            for (Map.Entry<Integer, String> templateEntry : playerLocation.entrySet()) {
                int templatePlayerId = templateEntry.getKey();
                if (playerId == templatePlayerId) {
                    continue;
                }
                MjLocationVo otherVo = new MjLocationVo();
                String templateLocation = templateEntry.getValue();
                String[] locationArray1 = MapDistance.getLocationArray(templateLocation);
                if (locationArray1 == null) {
                    continue;
                }
                String address = playerAddress.get(templatePlayerId);
                String distance = String.valueOf(MapDistance.getDistance(Double.parseDouble(locationArray[0]), Double.parseDouble(locationArray[1]), Double.parseDouble(locationArray1[0]), Double.parseDouble(locationArray1[1])));
                if (StringUtils.isBlank(address)) {
                    address = "当前用户未开启定位~";
                }
                otherVo.setAddress(address);
                otherVo.setIp(playerIp.get(playerId));
                otherVo.setDistance(distance);
                template.put(String.valueOf(templatePlayerId), otherVo);
            }
            data.put(String.valueOf(playerId), template);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            uls.setLocation(mapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            logger.info(e.getMessage());
        }
        for (PlayerInfo p : guard.getPlayerList()) {
            PushService.instance.pushLocationSyn(uls, p.playerId);
        }
    }

    private void pushGamingInfo(BattleContext ctx) {
        int wanfa = this instanceof MJDesk ? ((MJDesk) this).getWanfa() : 0; // TODO
        // 不好的设计
        int roomType = this instanceof MJDesk ? ((MJDesk) this).getRoomType() : 0; // TODO
        // 不好的设计
        int totalQuan = this instanceof MJDesk ? ((MJDesk) this).getTotalQuan() : 0; // TODO
        // 不好的设计
        PushService.instance.pushPlayerGamingInfo(ctx.session.userId, ctx.gameId, ctx.matchId, ctx.getRoomId(), this.getDeskID() + "", wanfa, roomType, totalQuan);
    }

    protected void onPlayerAfterSit(PlayerInfo player) {

    }

    @Override
    public void kickout(int playerId, String msg) {
        logger.info("act=kickout;reason={};playerId={};deskId={};", PlayerExitType.UNREADY_KICK, playerId, getDeskID());

        playerExit(playerId, PlayerExitType.UNREADY_KICK);

        BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
        if (session == null) {
            logger.error("act=kickout;reason=sessionMiss;playerId={};deskId={};", playerId, getDeskID());
            return;
        }

        // PushService.instance.pushDeskInfo(playerId, null, -1, msg);
        PushService.instance.pushKickoutSyn(playerId, msg);
    }

    protected void requestKickout(int requester, int targetPlayerId) {
        logger.error("act=requestKickout;error=illegalreq;player={};targetPlayer={};", requester, targetPlayerId);
        PushService.instance.pushDeskPlayerKickoutRsp(requester, false, "普通场不支持踢人");
    }

    protected synchronized void playerTryAway(int playerId) {
        if (this.deskConf.awayIsExit) { //尝试退出
            this.playerTryExit(playerId, PlayerExitType.REQUEST_EXIT);
            return;
        }

        playerAway(playerId);
    }

    private void playerAway(int playerId) {
        BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
        if (session == null) {
            logger.error("act=playerAway;error=sessionMiss;playerId={};deskId={};", playerId, getDeskID());
            return;
        }
        PlayerInfo player = guard.getPlayerById(playerId);
        if (player == null) {
            logger.error("act=playerAway;error=playerMiss;playerId={};deskId={};", playerId, getDeskID());
            return;
        }

        //广播离开状态
        game.playerAway(player);

        session.awayStatus = AwayStatus.AWAY;
        for (PlayerInfo p : guard.getPlayerList()) {
            PushService.instance.pushPlayerAwaySyn(player.position, playerId, p.playerId);
        }
        //离开桌子时回调
        onPlayerAfterAway(player);
    }

    protected synchronized void playerTryExit(int playerId, PlayerExitType reason) {
        BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
        if (session == null) {
            logger.error("act=playerTryExit;error=sessionMiss;reason={};playerId={};deskId={};", reason, playerId, getDeskID());
            return;
        }

        //普通场如果游戏中，退出，则托管游戏
        if (session.getStatus() == PlayerStatus.GAMING && !deskConf.allowExitWhenGaming) {
            playerAway(playerId); //如果是游戏中，当作是playerLeave处理
            return;
        }

        playerExit(playerId, reason);
    }

    protected synchronized void tryPauseGame() {
        if (this.status == DeskStatus.GAMING_PAUSE) {
            return;
        }
        this.status = DeskStatus.GAMING_PAUSE;
        this.pauseTime = System.currentTimeMillis();
        this.game.gamePause();

        for (int playerId : guard.getplayerIdList()) {
            PushService.instance.pushGamePauseSyn(this.id, playerId);
        }
    }

    @Override
    public synchronized void playerExit(int playerId, PlayerExitType reason) {
        logger.error("act=playerExit;reason={};playerId={};deskId={};", reason, playerId, getDeskID());

        // 告知管理人,申请离开, 腾出座位
        PlayerInfo player = guard.playerExit(playerId, reason.toString());

        if (player == null) {
            logger.error("act=playerExit;error=playerMiss;reason={};playerId={};deskId={};", reason, playerId, getDeskID());
            return;
        }

        game.playerExit(player);

        BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);

        if (session != null) {
            session.leaveDesk();
            session.setStatus(PlayerStatus.ENTER_ROOM, StatusChangeReason.LEAVE);// 在房间，不在桌子上
            session.currentModule = ServiceRepo.matchModule;
        }

        if (listener != null) {
            listener.onPlayerLeave(this, player);
        }

        // 告知同桌我已离开
        for (int playerOnDesk : guard.getplayerIdList()) {
            PushService.instance.pushPlayerExitSyn(player.position, playerId, playerOnDesk);
        }

        PushService.instance.pushPlayerExitSyn(player.position, playerId, playerId);

        onPlayerAfterExit(player);
    }

    @Override
    public void destroy(DeskDestoryReason reason) {
        logger.error("act=destroy;deskId={};reason={};", getDeskID(), reason);

        List<PlayerInfo> playerList = guard.getPlayerList();
        for (PlayerInfo player : playerList) {
            for (PlayerInfo other : playerList) {
                if (player != other) {
                    PushService.instance.pushPlayerExitSyn(player.position, player.playerId, other.playerId);
                }
            }
            for (PlayerInfo other : playerList) {
                if (player == other) {
                    PushService.instance.pushPlayerExitSyn(player.position, player.playerId, other.playerId);
                }
            }
        }

        for (PlayerInfo player : playerList) {
            BattleSession session = ServiceRepo.sessionManager.getIoSession(player.playerId);
            if (session != null) {
                session.leaveDesk();
                session.currentModule = ServiceRepo.matchModule;
                PushService.instance.pushDeskDestory(player.playerId, this.getDeskID());
            }
            guard.playerExit(player.playerId, "DeskDestroy");
            onPlayerAfterExit(player);
            this.listener.onPlayerLeave(this, player);
        }

        if (timer != null) {
            timer.setStop(true);
        }

        if (listener != null) {
            listener.onDeskDestroy(this);
        }
        this.status = DeskStatus.DESTROYED;
        dumpGameData();
    }

    @Override
    public String dumpGameData() {
        try {
            String data = this.game.dumpGameData();
            String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String defaultPath = "/home/game/data/";
            String thisPath = date + "/" + System.nanoTime() + ".json";
            IOUtil.writeFileContent(defaultPath + thisPath, data);
            return thisPath;
        } catch (Exception e) {
            logger.error("act=dumpGameDataError;deskId=" + getDeskID(), e);
        }
        return null;
    }

    protected void onPlayerAfterExit(PlayerInfo player) {

    }

    protected void onPlayerAfterAway(PlayerInfo player) {
        //默认玩家离开时托管游戏
        this.playerHangeup(player.playerId);
    }

    private void gameMsgRecieve(int playerID, byte[] content) {
        PlayerInfo player = guard.getPlayerById(playerID);
        game.handleGameMsg(player.position, content);
    }

    @Override
    public void onPlayerReadyPacketReceived(int playerId) {
        PlayerReadyEvent event = new PlayerReadyEvent();
        event.key = DeskEventKey.PLAYER_READY;
        event.playerId = playerId;
        otherEventQueue.add(event);
    }

    @Override
    public void onGameMsgPacketReceived(int playerID, byte[] content) {
        GameMsgEvent event = new GameMsgEvent();
        event.key = DeskEventKey.GAME_MSG;
        event.playerId = playerID;
        event.content = content;
        otherEventQueue.add(event);
    }

    @Override
    public void onChatMsgPacketReceived(int playerID, int contentType, byte[] content) {
        try {
            PlayerInfo p = guard.getPlayerById(playerID);
            if (p == null) {
                return;
            }
            PushService.instance.pushChatMsg(p, getDeskID(), guard.getplayerIdList(), contentType, content);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Override
    public void onKickoutPacketReceived(int playerID, int targetPlayerId) {
        KickoutEvent event = new KickoutEvent();
        event.key = DeskEventKey.KICKOUT_PLAYER;
        event.playerId = playerID;
        event.targetPlayerId = targetPlayerId;
        otherEventQueue.add(event);
    }

    @Override
    public void onPlayerMaiPiaoReceived(MJ.MaiPiaoSyn data, BattleSession session) {
        PlayerInfo p = guard.getPlayerById(session.getPlayerId());
        if (p == null) {
            return;
        }
        game.playerMaiPiao(new PlayerGameInfo(p.getPlayerID(),data.getIsBuy(),data.getPiaoNum()));
        MJ.MaiPiaoSyn.Builder msg = MJ.MaiPiaoSyn.newBuilder();
        msg.setPlayerId(p.getPlayerID());
        msg.setIsBuy(data.getIsBuy());
        msg.setPiaoNum(data.getPiaoNum());
        for (PlayerInfo other : guard.getPlayerList()) {
            PushService.instance.pushMaiPiaoSyn(msg, other.getPlayerID());
        }
        maiPiaoCount++;
    }

    @Override
    public void onPlayerSeeBestCardReceived(BattleSession session) {
        PlayerInfo p = guard.getPlayerById(session.getPlayerId());
        if (p == null) {
            return;
        }
        if (p.isGoodPlayer()){
            game.playerSeeBestCard(p);
        }
    }


    @Override
    public void onPlayerChangeBestCardReceived(BattleSession session,MJ.ChangeBestCards data) {
        PlayerInfo p = guard.getPlayerById(session.getPlayerId());
        if (p == null) {
            return;
        }
        if (p.isGoodPlayer()){
            game.playerChangeBestCard(p,data.getCard());
        }
    }

    @Override
    public void onPlayerChangeDeskPacketReceived(int playerId) {
        ChangeDeskEvent event = new ChangeDeskEvent();
        event.key = DeskEventKey.CHANGE_DESK;
        event.playerId = playerId;
        otherEventQueue.add(event);
    }

    @Override
    public int getBasePoint() {
        return this.getParent().getRoomConfig().basePoint;
    }

    @Override
    public void finalSettle(GameContext context) {
        // nothing 每局分开结算，总结算可以不用
    }

    private void settleInternal(PlayHandResult res) {
        PlayerInfo p = guard.getPlayerByPos(res.pos);

        if (p == null) {
            return;
        }

        // 机器人结算
        if (p.robot == 1) {
            ServiceRepo.robotManager.robotSettle(p, this.getParent().getParent().getMatchConfig().matchID, res.score);
        }

        int playerid = p.playerId;

        User user = ServiceRepo.hallPortalService.getUser(p.playerId);
        if (user == null) {
            logger.error("act=settleInternal;error=userMiss;playerId={};deskId={};", p.playerId, getDeskID());
            return;
        }

        if (res.score < 0) {
            logger.error("ErrorRankPoint,userId:{},point:{};", playerid, res.score);
        }

        GamePlayingVo ret = new GamePlayingVo();
        ret.coin = res.score;
        ret.gameId = this.getParent().getParent().getParent().getId();
        ret.matchId = this.getParent().getParent().getId();
        ret.enemyBankrupt = false; // TODO
        ret.bankrupt = false; // TODO
        ret.rankPoint = res.score;
        ret.tax = res.tax;
        ret.userId = res.playerId;
        ret.winCount = res.result == PlayHandResult.GAME_RESULT_WIN ? 1 : 0;
        ret.loseCount = res.result == PlayHandResult.GAME_RESULT_LOSE ? 1 : 0;
        ret.evenCount = res.result == PlayHandResult.GAME_RESULT_EVEN ? 1 : 0;
        ret.continueWin = ret.winCount;
        ret.gameTime = new Date();
        ret.maxFanDesc = res.fanDesc;
        ret.maxFanType = res.fanType;
        ret.maxFanNum = res.fanNum;
        ret.maxDownCards = res.downcards;
        ret.maxFanHandCards = res.handcards;

        ServiceRepo.hallPortalService.addGameResult(ret);

        // 更新用户属性
        BattleSession session = ServiceRepo.sessionManager.getIoSession(ret.userId);
        if (user != null && session != null) {
            UserHelper.copyUser2Player(user, session.player);
        }

        MatchConfig conf = this.getParent().getParent().getMatchConfig();
        if (conf.game.isRank || conf.isRank) { // 是排位赛
            // ServiceRepo.userRankServiceStub.addUserRankPoint(res.playerId,
            // res.playerName, conf.gameID, res.score, new Date());
        }
    }

    @Override
    public int getPlayerCount() {
        return guard.getPlayerCount();
    }

    @Override
    public DeskStatus getStatus() {
        return this.status;
    }

    @Override
    public boolean isAutoChangeDesk() {
        return deskConf.autoChangeDesk;
    }

    @Override
    public void reset() {
        markDeskAsWaitingGame();
        // TODO 重新new还是直接调用原来的reset?
        try {
            Class<?> cls = getClass().getClassLoader().loadClass(deskConf.gameClassFullName);
            game = (Game) cls.newInstance();
            game.setDesk(this, deskConf.gameParam);
        } catch (Exception e) {
            logger.error("act=reset;error=exception;deskId=" + getDeskID(), e);
        }
        this.deskConf = getParent().getParent().getMatchConfig().conditionInfo.deskConf;
    }

    protected void markDeskAsWaitingGame() {
        this.status = DeskStatus.WATING;
        this.waitingGameStartTime = System.currentTimeMillis();
        for (PlayerInfo p : this.guard.getPlayerList()) {
            BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
            session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.NEXT_GAME_UNREADY);
        }
    }

    // class GameMsgJob extends DelayJob {
    // public GameMsgEvent event;
    //
    // public GameMsgJob(GameMsgEvent event) {
    // super(1, 0, TimeUnit.MILLISECONDS);
    // this.event = event;
    // }
    // @Override
    // public void run() {
    // GameMsgEvent e = (GameMsgEvent)event;
    // gameMsgRecieve(e.playerId, e.content);
    // }
    // }

    @Override
    public void check() {
        JSONObject json = new JSONObject();
        json.put("deskId", id);
        json.put("roomId", room.getRoomId());
        json.put("deskStatus", status);
        json.put("delay", delayTimerCount + "/" + invokeTimerCount);

        JSONArray players = new JSONArray();
        json.put("players", players);
        for (PlayerInfo p : guard.getPlayerList()) {
            JSONObject player = new JSONObject();
            player.put("playerId", p.playerId);
            player.put("name", player.names());
            player.put("position", p.position);
            BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
            if (session != null) {
                player.put("status", session.getStatus());
            }
            players.add(player);
        }

        logger.info("\r\n" + new Gson().toJson(json));
    }

    @Override
    public void onPlayerReconnectPacketReceived(int playerId) {
        PlayReconnectEvent e = new PlayReconnectEvent();
        e.playerId = playerId;
        e.key = DeskEventKey.PLAYER_RECONNECT;
        this.otherEventQueue.add(e);
    }

    @Override
    public void onPlayerComeBackPacketReceived(int playerId) {
        PlayComBackEvent e = new PlayComBackEvent();
        e.playerId = playerId;
        e.key = DeskEventKey.PLAYER_COMBACK;
        this.otherEventQueue.add(e);
    }

    @Override
    public void onPlayerExitPacketReceived(int playerId) {
        PlayExitEvent e = new PlayExitEvent();
        e.playerId = playerId;
        e.key = DeskEventKey.PLAYER_EXIT;
        this.otherEventQueue.add(e);
    }

    @Override
    public void onPlayerAwayPacketReceived(int playerId) {
        PlayAwayEvent e = new PlayAwayEvent();
        e.playerId = playerId;
        e.key = DeskEventKey.PLAYER_AWAY;
        this.otherEventQueue.add(e);
    }

    @Override
    public void onPlayerOfflinePacketReceived(int playerId) {
        PlayerOfflineEvent e = new PlayerOfflineEvent();
        e.playerId = playerId;
        e.key = DeskEventKey.PLAYER_OFFLINE;
        this.otherEventQueue.add(e);
    }

    private void playerOffline(int playerId) {
        logger.info("act=playerOffline;playerId={};deskId={};", playerId, getDeskID());

        // 玩家A掉线
        PlayerInfo player = this.guard.getPlayerById(playerId);
        if (player == null) {
            logger.info("act=playerOffline;error=playerMiss;playerId={};deskId={};", playerId, getDeskID());
            return;
        }

        // 是否需要告知其他人玩家A已掉线
        if (isNeedPushPlayerOfflineMsg()) {
            for (PlayerInfo p : this.guard.getPlayerList()) {
                if (p != player) {
                    PushService.instance.pushPlayerOfflineSyn(player.position, player.playerId, p.playerId);
                }
            }
        }

        // 告知游戏模块玩家已掉线
        this.game.playerOffline(player);

        BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
        session.onlineStatus = OnlineStatus.OFFLINE;
    }

    /**
     * 是否需要告知其他人玩家A已掉线
     *
     * @return
     */
    private boolean isNeedPushPlayerOfflineMsg() {
        // 如果是游戏中
        // 游戏没有开始直接当离开房间处理
        if (this.getStatus() == DeskStatus.GAMING && deskConf.synPlayerOfflineAndReconnect) {
            return true;
        }
        return true;
    }

    private boolean isNeedPushPlayerReconnectMsg() {
        // 如果是游戏中
        // 游戏没有开始直接当离开房间处理
        if (this.getStatus() == DeskStatus.GAMING && deskConf.synPlayerOfflineAndReconnect) {
            return true;
        }
        return true;
    }

    private void playerReconnect(int playerId) {
        logger.info("act=playerReconnect;playerId={};deskId={};", playerId, getDeskID());
        PlayerInfo player = this.guard.getPlayerById(playerId);
        if (player == null) {
            logger.error("act=playerReconnect;error=playerMissById;playerId={};deskId={};", playerId, getDeskID());
            return;
        }

        BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
        if (session != null) {
            session.onlineStatus = OnlineStatus.ONLINE;
            session.awayStatus = AwayStatus.BACK;
        }

        // 向重连玩家A推送赛场、房间、桌子消息(已废弃，都是空方法,没有推消息)
        PushService.instance.pushMatchInfo(playerId, this.getParent().getParent().getId(), "");
        PushService.instance.pushRoomInfo(playerId, this.getParent().getRoomId(), "");
        PushService.instance.pushDeskInfo(playerId, this.getDeskID(), this.getPlayerCount(), "");

        // 向重连玩家A推送正在游戏的消息(推送赛场、房间、桌子消息)
        int wanfa = this instanceof MJDesk ? ((MJDesk) this).getWanfa() : 0; // TODO
        // 不好的设计
        int roomType = this instanceof MJDesk ? ((MJDesk) this).getRoomType() : 0; // TODO
        // 不好的设计
        int totalQuan = this instanceof MJDesk ? ((MJDesk) this).getTotalQuan() : 0; // TODO
        // 不好的设计
        PushService.instance.pushPlayerGamingInfo(playerId, this.getParent().getParent().getParent().getId(), this.getParent().getParent().getId(), this.getParent().getRoomId(),
                this.getDeskID() + "", wanfa, roomType, totalQuan);

        // 向重连玩家A推送A入场坐下的信息
        PushService.instance.pushPlayerSitSyn(playerId, playerId);

        // 向重连玩家A推送所有人(除自己,即B、C、D)的入场坐下信息
        for (PlayerInfo p : guard.getPlayerList()) {
            if (p != player) {
                PushService.instance.pushPlayerSitSyn(p.playerId, player.playerId);
            }
        }
        // 向重连玩家A推送游戏开始消息
        PushService.instance.pushGameStartMsg(playerId, this.getDeskID());

        // 向其他人推送A已重连的消息
        if (isNeedPushPlayerReconnectMsg()) {
            for (PlayerInfo p : this.guard.getPlayerList()) {
                if (p != player) {
                    PushService.instance.pushPlayerReconnectSyn(player.position, player.playerId, p.playerId);
                }
            }
        }

        // 通知游戏模块玩家重连(重发牌局数据)
        this.game.playerReconnect(player);

        // 如果游戏暂停，尝试恢复游戏
        if (this.status == DeskStatus.GAMING_PAUSE) {
            this.tryResumeGame();
        }
    }

    // 恢复已暂停的游戏
    protected void tryResumeGame() {
        if (this.getStatus() == DeskStatus.GAMING) {
            return;
        }

        for (int playerId : guard.getplayerIdList()) {
            BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
            if (session.getStatus() != PlayerStatus.GAMING) {
                return;
            }
        }
        this.status = DeskStatus.GAMING;

        for (int playerId : guard.getplayerIdList()) {
            PushService.instance.pushGameResumeSyn(this.id, playerId);
        }
    }

    private void playerComeBack(int playerId) {
        logger.info("act=playeyComeBack;playerId={};deskId={};", playerId, getDeskID());
        PlayerInfo player = this.guard.getPlayerById(playerId);
        if (player == null) {
            logger.error("act=playeyComeBack;error=playerMissById;playerId={};deskId={};", playerId, getDeskID());
            return;
        }

        BattleSession session = ServiceRepo.sessionManager.getIoSession(playerId);
        if (session != null) {
            session.awayStatus = AwayStatus.BACK;
            session.onlineStatus = OnlineStatus.ONLINE;
        }

        // 向重连玩家A推送赛场、房间、桌子消息(已废弃，都是空方法,没有推消息)
        PushService.instance.pushMatchInfo(playerId, this.getParent().getParent().getId(), "");
        PushService.instance.pushRoomInfo(playerId, this.getParent().getRoomId(), "");
        PushService.instance.pushDeskInfo(playerId, this.getDeskID(), this.getPlayerCount(), "");

        // 向重连玩家A推送正在游戏的消息(推送赛场、房间、桌子消息)
        int wanfa = this instanceof MJDesk ? ((MJDesk) this).getWanfa() : 0; // TODO
        // 不好的设计
        int roomType = this instanceof MJDesk ? ((MJDesk) this).getRoomType() : 0; // TODO
        // 不好的设计
        int totalQuan = this instanceof MJDesk ? ((MJDesk) this).getTotalQuan() : 0; // TODO
        // 不好的设计
        PushService.instance.pushPlayerGamingInfo(playerId, this.getParent().getParent().getParent().getId(), this.getParent().getParent().getId(), this.getParent().getRoomId(),
                this.getDeskID() + "", wanfa, roomType, totalQuan);

        // 向重连玩家A推送A入场坐下的信息
        PushService.instance.pushPlayerSitSyn(playerId, playerId);

        // 向重连玩家A推送所有人(除自己,即B、C、D)的入场坐下信息
        for (PlayerInfo p : guard.getPlayerList()) {
            if (p != player) {
                PushService.instance.pushPlayerSitSyn(p.playerId, player.playerId);
            }
        }
        // 向重连玩家A推送游戏开始消息
        PushService.instance.pushGameStartMsg(playerId, this.getDeskID());

        // 向其他人推送A已重连的消息
        if (isNeedPushPlayerReconnectMsg()) {
            for (PlayerInfo p : this.guard.getPlayerList()) {
                if (p != player) {
                    PushService.instance.pushPlayerComebackSyn(player.position, player.playerId, p.playerId);
                }
            }
        }

        // 通知游戏模块玩家重连(重发牌局数据)
        this.game.playerComeBack(player);

        // 如果游戏暂停，尝试恢复游戏
        if (this.status == DeskStatus.GAMING_PAUSE) {
            this.tryResumeGame();
        }
    }

    @Override
    public List<PlayerInfo> getPlayers() {
        return guard.getPlayerList();
    }

    @Override
    public DeskConfig getDeskConfig() {
        return this.deskConf;
    }

    @Override
    public void setDeskConfig(DeskConfig conf) {
        this.deskConf = conf;
        if (this.game != null) {
            this.game.setGameParam(this.deskConf.gameParam);
        }
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    @Override
    public double getFee() {
        return this.getParent().getRoomConfig().fee.get(0).currenceCount;
    }

    @Override
    public int getDeskOwner() {
        return this.ownerId;
    }

    @Override
    public void setDeskOwner(int ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public double getDeskDelayStatus() {
        if (invokeTimerCount == 0) {
            return 0;
        }
        double f = delayTimerCount * 100 / invokeTimerCount;
        BigDecimal bd = new BigDecimal(f);
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void onDismissPacketRequest() {
        DismissEvent event = new DismissEvent();
        event.key = DeskEventKey.DISMISS;
        otherEventQueue.add(event);
    }

    @Override
    public void setDeskInValid() {
        errCount++;
    }

    @Override
    public boolean isAutoReady() {
        return this.deskConf.autoReady;
    }

    @Override
    public String getReplyData() {
        return replayData;
    }

    public void setReplayData(String historyData) {
        this.replayData = historyData;
    }

    @Override
    public void markAsAdminUse() {
        this.adminDesk = true;

        loadReplayData();
    }

    private void loadReplayData() {
        logger.info("act=loadReplayData;deskId={};", getDeskID());
        try {
            byte data[] = IOUtil.tryGetFileData("/home/game/data/replay.json");
            if (data != null) {
                String json = new String(data, "UTF-8");
                this.setReplayData(json);
            }
        } catch (Exception e) {
            logger.error("act=loadReplayData;error=exception;", e);
        }
    }

    @Override
    public boolean isAdminUse() {
        return adminDesk;
    }

    @Override
    public List<PlayerInfo> loopGetPlayer(int pos, int count, int includePos) {
        List<PlayerInfo> list = new LinkedList<PlayerInfo>();
        int maxInd = getDeskConfig().seatSizeUpper;
        int fromInd = (pos + 1) % maxInd;
        while (fromInd != pos) {
            PlayerInfo p = guard.getPlayerByPos(fromInd);
            if (p != null) {
                list.add(p);
                if (count > 0 && list.size() >= count) {
                    break;
                }
            }
            fromInd++;
            fromInd = (fromInd) % maxInd;
        }
        if (includePos == 1) {
            PlayerInfo p = guard.getPlayerByPos(pos);
            if (p != null) {
                list.add(p);
            }
        }
        if (includePos == 2) {
            PlayerInfo p = guard.getPlayerByPos(pos);
            if (p != null) {
                list.add(0, p);
            }
        }
        return list;
    }

    @Override
    public void ready4NextGame(GameContext context) {
        for (PlayerInfo p : this.guard.getPlayerList()) {
            BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
            session.setStatus(PlayerStatus.UNREADY, StatusChangeReason.NEXT_GAME_UNREADY);
        }
        guard.ready4NextGame();
    }

    @Override
    public void startNextGame(GameContext context) {
        this.game.setDesk(this, deskConf.gameParam);
        this.status = DeskStatus.WATING;
        this.waitingGameStartTime = System.currentTimeMillis();
    }

    @Override
    public List<Integer> getDebugData(int pos) {
        PlayerInfo p = getDeskPlayer(pos);
        BattleSession session = ServiceRepo.sessionManager.getIoSession(p.playerId);
        return session.debugData == null ? new ArrayList<Integer>() : session.debugData;
    }

    @Override
    public void sendErrorMsg(int position, String msg) {
        try {
            PlayerInfo player = guard.getPlayerByPos(position);
            PushService.instance.pushGlobalErrorSyn(player.playerId, msg);
        } catch (Exception e) {
            logger.info("act=sendErrorMsg;error=exception;", e);
        }
    }

    @Override
    public void log(LogLevel level, String msg, int position) {
        msg += ";position=" + position + ";deskId=" + getDeskID() + ";matchId=" + matchId + ";roomId=" + roomId;
        switch (level) {
            case DEBUG:
                logger.debug(msg);
                break;
            case INFO:
                logger.info(msg);
                break;
            case ERROR:
                logger.error(msg);
                break;
            default:
                break;
        }

    }

    @Override
    public void setDeskId(String id) {
        this.id = id;
    }

    @Override
    public void handSettle(GameContext context) {
        dumpGameData();

        // 更新胜败记录和排行榜信息
        for (PlayHandResult item : context.playerHandResults.playDetail) {
            if (item.playerId > 0) {
                settleInternal(item);
            }
        }

        addGameLog(context);
    }

    @Override
    public boolean hasNextGame(GameContext context) {
        return false;
    }

    @Override
    public int getGameCount() {
        return gameCount;
    }

    @Override
    public int getPlayerActionTimeOut(int act) {
        return this.deskConf.gameOperTimeOut;
    }

    @Override
    public DeskModel getDeskInfo() {
        Room room = this.getParent();
        Match match = room.getParent();
        com.buding.battle.logic.module.game.Game game = match.getParent();
        DeskModel model = new DeskModel();
        model.deskId = getDeskID();
        model.gameId = game.getId();
        model.gameName = game.getName();
        model.matchId = match.getId();
        model.matchName = match.getName();
        model.roomId = room.getRoomId();
        model.roomName = room.getRoomConfig().roomName;
        model.deskStatus = this.status.toString();
        model.gameCount = this.gameCount;
        model.createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(createTime));
        model.players.addAll(guard.getPlayerList());
        return model;
    }

    @Override
    public void onSetGamingDataReq(String json) {
        this.game.setGamingDate(json);
    }

    @Override
    public String printGameDetail() {
        return null;
    }

    @Override
    public boolean isHasPlayer(int playerId) {
        for (int pid : guard.getplayerIdList()) {
            if (pid == playerId) {
                return true;
            }
        }
        return false;
    }
}
