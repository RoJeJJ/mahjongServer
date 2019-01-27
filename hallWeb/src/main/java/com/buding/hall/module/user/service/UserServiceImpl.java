package com.buding.hall.module.user.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.quartz.simpl.RAMJobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.buding.common.result.Result;
import com.buding.common.server.facade.ContainerFacade;
import com.buding.common.util.DateUtil;
import com.buding.common.util.DesUtil;
import com.buding.common.util.IOUtil;
import com.buding.db.model.User;
import com.buding.db.model.UserCurrencyLog;
import com.buding.db.model.UserItem;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.config.PropsConfig;
import com.buding.hall.config.task.BankruptTaskConf;
import com.buding.hall.helper.HallPushHelper;
import com.buding.hall.module.common.constants.CurrencyType;
import com.buding.hall.module.common.constants.UserType;
import com.buding.hall.module.currency.dao.CurrencyLogDao;
import com.buding.hall.module.event.EventService;
import com.buding.hall.module.item.service.ItemService;
import com.buding.hall.module.item.type.ItemChangeReason;
import com.buding.hall.module.server.facade.impl.HallContainerFacade;
import com.buding.hall.module.task.type.TaskType;
import com.buding.hall.module.task.vo.GamePlayingVo;
import com.buding.hall.module.user.dao.UserDao;
import com.buding.hall.module.user.type.UserRole;
import com.buding.hall.network.HallSessionManager;
import com.google.gson.Gson;

public class UserServiceImpl implements InitializingBean, UserService {
	Logger logger = LoggerFactory.getLogger(getClass());
	
	public static UserService instance;
	
	String initJson = null;
	
	@Autowired(required=true)
	UserDao userDao;
		
	@Autowired
	HallContainerFacade containerFacade;
	
	@Autowired
	EventService eventService;
	
	@Autowired
	ItemService itemService;
	
	@Autowired
	ConfigManager configManager;
	
//	@Autowired
//	TaskService taskService;
	
	@Autowired
	HallPushHelper pushHelper;
	
	@Autowired
	HallSessionManager hallSessionManager;
	
	@Autowired
	CurrencyLogDao currencyLogDao;
		
	@Override
	public void afterPropertiesSet() throws Exception {
		 initJson = IOUtil.getFileResourceAsString(new File(configManager.userinitTplPath), "utf-8");
		 instance = this;
	}
	
	public User initUser() {
		User user = new Gson().fromJson(initJson, User.class);
//		user.setCardRecorder(new Date(new Date().getTime()+7*24*3600*1000));
		user.setHeadImg("portrait_img_0" + (new Random().nextInt(8) + 1));
		return user;
	}
	
	public User addGameResult(GamePlayingVo gameResult) {
		User user = null;
		try {			
			logger.info(new Gson().toJson(gameResult));
			
			userDao.addGameResult(gameResult);
						
//			hallRmiClient.notifyUserAttrUpdate(gameResult.userId); 推送属性变更
			pushHelper.pushUserInfoSyn(gameResult.userId); //全量推送用户属性
			
		} catch (Exception e) {
			logger.error("ChangeUserAttr4GameOverError", e);
		}
		
		try {
			eventService.postGamePlayedEvent(gameResult);
		} catch (Exception e) {
			logger.error("UpdateGameTaskEventError", e);
		}
		
		if(gameResult.coin != 0) {
			try {
				this.changeCoin(gameResult.userId, gameResult.coin, false, ItemChangeReason.GAME_WIN_LOSE);
			} catch (Exception e) {
				logger.error("changeCoinError", e);
			}
		}
		
		user = getUser(gameResult.userId);//刷新用户数据
		
		return user;
	}
	
	@Override
	public User getUser(int userId) {
		return this.userDao.getUser(userId);
	}

	@Override
	public void updateUser(User user) {
		this.userDao.updateUser(user);
	}

	@Override
	public boolean isCanReceiveBankAssist(int userId) {		
		int taskType = TaskType.CORUPT_ASSIST;
		int day = DateUtil.getYYYYMMdd(new Date());
		
		//破产类任务的配置只能有一个，所以可以根据TaskType来获取
		BankruptTaskConf conf = (BankruptTaskConf)configManager.getTaskConfByType(taskType);
		if(conf == null) {
			return true;
		}
		
		int repeatCount = conf.repeatCount;
		
//		List<UserTask> list = taskService.getUserTaskList(userId, conf.taskId, day);
//		
//		UserTask task = null;
//		if(list.size() > 0) {
//			task = list.get(list.size() - 1);
//			if(task.getAward()) { //已领奖
//				task = null;
//				boolean finish = list.size() >= repeatCount;
//				if(finish) {
//					return false;
//				}
//			}
//		}
		return false; //默认没有
	}

	@Override
	public Result register(User user) {
		try {
			User user1 = this.userDao.getUserByUserName(user.getUserName());
			if(user1 != null) {
				return Result.fail("用户已存在");
			}
			if(StringUtils.isBlank(user.getNickname())) {
				user.setNickname(user.getUserName());
			}
			user.setPasswd(DesUtil.md5(user.getPasswd(), 16));
			user.setMtime(new Date());
			user.setCtime(new Date());
			this.userDao.insert(user);
			return Result.success();
		} catch (Exception e) {
			logger.error("", e);
			return Result.fail("注册失败,系统错误");
		}
	}

	@Override
	public Result bindMobile(User user, String phone) {
		user.setPhone(phone);
		//如果是游客则设置为手机用户.
		if(user.getUserType() == UserType.VISITOR) {
			user.setUserType(UserType.MOBILE_USER);
		}
		this.userDao.updateUser(user);
		return Result.success();
	}

	@Override
	public User login(String username, String password) {
		try {
			User user = this.userDao.getUserByUserName(username);
			if(user == null) {
				//插入一个. 测试期间用
//				user = initUser();
//				user.setUserName(username);
//				user.setPasswd(password);
//				user.setUserType(UserType.VISITOR);
//				user.setNickname(user.getUserName());
//				Result result = register(user);
//				if(result.isFail()) {
//					return null;	
//				}
				return null;
			}
//			user = this.userDao.getUserByUserName(username);
			if(!user.getPasswd().equals(DesUtil.md5(password, 16))) {
				return null;
			}			
			return user;
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}

	@Override
	public User getByUserName(String username) {
		return this.userDao.getUserByUserName(username);
	}

	@Override
	public void onUserLogin(User user) {		
		containerFacade.onLoginListener(user);		
		eventService.postLoginEvent(user.getId());
		
		logger.info("act=userLogin;userId={};date={}", user.getId(), new SimpleDateFormat("yyyyMMdd HHmmss").format(new Date()));
		
		Date lastLogin = user.getLastLogin();
		user.setLastLogin(new Date());
		Integer continueLogin = user.getContinueLogin();
		continueLogin = continueLogin == null ? 1 : continueLogin;
		if(lastLogin != null) {
			int lastLoginDay = Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(lastLogin));
			int today = Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			if(today - lastLoginDay == 1) {				
				continueLogin ++;
			} else if (today - lastLoginDay > 1) {
				continueLogin = 1;
			}
		}
		user.setContinueLogin(continueLogin);
		userDao.updateUser(user);
	}

	@Override
	public Result hasEnoughCurrency(int userId, int currenceType, int count) {
		User user = this.getUser(userId);
		switch (currenceType) {
		case CurrencyType.coin:
			if(user.getCoin() < count) {
				return Result.fail("金币不足");
			}
			return Result.success();
		case CurrencyType.fanka:
			if(user.getFanka() < count) {
				return Result.fail("房卡不足");
			}
			return Result.success();
		default:
			throw new RuntimeException("检查失败");
		}
	}

	@Override
	public Result hasEnoughItem(int userId, String itemId, int count) {
		PropsConfig props = this.configManager.getPropsConfigById(itemId);
		UserItem ui = itemService.getUserItem(userId, props.itemType);
		if(ui == null || ui.getItemCount() < count) {
			return Result.fail(props.itemName + "不足");
		}
		return Result.success();
	}

	@Override
	public Result changeCoin(int userId, int coin, boolean check, ItemChangeReason reason) {
		if(coin < 0 && check) {
			Result ret = hasEnoughCurrency(userId, CurrencyType.coin, Math.abs(coin));
			if(ret.isFail()) {
				return ret;
			}
		}		
		User user = this.getUser(userId);
		if(user == null) {
			return Result.fail("用户不存在 ");
		}
		int old = user.getCoin();
		user.setCoin(Math.max(user.getCoin() + coin, 0));
		int change = user.getCoin() - old;
		userDao.updateUser(user);
		eventService.postCoinChangeEvent(userId, change);
		pushHelper.pushUserInfoSyn(userId);
		
		addCurrencyLog(userId, "coin", reason, old, change, user.getCoin());
		
		return Result.success();
	}

	private void addCurrencyLog(int userId, String operMainType, ItemChangeReason reason, int old, int change, int to) {
		UserCurrencyLog log = new UserCurrencyLog();
		log.setChangeVal(change+"");
		log.setChangeFrom(old+"");
		log.setChangeTo(to+"");
		log.setOperDesc("");
		log.setOperMainType(operMainType);
		log.setOperSubType(reason.toString());
		log.setOperTime(new Date());
		log.setUserId(userId);
		currencyLogDao.insertLog(log);
	}

	@Override
	public Result changeFangka(int userId, int fanka, boolean check, ItemChangeReason reason) {
		if(fanka < 0 && check) {
			Result ret = hasEnoughCurrency(userId, CurrencyType.fanka, Math.abs(fanka));
			if(ret.isFail()) {
				return ret;
			}
		}		
		User user = this.getUser(userId);
		if(user == null) {
			return Result.fail("用户不存在 ");
		}
		int old = user.getFanka();
		user.setFanka(Math.max(user.getFanka() + fanka, 0));
		int change = user.getFanka() - old;
		userDao.updateUser(user);
		pushHelper.pushUserInfoSyn(userId);
		
		addCurrencyLog(userId, "fangka", reason, old, change, user.getFanka());
		
		return Result.success();
	}

	@Override
	public boolean isUserOnline(int userId) {
		return hallSessionManager.getIoSession(userId) != null;
	}

	@Override
	public Result auth(int userId) {
		User user = getUser(userId);
		if(user == null) {
			return Result.fail("用户不存在");
		}
		if(user.getRole() == null) {
			user.setRole(0);
		}
		int role = user.getRole();
		role = (role | UserRole.USER_ROLE_AUTH);
		user.setRole(role);
		this.userDao.updateUser(user);
		this.pushHelper.pushUserInfoSyn(userId);
		return Result.success();
	}

	@Override
	public Result cancelAuth(int userId) {
		User user = getUser(userId);
		if(user == null) {
			return Result.fail("用户不存在");
		}
		if(user.getRole() == null) {
			user.setRole(0);
		}
		int role = user.getRole();
		if((role & UserRole.USER_ROLE_AUTH) == UserRole.USER_ROLE_AUTH) {
			role = role - UserRole.USER_ROLE_AUTH;
			user.setRole(role);
			this.userDao.updateUser(user);
		}
		return Result.success();
	}

	@Override
	public void onUserLogout(int userId) {
		User user = getUser(userId);
		user.setLastOffline(new Date());
		userDao.updateUser(user);
		
		//统计在线时长
		Date login = user.getLastLogin();
		Date logout = user.getLastOffline();
		int minute = DateUtil.minuteDiff(login, logout);
		userDao.addUserOnlineData(userId, DateUtil.getYYYYMMdd(login), minute);
	}

	@Override
	public Result resetPasswd(int userId, String passwd) {
		if(StringUtils.isBlank(passwd)) {
			return Result.fail("密码为空");
		}
		if(passwd.length() < 6) {
			return Result.fail("密码长度不得少于6位");
		}
		
		User user = userDao.getUser(userId);
		if(user == null) {
			return Result.fail("用户不存在");
		}
		
		user.setPasswd(DesUtil.md5(passwd, 16));
		userDao.updateUser(user);
		return Result.success();
	}

	@Override
	public Result changeUserType(int userId, int type) {
		User user = userDao.getUser(userId);
		if(user == null) {
			return Result.fail("用户不存在");
		}
		user.setUserType(type);
		userDao.updateUser(user);
		return Result.success();
	}	
}
