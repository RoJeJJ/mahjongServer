package com.buding.hall.module.ws;

import com.buding.common.result.Result;
import com.buding.hall.module.msg.vo.BoxMsg;
import com.buding.hall.module.msg.vo.BoxMsgReq;
import com.buding.hall.module.msg.vo.GameChatMsg;
import com.buding.hall.module.msg.vo.MarqueeMsg;
import com.buding.hall.module.msg.vo.MarqueeMsgReq;
import com.buding.hall.module.msg.vo.TextMsg;

public interface MsgPortalService {
	Result sendTplMarqueeMsg(MarqueeMsgReq req) throws Exception;

	Result sendTplBoxMsg(BoxMsgReq req) throws Exception;

	Result sendMarqueeMsg(MarqueeMsg req) throws Exception;

	Result sendBoxMsg(BoxMsg req) throws Exception;
	
	Result sendTextMsg(TextMsg req) throws Exception;
	
	Result sendGameChatMsg(GameChatMsg req) throws Exception;
	
	Result sendMarquee(long msgId, boolean check) throws Exception;
	
	Result removeMarquee(long msgId) throws Exception;
	
	Result removeMail(long mailId) throws Exception;
	
	Result sendMail(long msgId) throws Exception;
	
	Result repushActNotice() throws Exception;
}
