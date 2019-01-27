package com.buding.battle.logic.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import packet.game.MsgGame.CreateVipRoomRequest;
import packet.game.MsgGame.DismissVipRoomRequest;
import packet.game.MsgGame.DissmissVoteSyn;
import packet.game.MsgGame.EnrollRequest;
import packet.game.MsgGame.GameChatMsgRequest;
import packet.game.MsgGame.KickPlayerRequest;
import packet.game.MsgGame.PlayerGamingSynInquire;
import packet.mj.MJ;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;
import packet.user.User.AuthRequest;

import com.buding.battle.common.network.Invoker;
import com.buding.battle.common.network.session.BattleSession;
import com.buding.battle.common.network.session.BattleSessionManager;
import com.buding.battle.logic.module.common.AwayStatus;
import com.buding.battle.logic.module.common.BattleContext;
import com.buding.battle.logic.module.common.PlayerStatus;
import com.buding.battle.logic.module.common.PushService;
import com.buding.battle.logic.module.desk.bo.CommonDesk;
import com.buding.battle.logic.module.desk.bo.PlayerExitType;
import com.buding.battle.logic.module.game.service.GameService;
import com.buding.battle.logic.module.game.service.VipService;
import com.buding.battle.logic.module.match.Match;
import com.buding.battle.logic.module.user.service.LoginService;
import com.buding.battle.logic.network.module.Module;
import com.buding.common.result.Result;
import com.google.gson.Gson;

public class BaseModule implements Module<PacketType, PacketBase>, InitializingBean {
	protected Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	protected BattleSessionManager sessionManager;

	@Autowired
	GameService matchService;

	@Autowired
	GameService gameService;

	@Autowired
	PushService pushService;

	@Autowired
	LoginService loginService;

	@Autowired
	VipService vipService;

	protected Map<PacketType, Invoker<PacketBase>> cmdInvokerMap = new HashMap<>();

	public void handleMsg(ChannelHandlerContext ctx, BattleSession session, PacketType key, PacketBase packet) throws Exception {
		long startTime = System.currentTimeMillis();
		Invoker<PacketBase> invoker = cmdInvokerMap.get(key);
		if (invoker == null) {
			onUnRecognizeMsgReceived(session, key.toString(), packet);
		} else {
			invoker.invoke(session, packet);
		}
		if(key != PacketType.HEARTBEAT) {
			LOG.info("type={};wait={};execute={}", key, -1, System.currentTimeMillis() - startTime);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.cmdInvokerMap.put(PacketType.AuthRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onAuthMsgReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.EnrollRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onEnrollMsgReceived(session, msg);
			}
		});

		// this.cmdInvokerMap.put("PB_Room_Enter_Room_Request", new
		// Invoker<PacketBase>() {
		// @Override
		// public void invoke(BattleSession session, PacketBase msg) throws
		// Exception {
		// onEnterRoomMsgReceived(session, msg);
		// }
		// });

		this.cmdInvokerMap.put(PacketType.ReadyRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onDeskReadyMsgReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.GameOperation, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onGamePacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.ChangeDeskRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onChangeDeskMsgReceived(session, msg);
			}
		});

		// this.cmdInvokerMap.put("PB_QuickStart_Request", new
		// Invoker<PacketBase>() {
		// @Override
		// public void invoke(BattleSession session, PacketBase msg) throws
		// Exception {
		// onQuickStartMsgReceived(session, msg);
		// }
		// });

		this.cmdInvokerMap.put(PacketType.Back2HallRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onBack2HallReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.KickPlayerRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onKickoutPlayerReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.HEARTBEAT, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onHeatbeatReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.Dump, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onDumpGamePacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.CreateVipRoomRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onCreateVipPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.EnterVipRoomRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onEnterVipPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.VipRoomListReuqest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onVipRoomListPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.DismissVipRoomRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onDismissVipRoomPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.ExitGameRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerExitPacketReceived(session, msg);
			}
		});
		
		this.cmdInvokerMap.put(PacketType.AwayGameRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerAwayPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.GameChatMsgRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onGameChatMsgPacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.HangupRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onHangupRequestPacketReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.CancelHangupRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onCancelHangupRequestPacketReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.DissmissVoteSyn, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onDismissVoteRequestPacketReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.PlayerGamingSynInquire, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerGamingSynReqPacketReceived(session, msg);
			}
		});
		this.cmdInvokerMap.put(PacketType.BackGameRequest, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerBackGamePacketReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.MaiPiaoSyn, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerMaiPiaoReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.OpenBestCard, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerSeeBestCardReceived(session, msg);
			}
		});

		this.cmdInvokerMap.put(PacketType.ChangeBestCard, new Invoker<PacketBase>() {
			@Override
			public void invoke(BattleSession session, PacketBase msg) throws Exception {
				onPlayerChangeBestCardReceived(session, msg);
			}
		});
	}

	public void onHangupRequestPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		CommonDesk desk = session.getDesk();
		if (desk != null) {
			desk.onPlayerHangupPacketReceived(session.userId);
		}
	}

	public void onPlayerGamingSynReqPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		PlayerGamingSynInquire.Builder pb =  PlayerGamingSynInquire.newBuilder();
		if (session != null && session.getDesk() != null) {
			if(session.awayStatus == AwayStatus.AWAY) {
				session.getDesk().onPlayerComeBackPacketReceived(session.userId);
			} else {
				session.getDesk().onPlayerReconnectPacketReceived(session.userId);
			}
			pb.setIsGaming(true);
		} else {
			pb.setIsGaming(false);
		}
		pushService.pushPBMsg(session, PacketType.PlayerGamingSynInquire, pb.build().toByteString());
	}
	
	public void onPlayerBackGamePacketReceived(BattleSession session, PacketBase packet) throws Exception {
		if (session != null && session.getDesk() != null) {
			session.getDesk().onPlayerComeBackPacketReceived(session.userId);
		}
	}


	public void onDismissVoteRequestPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		CommonDesk desk = session.getDesk();
		if (desk != null) {
			DissmissVoteSyn req = DissmissVoteSyn.parseFrom(packet.getData());
			desk.onPlayerDissVotePacketReceived(session.userId, req.getAgree());
		}
	}

	public void onCancelHangupRequestPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		CommonDesk desk = session.getDesk();
		if (desk != null) {
			desk.onPlayerCancelHangupPacketReceived(session.userId);
		}
	}

	public void onGameChatMsgPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		CommonDesk desk = session.getDesk();
		if (desk != null) {
			GameChatMsgRequest req = GameChatMsgRequest.parseFrom(packet.getData());
			desk.onChatMsgPacketReceived(session.getPlayerId(), req.getContentType(), req.getContent().toByteArray());
		}
	}

	public void onVipRoomListPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		vipService.pushVipRoomList(session.userId);
	}

	public void onDismissVipRoomPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		DismissVipRoomRequest req = DismissVipRoomRequest.parseFrom(packet.getData());
		vipService.dissmissVipRoom(session.userId, req.getCode());
	}

	public void onPlayerExitPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		CommonDesk desk = session.getDesk();
		if (desk != null) {
			desk.onPlayerExitPacketReceived(session.userId);
		}
	}
	
	public void onPlayerAwayPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		CommonDesk desk = session.getDesk();
		if (desk != null) {
			desk.onPlayerAwayPacketReceived(session.userId);
		}
	}

	public void onKickoutPlayerReceived(BattleSession session, PacketBase packet) throws Exception {
		KickPlayerRequest req = KickPlayerRequest.parseFrom(packet.getData());
		String deskCode = req.getCode();
		int playerId = req.getPlayerId();

		vipService.kick(session.userId, playerId, deskCode);
	}

	public void onHeatbeatReceived(BattleSession session, PacketBase packet) throws Exception {
//		PacketBase.Builder pb = PacketBase.newBuilder();
//		pb.setCode(0);
//		pb.setPacketType(packet.getPacketType());
		// sessionManager.write(session, pb.build().toByteArray());
	}

	public void onBack2HallReceived(BattleSession session, PacketBase packet) {
		if (session.getStatus() == PlayerStatus.GAMING) {
			pushService.pushErrorMsg(session, PacketType.Back2HallRequest, "游戏已开始,不能退出");
			return;
		}
		//
		int playerId = session.userId;
		Match match = session.getMatch();

		if (match != null) {
			match.playerExit(playerId, PlayerExitType.REQUEST_EXIT);
		}
		//
		// pushService.pushBack2HallRsp(session.userId, BaseRsp.success());
	}

	public void onDumpGamePacketReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onCreateVipPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		CreateVipRoomRequest request = CreateVipRoomRequest.parseFrom(packet.getData());
		vipService.createVipRoom(session.userId, request);
	}

	public void onEnterVipPacketReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onGamePacketReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onDeskReadyMsgReceived(BattleSession session, PacketBase packet) {
		gameService.requestReady(session);
	}

	public void onChangeDeskMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onQuickStartMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onEnterRoomMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		onUnRecognizeMsgReceived(session, packet.getPacketType().toString(), packet);
	}

	public void onEnrollMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		EnrollRequest enrollReq = EnrollRequest.parseFrom(packet.getData());
		String gameId = enrollReq.getGameId();
		String matchId = enrollReq.getMatchId();

		session.debugData = enrollReq.getCardsList();

		String roomCode = enrollReq.getRoomCode();

		Result ret = null;
		if (StringUtils.isNotBlank(roomCode)) {
			ret = vipService.enroll(session, roomCode);
		} else {
			ret = gameService.enroll(session, BattleContext.create(session).setGameId(gameId).setMatchId(matchId));
		}

		// 2 代表已经在游戏中
		pushService.pushEnrollRsp(session.userId, ret.isOk() || ret.code == 2, ret.msg);

		if (ret.code == 2) {
			return;
		}

		// 如果是配置了自动准备，则直接准备
		if (ret.isOk() && session.getDesk().isAutoReady()) {
			session.getDesk().onPlayerReadyPacketReceived(session.userId);
		}
	}

	public void onAuthMsgReceived(BattleSession session, PacketBase packet) throws Exception {
		AuthRequest authReq = AuthRequest.parseFrom(packet.getData());
		if (StringUtils.isNotBlank(authReq.getLocation()) ){
			session.setLocation(authReq.getLocation());
		}
		loginService.auth(session, authReq.getUserId(), authReq.getToken());
	}

	public void onPlayerMaiPiaoReceived(BattleSession session, PacketBase packet) throws Exception {
		MJ.MaiPiaoSyn data = MJ.MaiPiaoSyn.parseFrom(packet.getData());
		if (session != null && session.getDesk() != null) {
			session.getDesk().onPlayerMaiPiaoReceived(data,session);
		}
	}

	public void onPlayerSeeBestCardReceived(BattleSession session, PacketBase packet) throws Exception {
		if (session != null && session.getDesk() != null) {
			session.getDesk().onPlayerSeeBestCardReceived(session);
		}
	}

	public void onPlayerChangeBestCardReceived(BattleSession session, PacketBase packet) throws Exception {
		MJ.ChangeBestCards data =  MJ.ChangeBestCards.parseFrom(packet.getData());
		if (session != null && session.getDesk() != null) {
			session.getDesk().onPlayerChangeBestCardReceived(session,data);
		}
	}

	protected void writeResponse(BattleSession session, String key, Object obj) {
		JSONObject json = new JSONObject();
		json.put(key, new Gson().toJson(obj));
		String txt = new Gson().toJson(json);
		session.channel.writeAndFlush(new TextWebSocketFrame(txt));
	}

	/**
	 * 处理无法识别的命令
	 * 
	 * @param packet
	 */
	public void onUnRecognizeMsgReceived(BattleSession session, String key, PacketBase packet) {
		// TODO
		LOG.error("ErrCmd:{}, currentModule:{},userId:{}", key, session.currentModule.getClass(), session.user.getId());
		PacketBase.Builder pb = PacketBase.newBuilder();
		pb.setCode(-1);
		pb.setPacketType(packet.getPacketType());
		pb.setMsg("无法处理该命令:" + packet.getPacketType());
		sessionManager.write(session, pb.build().toByteArray());

		// GlobalNotify notify = new GlobalNotify();
		// notify.msg = "无法处理的命令:" + key;
		// sessionManager.writeTextWebSocketFrame(session, "PB_Global_Notify",
		// notify);
	}

	// /**
	// * 收到登录命令
	// * @param ctx
	// * @param e
	// * @param packet
	// */
	// public void onLoginMsgReceived(ChannelHandlerContext ctx, Session
	// session, PB_Packet packet) throws Exception {
	// onUnRecognizeMsgReceived(ctx, session, packet);
	// }
	//
	// /**
	// * 收到报名命令
	// * @param ctx
	// * @param e
	// * @param packet
	// */
	// public void onEnrollMsgReceived(ChannelHandlerContext ctx, Session
	// session, PB_Packet packet) throws Exception {
	// onUnRecognizeMsgReceived(ctx, session, packet);
	// }
	//
	// /**
	// * 进入房间
	// * @param ctx
	// * @param e
	// * @param session
	// * @param packet
	// */
	// public void onEnterRoomMsgReceived(ChannelHandlerContext ctx, Session
	// session, PB_Packet packet) throws Exception {
	// onUnRecognizeMsgReceived(ctx, session, packet);
	// }
	//
	// /**
	// * 收到退出游戏返回大厅命令
	// * @param ctx
	// * @param e
	// * @param packet
	// */
	// public void onBack2HallReceived(ChannelHandlerContext ctx, Session
	// session, PB_Packet packet) throws Exception {
	// onUnRecognizeMsgReceived(ctx, session, packet);
	// }
	//
	// /**
	// * 收到换桌命令
	// * @param ctx
	// * @param e
	// * @param packet
	// */
	// public void onChangeDeskMsgReceived(ChannelHandlerContext ctx, Session
	// session, PB_Packet packet) throws Exception {
	// onUnRecognizeMsgReceived(ctx, session, packet);
	// }
	//
	// /**
	// * 收到准备命令
	// * @param ctx
	// * @param e
	// * @param session
	// * @param packet
	// */
	// public void onDeskReadyMsgReceived(ChannelHandlerContext ctx, Session
	// session, PB_Packet packet) throws Exception {
	// onUnRecognizeMsgReceived(ctx, session, packet);
	// }
	//
	// /**
	// * 收到游戏数据包
	// * @param ctx
	// * @param e
	// * @param session
	// * @param packet
	// */
	// public void onGamePacketReceived(ChannelHandlerContext ctx, Session
	// session, PB_Packet packet) throws Exception {
	// onUnRecognizeMsgReceived(ctx, session, packet);
	// }
}
