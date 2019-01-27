package com.buding.hall.module.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.buding.common.event.EventBus;
import com.buding.common.util.IOUtil;
import com.buding.hall.config.ConfigManager;
import com.buding.hall.module.task.event.BindMobileEvent;
import com.buding.hall.module.task.event.CoinChangeEvent;
import com.buding.hall.module.task.event.GamePlayedEvent;
import com.buding.hall.module.task.event.LoginEvent;
import com.buding.hall.module.task.event.RatingEvent;
import com.buding.hall.module.task.event.ShareEvent;
import com.buding.hall.module.task.vo.GamePlayingVo;
import com.buding.hall.module.task.vo.PlayerCoinVo;
import com.buding.hall.module.task.vo.RatingVo;
import com.google.gson.Gson;

@Component
public class EventService {
	@Autowired
	@Qualifier("hallEventBus")
	EventBus eventBus;
	
	@Autowired
	ConfigManager configManager;
	
//	@Autowired
//	TaskService taskService;
		
	public void postShareEvent(int userId, int rating) {
		ShareEvent event = new ShareEvent(userId);
		eventBus.post(event);
	}
		
	public void postBindMobileEvent(int userId) {
		BindMobileEvent event = new BindMobileEvent(userId);
		eventBus.post(event);
	}
	
	public void postCoinChangeEvent(int userId, int coinChange) {
		PlayerCoinVo vo = new PlayerCoinVo(userId, coinChange);
		CoinChangeEvent event = new CoinChangeEvent(vo);
		eventBus.post(event);
	}
	
	public void postGamePlayedEvent(GamePlayingVo ret) {
		GamePlayedEvent event = new GamePlayedEvent(ret);
		eventBus.post(event);
	}
	
	public void postLoginEvent(int userId) {
		LoginEvent event = new LoginEvent(userId);
		eventBus.post(event);
	}
	
	public void postRatingEvent(RatingVo ret) {
		RatingEvent event = new RatingEvent(ret);
		eventBus.post(event);
	}
	
	public void triggerGameResultEvent() throws Exception {
		String path = configManager.gmPath;
		String json = IOUtil.getFileResourceAsString(path+"/GameResult.json", "utf-8");
		GamePlayingVo copy = new Gson().fromJson(json, GamePlayingVo.class);
		postGamePlayedEvent(copy);
	}
}
