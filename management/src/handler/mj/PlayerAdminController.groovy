package handler.mj

import com.buding.common.result.Result
import com.buding.db.model.User
import com.buding.hall.module.common.constants.CurrencyType
import com.buding.hall.module.item.type.ItemChangeReason
import com.buding.hall.module.ws.BattlePortalBroadcastService
import com.buding.hall.module.ws.HallPortalService
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.battle.HallProxy
import org.slf4j.Logger
import service.AdminLogService

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/player/list/:pageNum') { req, resp, ctx ->
    int page = req.params.pageNum as int
    def name = req.params.name
    def registerStartTime = req.params.registerStartTime
    def registerEndTime = req.params.registerEndTime
    def auth = req.params.auth as int ;
    def userType = req.params.userType
    def deviceType = req.params.deviceType

    D d = ctx.d;
    def sql = "select *, '获取失败' as online from user where binded_match is null "
    def args = []
    if(name) {
        sql += " and (user_name like ? or nickname like ? or id like ? ) "
        def key = "%"  + name + "%"
        args << key
        args << key
        args << key
    }
    if(registerStartTime) {
        sql += " and ctime >= ? "
        args << registerStartTime
    }
    if(registerEndTime) {
        sql += " and ctime <= ? "
        args << registerEndTime
    }
    if(userType) {
        sql += " and user_type = ? "
        args << userType
    }
    if(deviceType) {
        sql += " and device_type =  ? "
        args << deviceType
    }

    if(auth == 1) {
        sql += " and (role & 2) = 2 " //已授权
    }
    if(auth == 2) {
        sql += " and (role & 2) <> 2 "; //未授权
    }

    def pager = d.pagi(sql, args, page, 20)
    try {
        pager.ll.each {
            HallProxy hallProxy = ctx.hallProxy;
            it.online = hallProxy.getHallService(c.zkHost).isUserOnline(it.id) ? "在线" : "离线"
        }
    } catch (Exception e) {
        l.error("", e)
    }
    resp.ok(pager:pager)
}

h.get('/a/mj/player/detail/:id') { req, resp, ctx ->
    int userId = req.params.id as int
    D d = ctx.d;
    def map = d.queryMap("select * from user where id = ? ", [userId])
    resp.ok(map)
}

h.get('/a/mj/player/coin/edit') { req, resp, ctx ->
    int userId = req.params.userId as int
    int coin = req.params.coin as int
    HallProxy hallProxy = ctx.hallProxy
    User user = hallProxy.getHallService(c.zkHost).getUser(userId)
    int change = coin - user.getCoin()
    Result ret = hallProxy.getHallService(c.zkHost).changeCoin(userId, change, false, ItemChangeReason.ADMIN_CHANGE)
    if(ret.isOk()) {
        def str = "给玩家${user.nickname}增加金币${change}个"
        AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_USER_COIN", userId, 0, 0, change, new Date(), str)
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/fanka/edit') { req, resp, ctx ->
    int userId = req.params.userId as int
    int fanka = req.params.fanka as int
    HallProxy proxy = ctx.hallProxy
    def srv = proxy.getHallService(c.zkHost)
    User user = srv.getUser(userId)
    int change = fanka - user.getFanka()
    int from = user.getFanka();
    int to = fanka;
    Result ret = srv.changeFangka(userId, change, false, ItemChangeReason.ADMIN_CHANGE)
    if(ret.isOk()) {
        def str = "给玩家${user.nickname}增加房卡${change}个"
        AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_USER_FANGKA", userId, from, to, change, new Date(), str)
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/coin/move') { req, resp, ctx ->
    int fromUser = req.params.fromUser as int
    int toUser = req.params.toUser as int
    int coin = req.params.coin as int
    HallProxy hallProxy = ctx.hallProxy
    HallPortalService userService = hallProxy.getHallService(c.zkHost)
    User fromUserModel = userService.getUser(fromUser)
    User toUserModel = userService.getUser(toUser)
    if(!fromUserModel || !toUserModel) {
        resp.fail("玩家不存在")
        return;
    }

    Result ret = userService.hasEnoughCurrency(fromUser, CurrencyType.coin, coin)
    if(ret.isFail()) {
        resp.fail(ret.msg)
        return;
    }

    ret = userService.changeCoin(fromUser, -coin, true, ItemChangeReason.MOVE)
    if(ret.isFail()) {
        resp.fail(ret.msg)
        return;
    }
    ret = userService.changeCoin(toUser, coin, false, ItemChangeReason.MOVE)
    if(ret.isFail()) {
        resp.fail(ret.msg)
        return;
    }
    resp.ok()
}

h.post('/a/mj/player/fangka/batch_add') { req, resp, ctx ->
    req.jsonHandler {json->
        def users = json.users
        def fangka = json.fangka as int
        HallProxy hallProxy = ctx.hallProxy;
        HallPortalService userService = hallProxy.getHallService(c.zkHost)
        users.each {
            def userId = it.userId as int
            def user = ctx.d.queryMap("select * from user where id = ?", [userId])
            Result ret = userService.changeFangka(userId, fangka, false, ItemChangeReason.ADMIN_CHANGE)
            it.result = ret.isOk()
            it.msg = ret.msg;
            if(ret.isOk()) {
                int from = user.fanka;
                int to = from + fangka;
                def str = "给玩家${user.userName}增加房卡${fangka}张"
                AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_USER_FANGKA", userId, from, to, fangka, new Date(), str)
            }
        }
        resp.ok(json)
    }
}

h.get('/a/mj/player/auth/cancel') { req, resp, ctx ->
    D d = ctx.d;
    def userId = req.params.userId as int
    HallProxy hallProxy = ctx.hallProxy;
    HallPortalService userService = hallProxy.getHallService(c.zkHost)
    Result ret = userService.cancelAuth(userId)
    if(ret.isOk()) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/auth/add') { req, resp, ctx ->
    D d = ctx.d;
    def userId = req.params.userId as int
    HallProxy hallProxy = ctx.hallProxy;
    HallPortalService userService = hallProxy.getHallService(c.zkHost)
    Result ret = userService.auth(userId)
    if(ret.isOk()) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/passwd/change') { req, resp, ctx ->
    D d = ctx.d;
    def userId = req.params.userId as int
    def passwd = req.params.passwd
    HallProxy hallProxy = ctx.hallProxy;
    HallPortalService userService = hallProxy.getHallService(c.zkHost)
    Result ret = userService.resetPasswd(userId, passwd)
    if(ret.isOk()) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/desk/clear') { req, resp, ctx ->
    D d = ctx.d;
    def userId = req.params.userId as int
    HallProxy hallProxy = ctx.hallProxy;
    BattlePortalBroadcastService srv = hallProxy.getBattleService(c.zkHost)
    Result ret = srv.clearDesk("all", userId)
    if(ret.isOk()) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}

h.get('/a/mj/player/user_type/change') { req, resp, ctx ->
    D d = ctx.d;
    def userId = req.params.userId as int
    HallProxy hallProxy = ctx.hallProxy;
    HallPortalService srv = hallProxy.getHallService(c.zkHost);
    Result ret = srv.changeUserType(userId)
    if(ret.isOk()) {
        resp.ok()
    } else {
        resp.fail(ret.msg)
    }
}