package com.buding.battle.logic.module.game.service;

import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import packet.game.MsgGame;
import packet.game.MsgGame.PlayerModel;
import packet.game.MsgGame.VipRoomListSyn;
import packet.game.MsgGame.VipRoomModel;

import com.buding.api.player.PlayerInfo;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.DeskStatus;
import com.buding.battle.logic.module.common.PushService;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.game.Game;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.room.bo.Room;
import com.buding.common.result.Result;
import com.buding.common.result.TResult;
import com.buding.db.model.User;
import com.buding.db.model.UserRoom;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.DeskFee;
import com.buding.hall.config.MatchConfig;
import com.buding.hall.config.RoomConfig;
import com.buding.hall.module.common.constants.CurrencyType;
import com.buding.hall.module.common.constants.RoomState;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.user.type.UserRole;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.buding.hall.module.ws.HallPortalService;

@Component
public class VipService {
	@Autowired
	GameService gameService;

	@Autowired
	UserRoomDao userRoomDao;

	@Autowired
	PushService pushService;

	@Autowired
	HallPortalService hallService;

	@Autowired
	ConfigManager configManager;
	
	@Autowired
	BattleSessionManager battleSessionManager;

	public Result enroll(BattleSession session, String roomCode) {
		UserRoom ur = userRoomDao.getByCode(roomCode);
		if(ur == null) {
			return Result.fail("房间不存在");
		}
		Result ret = gameService.enroll(session, BattleContext.create(session).setGameId(ur.getGameId()).setMatchId(ur.getMatchId()).setDeskId(roomCode));
		return ret;
	}
	
	public void createVipRoom(int playerId, MsgGame.CreateVipRoomRequest request) {
		int quanNum = request.getQuanNum();
		int wanfa = request.getWangfa();
		int vipRoomType = request.getVipRoomType();
		int pay = request.getPay();
		int score = request.getScore();
		String matchId = null;
		if (vipRoomType == 2) {
			matchId = "G_DQMJ_MATCH_2VIP";
		} else {
			matchId = "G_DQMJ_MATCH_4VIP";
		}


		User user = hallService.getUser(playerId);
		if (user == null) {
			pushService.pushCreateVipRoomRsp(playerId, false, "用户不存在");
			return;
		}

		int c = userRoomDao.getMyRoomListCount(playerId);
		boolean canOpenMulti = user.getRole() != null && (user.getRole() & UserRole.USER_ROLE_AUTH) == UserRole.USER_ROLE_AUTH;
		if (!canOpenMulti && c >= 1) {
			pushService.pushCreateVipRoomRsp(playerId, false, "房间数量已达上限");
			return;
		}

		MatchConfig matchConf = configManager.getMatchConfById(matchId);
		RoomConfig roomConf = matchConf.conditionInfo.roomArray[0];
		DeskFee fee = null;
		for (DeskFee df : roomConf.fee) {
			if (df.itemCount == quanNum) {
				fee = df;
				break;
			}
		}
		if (fee == null) {
			pushService.pushCreateVipRoomRsp(playerId, false, "quanNum参数不对:" + quanNum);
			return;
		}
		Result r = hallService.hasEnoughCurrency(playerId, CurrencyType.fanka, fee.itemCount);
		if (r.isFail()) {
			pushService.pushCreateVipRoomRsp(playerId, false, "开房卡不足");
			return;
		}

		UserRoom room = new UserRoom();
		room.setMatchId(matchId);
		room.setGameId(matchConf.game.gameId);
		String code = hallService.genRoomUniqCode();
		if (code == null) {
			pushService.pushCreateVipRoomRsp(playerId, false, "生成房间编号失败");
			return;
		}
		room.setRoomCode(code); // 编号系统生成
		JSONObject json = new JSONObject();
		json.put("quanNum", fee.gameCount);
		json.put("vipRoomType", vipRoomType);
		json.put("fee", fee.itemCount);
		json.put("pay", pay);
		json.put("bottomNote", score);
		room.setParams(json.toString());
		room.setOwnerId(playerId);
		room.setWanfa(wanfa + "");
		room.setRoomName(code);

		initRoom(null, room);
		room.setRoomState(1);
		Result result = hallService.addRoom(room);
		pushService.pushCreateVipRoomRsp(playerId, result.isOk(), result.msg);
		if(result.isOk()) {
			pushVipRoomList(playerId);
		}
		
		//扣除房卡
		hallService.changeFangka(playerId, -1*fee.itemCount, false, ItemChangeReason.CREATE_ROOM);
	}

	public TResult<UserRoom> initRoom(String roomConfigId, UserRoom room) {
		MatchConfig conf = configManager.getMatchConfById(room.getMatchId());
		if (conf == null) {
			return TResult.fail1("赛场配置不存在");
		}

		room.setCtime(new Date());
		room.setMtime(new Date());

		RoomConfig rc = null;
		if (roomConfigId != null) {
			rc = conf.conditionInfo.roomConf.get(roomConfigId);
		} else {
			rc = conf.conditionInfo.roomArray[0];
		}

		room.setRoomConfId(rc.roomId);
		room.setRoomState(RoomState.ACTIVE);
		return TResult.sucess1(room);
	}
	
	public void kick(int playerId, int tokickPlayerId, String roomCode) {
		UserRoom ur = userRoomDao.getByCode(roomCode);
		if (ur == null) {
			pushService.pushDismissVipRoomResponse(playerId, false, "房间不存在");
			return;
		}
		if(ur.getOwnerId() != playerId) {
			pushService.pushDismissVipRoomResponse(playerId, false, "你不是房主");
			return;
		}
		Game game = gameService.getById(ur.getGameId());
		Match m = game.getMatch(ur.getMatchId());
		Room room = m.getRoom(ur.getRoomConfId());
		CommonDesk<?> desk = room.getById(ur.getRoomCode());
		if (desk.getStatus() == DeskStatus.GAMING) {
			pushService.pushDismissVipRoomResponse(playerId, false, "房间已开始游戏，无法踢人");
			return;
		}
		desk.onKickoutPacketReceived(playerId, tokickPlayerId);
	}
	
	public void dissmissVipRoom(int playerId, String roomCode) {
		UserRoom ur = userRoomDao.getByCode(roomCode);
		if (ur == null) {
			pushService.pushDismissVipRoomResponse(playerId, false, "房间不存在");
			return;
		}
		if(ur.getOwnerId() != playerId) {
			pushService.pushDismissVipRoomResponse(playerId, false, "你不是房主");
			return;
		}
		Game game = gameService.getById(ur.getGameId());
		Match m = game.getMatch(ur.getMatchId());
		Room room = m.getRoom(ur.getRoomConfId());
		CommonDesk<?> desk = room.getById(ur.getRoomCode());
		if (desk != null) {
			if (desk.getStatus() == DeskStatus.GAMING) {
				pushService.pushDismissVipRoomResponse(playerId, false, "房间已开始游戏，无法销毁");
				return;
			}
			desk.onDismissPacketRequest();
		}
		ur.setRoomState(RoomState.CLOSE);
		this.userRoomDao.updateUserRoom(ur);
		pushVipRoomList(playerId);
	}
	
	public void pushVipRoomList(int playerId) {
		BattleSession session = battleSessionManager.getIoSession(playerId);
		if(session == null) {
			return;
		}
		VipRoomListSyn.Builder vb = VipRoomListSyn.newBuilder();
		List<UserRoom> roomList = userRoomDao.getMyRoomList(playerId);
		for (UserRoom ur : roomList) {
			if(ur.getRoomState() != RoomState.ACTIVE) {
				continue;
			}
			JSONObject obj = JSONObject.fromObject(ur.getParams());
			int vipRoomType = obj.getInt("vipRoomType");
			int quanNum = obj.getInt("quanNum");
			int pay = obj.getInt("pay");
			int bottomNote = obj.getInt("bottomNote");
			Game game = gameService.getById(ur.getGameId());
			Match m = game.getMatch(ur.getMatchId());
			Room room = m.getRoom(ur.getRoomConfId());
			CommonDesk<?> desk = room.getById(ur.getRoomCode());
			VipRoomModel.Builder model = VipRoomModel.newBuilder();
			model.setCode(ur.getRoomCode());
			model.setName(ur.getRoomName());
			model.setRoomType(vipRoomType);
			model.setQuanNum(quanNum);
			model.setWangfa(Integer.valueOf(ur.getWanfa()));

			if (desk != null) {
				for (PlayerInfo p : (List<PlayerInfo>) desk.getPlayers()) {
					PlayerModel.Builder pb = PlayerModel.newBuilder();
					pb.setCoin(p.coin);
					pb.setNickName(p.name);
					pb.setOnline(1);
					pb.setPlayerId(p.playerId);
					pb.setPosition(p.position);
					pb.setState(1);
					model.addPlayers(pb);
				}
			}

			vb.addRoomList(model);
		}
		pushService.pushVipRoomListSyn(playerId, vb);
	}
}
