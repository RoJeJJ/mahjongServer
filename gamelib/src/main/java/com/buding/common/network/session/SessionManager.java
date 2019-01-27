package com.buding.common.network.session;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.buding.common.logger.LogLevel;
import com.google.gson.GsonBuilder;
import com.google.protobuf.MessageLite;

public abstract class SessionManager<T extends BaseSession> implements InitializingBean, SessionListener<T>, Runnable {
	public static final Logger log = LoggerFactory.getLogger(SessionManager.class);

	// 已登录的用户列表,key为playerId
	private final ConcurrentHashMap<Integer, T> ONLINE_PLAYERID_IOSESSION_MAP = new ConcurrentHashMap<Integer, T>();

	// 未登录的用户列表,key为sessionId
	private final ConcurrentHashMap<Integer, T> ANONYMOUS_IOSESSION_MAP = new ConcurrentHashMap<Integer, T>();

	// 已关闭的SESSSION，如果不重连将被清除
	private final ConcurrentHashMap<Integer, T> SCHEDULE_REMOVE_IOSESSION_MAP = new ConcurrentHashMap<Integer, T>();

	private int maxOnlineCount = 0;

	private int minOnlineCount = 0;

//	SessionDataWriter<byte[]> sessionWriter = new UnCompressBinaryWriter();
	public LogLevel logLevel = LogLevel.DEBUG;

	@Override
	public void afterPropertiesSet() throws Exception {
		ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
		pool.scheduleAtFixedRate(this, 10, 10, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		try {
			checkInvalidSession();
		} catch (Exception e) {
			log.error("CleanSessionError", e);
		}
	}

	public void checkInvalidSession() {
		for (int userid : SCHEDULE_REMOVE_IOSESSION_MAP.keySet()) {
			T session = SCHEDULE_REMOVE_IOSESSION_MAP.get(userid);
			if (session == null) {
				return;
			}
			if (session.isCanRemove()) {
				cleanSession(session);
				this.SCHEDULE_REMOVE_IOSESSION_MAP.remove(userid);
			}
			if (session.channel != null && session.channel.isOpen()) {
				session.channel.close();
			}
		}
	}

	public int getPlayerId(T session) {
		return session.getPlayerId();
	}

	/**
	 * 登入成功,加入在线列表
	 * 
	 * @param playerId
	 * @param session
	 */
	public void put2OnlineList(int playerId, T session) {
		if (session == null) {
			return;
		}

		session.setPlayerId(playerId);
		Integer sessionId = session.getSessionId();
		if (ANONYMOUS_IOSESSION_MAP.containsKey(sessionId)) {
			ANONYMOUS_IOSESSION_MAP.remove(sessionId);
		}

		if (SCHEDULE_REMOVE_IOSESSION_MAP.contains(playerId)) {
			T oldSession = SCHEDULE_REMOVE_IOSESSION_MAP.get(playerId);
			if (session == oldSession) {// 准备移除的会话再次上线的话不用在移除
				SCHEDULE_REMOVE_IOSESSION_MAP.remove(playerId);
			}
		}

		session.sessionStatus = SessionStatus.VALID;

		T orignSession = (T) ONLINE_PLAYERID_IOSESSION_MAP.put(playerId, session);
		if (orignSession != null && orignSession != session && (orignSession.getChannel() != null && orignSession.getChannel().isOpen())) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("玩家[%d]加入在线用户列表前关闭之前的Session", playerId));
			}
			orignSession.channel.close();
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("SessionId: [ %d ] 绑定角色ID:[ %d ] ", session.getSessionId(), playerId));
		}
		int onlineCount = getCurrentOnlineCount();
		if (this.maxOnlineCount < onlineCount) {
			this.maxOnlineCount = onlineCount;
		}
		if (log.isDebugEnabled())
			log.debug(String.format("把玩家[%d]加入在线用户列表", playerId));
	}

	/**
	 * 从在线列表中移除,比如登出
	 * 
	 * @param playerId
	 */
	public void removeFromOnlineList(Integer playerId) {
		T session = (T) ONLINE_PLAYERID_IOSESSION_MAP.remove(playerId);
		if (session == null) {
			return;
		}

		if (session.getChannel().isActive()) {
			ANONYMOUS_IOSESSION_MAP.put(session.getSessionId(), session);
		}

		int onlineCount = getCurrentOnlineCount();
		if (this.minOnlineCount > onlineCount) {
			this.minOnlineCount = onlineCount;
		}

		if (log.isDebugEnabled())
			log.debug(String.format("把玩家[%d]从在线用户列表删除", playerId));
	}

	public int getMaxOnlineCount() {
		return this.maxOnlineCount;
	}

	public void setMaxOnlineCount(int maxOnlineCount) {
		this.maxOnlineCount = maxOnlineCount;
	}

	public int getMinOnlineCount() {
		return this.minOnlineCount;
	}

	public void setMinOnlineCount(int minOnlineCount) {
		this.minOnlineCount = minOnlineCount;
	}

	public void resetOnlineUserCount() {
		int onlineCount = getCurrentOnlineCount();
		this.maxOnlineCount = onlineCount;
		this.minOnlineCount = onlineCount;
	}

	public void removeFromOnlineList(T session) {
		int playerId = getPlayerId(session);
		T oldSession = getIoSession(playerId);

		if ((oldSession != null) && (session == oldSession))
			removeFromOnlineList(playerId);
	}

	public void put2AnonymousList(T session) {
		if (session != null) {
			ANONYMOUS_IOSESSION_MAP.put(session.getSessionId(), session);
			if (log.isDebugEnabled())
				log.debug(String.format("Session [%d]加入匿名用户列表", session.getSessionId()));
		}
	}

	public void removeFromAnonymousList(int sessionId) {
		if (ANONYMOUS_IOSESSION_MAP.containsKey(sessionId)) {
			ANONYMOUS_IOSESSION_MAP.remove(sessionId);
			if (log.isDebugEnabled())
				log.debug(String.format("Session [%d]从匿名用户列表删除", sessionId));
		}
	}

	public boolean isOnline(Integer playerId) {
		return ONLINE_PLAYERID_IOSESSION_MAP.containsKey(playerId);
	}

	public Set<Integer> getOnlinePlayerIdList() {
		Set<Integer> onLinePlayerIdList = new HashSet<>();
		Set<Integer> onlinePlayerIds = ONLINE_PLAYERID_IOSESSION_MAP.keySet();
		if ((onlinePlayerIds != null) && (!onlinePlayerIds.isEmpty())) {
			onLinePlayerIdList.addAll(onlinePlayerIds);
		}
		return onLinePlayerIdList;
	}

	public int getCurrentOnlineCount() {
		return ONLINE_PLAYERID_IOSESSION_MAP.size();
	}

	public T getIoSession(int playerId) {
		return (T) ONLINE_PLAYERID_IOSESSION_MAP.get(playerId);
	}

	public List<T> getAnonymousSessions() {
		return (List<T>) ANONYMOUS_IOSESSION_MAP.values();
		// return new ArrayList(ANONYMOUS_IOSESSION_MAP.values());
	}

	public T getAnonymousSession(int sessionId) {
		return ANONYMOUS_IOSESSION_MAP.get(sessionId);
	}

	public void write(Integer playerId, MessageLite msg) {
		T session = getIoSession(playerId);
		write(session, msg.toByteArray());
	}

//	public void writeTextWebSocketFrame(Integer playerId, String msg) {
//		T session = getIoSession(playerId);
//
//		writeTextWebSocketFrame(session, msg);
//	}

//	public void writeTextWebSocketFrame(T session, String msg) {
//		if (session.channel == null || session.channel.isOpen() == false) {
//			return;
//		}
//
//		if (logLevel.isDebugEnable() || session.logLevel.isDebugEnable()) {
//			log.info("发送网络信息:SessionId:{},Content:{}", session.getSessionId(), msg);
//		}
//
//		try {
//			sessionWriter.write(session, msg);
//		} catch (Exception e) {
//			log.error("SocketWriteError", e);
//		}
//	}

//	public void writeTextWebSocketFrame(Integer playerId, String key, Object content) {
//		JSONObject json = new JSONObject();
//		json.put(key, new Gson().toJson(content));
//		String txt = new Gson().toJson(json);
//		writeTextWebSocketFrame(playerId, txt);
//	}

//	public void writePbMsg2WebSocket(Integer playerId, String key, MessageLite msg) {
//		ByteBuf writeBytes = new PooledByteBufAllocator().buffer().writeBytes(msg.toByteArray());
//		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(writeBytes);
//
//		T session = getIoSession(playerId);
//		if (session.channel == null || session.channel.isOpen() == false) {
//			return;
//		}
//
//		session.channel.writeAndFlush(frame);
//
//		if (logLevel.isDebugEnable() || session.logLevel.isDebugEnable()) {
//			log.info("发送网络信息:PlayerId:{},Content:{}", playerId, msg.toString());
//		}
//	}

//	public void writeTextWebSocketFrame(T session, String key, Object content) {
//		JSONObject json = new JSONObject();
//		json.put(key, new Gson().toJson(content));
//		String txt = new Gson().toJson(json);
//		writeTextWebSocketFrame(session, txt);
//	}

	public void write(Collection<Integer> playerIdList, MessageLite msg) {
		if ((playerIdList == null) || (playerIdList.isEmpty())) {
			return;
		}

		byte[] bytes = msg.toByteArray();
		if (bytes == null) {
			return;
		}

		for (Iterator<Integer> i = playerIdList.iterator(); i.hasNext();) {
			// long playerId = ((Long) i.next()).longValue();
			Integer playerId = i.next();
			T session = getIoSession(playerId);
			if (session != null) {
				write(session, bytes);
			}
		}
	}

	public void writeAllOnline(MessageLite msg) {
		// write(new HashSet(ONLINE_PLAYERID_IOSESSION_MAP.keySet()), response);
		write(ONLINE_PLAYERID_IOSESSION_MAP.keySet(), msg);
	}
	
	public void write(T session, byte[] buffer) {
		if (session == null) {
			return;
		}

		if (session.channel == null || session.channel.isOpen() == false) {
			return;
		}

		Integer playerId = getPlayerId(session);
		if (log.isDebugEnabled()) {
			log.debug(String.format("写数据 [playerId: %d]", new Object[] { playerId }));
		}
		
//		log.info(String.format("发送数据[playerId:%d, 大小:%d]", new Object[]{playerId, buffer.length}));

		if (session.channel.isOpen()) {
			if (buffer != null)
				session.channel.writeAndFlush(buffer);
		} else {
			if (log.isDebugEnabled()) {
				log.debug(String.format("写数据 给玩家[playerId: %d], 因Session未连接, 从在线列表删除", new Object[] { playerId }));
			}
			session.channel.close();
		}
	}

	public String getRemoteIp(T session) {
		if (session == null) {
			return "";
		}

		String remoteIp = (String) session.getAttribute(SessionType.REMOTE_HOST_KEY);
		if (StringUtils.isNotBlank(remoteIp)) {
			return remoteIp;
		}
		try {
			remoteIp = ((InetSocketAddress) session.getChannel().remoteAddress()).getAddress().getHostAddress();
			if (StringUtils.isBlank(remoteIp)) {
				remoteIp = ((InetSocketAddress) session.getChannel().localAddress()).getAddress().getHostAddress();
			}
			session.setAttribute(SessionType.REMOTE_HOST_KEY, remoteIp);
		} catch (Exception e) {
			remoteIp = null;
		}
		return StringUtils.defaultIfBlank(remoteIp, "");
	}

	@Override
	public void sessionInvalided(T session) {
		removeFromAnonymousList(session.getSessionId());

		removeFromOnlineList(session.getSessionId());
	}

	public void schedule2Remove(T session) {
		if (session == null) {
			return;
		}
		int userId = session.userId;
		if(userId == 0) {
			removeFromAnonymousList(session.sessionId);
			return;
		}
		session.sessionStatus = SessionStatus.INVALID;
		session.planRemoveTime = System.currentTimeMillis();
		SCHEDULE_REMOVE_IOSESSION_MAP.put(userId, session);
	}
	
	public String getRunThreads() {
		ThreadGroup group = Thread.currentThread().getThreadGroup();  
		ThreadGroup topGroup = group;  
		// 遍历线程组树，获取根线程组  
		while (group != null) {  
		    topGroup = group;  
		    group = group.getParent();  
		}  
		// 激活的线程数加倍  
		int estimatedSize = topGroup.activeCount() * 2;  
		Thread[] slackList = new Thread[estimatedSize];  
		// 获取根线程组的所有线程  
		int actualSize = topGroup.enumerate(slackList);  
		// copy into a list that is the exact size  
		Thread[] list = new Thread[actualSize];  
		System.arraycopy(slackList, 0, list, 0, actualSize);  
		List<String> nameList = new ArrayList<String>();
		System.out.println("Thread list size == " + list.length);  
		for (Thread thread : list) {  
		    nameList.add(thread.getName());  
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Thread list size == " + list.length);
		sb.append("\r\n");
		sb.append(new GsonBuilder().setPrettyPrinting().create().toJson(nameList));
		log.info(sb.toString());
		return sb.toString();
	}

	protected boolean cleanSession(T session) {
		if (session == ONLINE_PLAYERID_IOSESSION_MAP.get(session.userId)) {
			this.ONLINE_PLAYERID_IOSESSION_MAP.remove(session.userId);
		}

		if (session == ANONYMOUS_IOSESSION_MAP.get(session.sessionId)) {
			this.ANONYMOUS_IOSESSION_MAP.remove(session.sessionId);
		}
		return true;
	}
}
