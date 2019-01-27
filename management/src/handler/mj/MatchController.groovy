package handler.mj

import com.buding.hall.module.item.type.ItemChangeReason
import com.buding.hall.module.ws.BattlePortalBroadcastService
import com.buding.hall.module.ws.MsgPortalService
import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.battle.HallProxy
import org.slf4j.Logger
import utils.Utils

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

h.get('/a/mj/match/list') { req, resp, ctx ->
    def roomType = req.params.roomType
    D d = ctx.d;
    def list = d.query("select * from room_conf where status = 1 and room_type = ? ", [roomType])
    resp.ok(list)
}

h.post('/a/mj/match/save') { req, resp, ctx ->
    req.jsonHandler {json->
        D d = ctx.d;
        def map = Utils.getMapInKeys(json, "id,roomId,roomName,roomType,matchId,baseScore,minCoinLimit,maxCoinLimit,icon,matchClassFullName,gameParamOperTimeOut,gameParamAutoOperWhenTimeout,gameParamThinkMillsWhenAutoOper,gameParamChiPengPlayMills,gameParamChuPlayMills,seatSizeLower,seatSizeUpper,autoStartGame,autoChangeDesk,gameClassFullName,deskClassFullName,roomClassFullName,supportRobot,secondsAddFirstRobot,addRobotRate,autoReady,secondsBeforKickout,fee,srvFee,gameCountLow,gameCountHigh")
        map.status = 1;

        def dbModel = d.queryMap("select * from room_conf where room_id = ? ", [map.roomId])
        if(dbModel) {
            map.id = dbModel.id;
            d.update(map, "room_conf")
        } else {
            map.id = d.add(map, "room_conf")
        }
        resp.ok()
    }
}

//发布
h.get('/a/mj/match/publish') { req, resp, ctx ->
    D d = ctx.d;
    def list = d.query("select * from room_conf where status = 1")
    list.each {
        def fee = """
            [
                {
                    "currenceType":'2',
                    "currenceCount":20,
                    "gameCount":1
                }
            ]
        """;
        if(it.roomType == 'VIP') {
            fee = """
            [
                {
                    "itemId":'A001',
                    "itemCount":4,
                    "gameCount":${it.gameCountLow}
                },
                {
                    "itemId":'A001',
                    "itemCount":8,
                    "gameCount":${it.gameCountHigh}
                }
            ]
        """;
        }
        it.confJson = """
    {
        "comment": "大庆麻将${it.roomName}",
        "matchID": "${it.matchId}",
        "gameID": "G_DQMJ",
        "matchClassFullName":"${it.matchClassFullName}",
        "conditionInfo": {
            "comment": "这是房间信息,roomArray有不同底分来区分房间, enterCondition是入场条件,deskConf是桌子人数和自动开赛的设置",
            "version": 1,
            "enterCondition": {
                "minCoinLimit": ${it.minCoinLimit},
                "maxCoinLimit": ${it.maxCoinLimit},
                "currencyType":2
            },
            "deskConf": {
                "allowExitWhenGaming":${it.allowExitWhenGaming},
                "awayIsGaming":${it.awayIsExit},
                "seatSizeLower": ${it.seatSizeLower},
                "seatSizeUpper": ${it.seatSizeLower},
                "autoStartGame": ${it.autoStartGame},
                "autoChangeDesk": ${it.autoChangeDesk},
                "gameClassFullName": "${it.gameClassFullName}",
                "deskClassFullName": "${it.deskClassFullName}",
                "supportRobot":${it.supportRobot},
                "secondsAddFirstRobot": ${it.secondsAddFirstRobot},
                "addRobotRate": ${it.addRobotRate},
                "autoReady":${it.autoReady},
                "secondsBeforKickout":${it.secondsBeforKickout},
                "gameParam":"{'autoOperWhenTimeout':${it.gameParamAutoOperWhenTimeout}, 'chiPengPlayMills':${it.gameParamChiPengPlayMills},chuPlayMills:${it.gameParamChuPlayMills},operTimeOutSeconds:${it.gameParamOperTimeOut},thinkMills4AutoOper:${it.gameParamThinkMillsWhenAutoOper}}"
            },
            "roomArray": [
                {
                    "comment": "roomName用于打印区分,basepoint为底分,roomId是房间配置id必须全局唯一, 这是是否需要添加人数限制,settleCurrenceType是结算货币,feeCurrenType是收费货币",
                    "roomName": "${it.roomName}",
                    "roomId": "${it.roomId}",
                    "basePoint": ${it.baseScore},
                    "low": ${it.minCoinLimit},
                    "high": ${it.maxCoinLimit},
                    "fee": ${fee},
                    "currencyType":2,
                    "roomClassFullName":"${it.roomClassFullName}",
					"roomType":${it.roomType}
                }
            ]
        }
    }
        """ as String

        d.update(it, "room_conf");
    }

    HallProxy proxy = ctx.hallProxy
    proxy.getBattleService(c.zkHost).reloadMatchConf("all");
    resp.ok()
}