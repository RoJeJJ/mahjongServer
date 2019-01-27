package handler.mj

import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.common.FieldMerger
import org.slf4j.Logger
import service.AdminLogService
import utils.Utils

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config


h.get('/a/mj/agent/list/:pageNum') { req, resp, ctx ->
    int page = req.params.pageNum as int
    def name = req.params.name
    def registerStartTime = req.params.registerStartTime
    def registerEndTime = req.params.registerEndTime

    D d = ctx.d;
    def sql = "select * from agent where status <> 10 "
    def args = []
    if(name) {
        sql += " and (username like ? or name like ? ) "
        def key = "%"  + name + "%"
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

    def pager = d.pagi(sql, args, page, 20)
    resp.ok(pager:pager)
}

h.get('/a/mj/agent/fangka/add') { req, resp, ctx ->
    int id = req.params.id as int
    int count = req.params.fangka as int
    D d = ctx.d;
    def agent = d.queryMap("select * from agent where id = ? ", [id])
    if(agent) {
        d.update([fangka:agent.fangka + count, totalBuyFangka:agent.totalBuyFangka+count, id : id], "agent")
        d.add([fromUserId:-1, fromUserName:'系统管理员', toUserId:agent.id, toUserName:agent.name, dealAmount:count, dealType:1, dealDate:new Date(), dealDesc:"购入房卡${count}张" as String, agentId:agent.id], "agent_fangka_log")
        resp.ok([fangka:agent.fangka + count, totalBuyFangka:agent.totalBuyFangka+count])
        AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_AGENT_FANGKA", id, 0, 0, count, new Date(), "出售房卡${count}张给代理商${agent.username}")
        return;
    }
    resp.fail("代理商不存在")
}

h.get('/a/mj/agent/integral/add') { req, resp, ctx ->
    int id = req.params.id as int
    int count = req.params.integral as int
    D d = ctx.d;
    def agent = d.queryMap("select * from agent where id = ? ", [id])
    if(agent) {
        d.update([integral:agent.integral + count, id : id, totalBuyFangka:agent.totalBuyFangka+count], "agent")
        resp.ok([integral:agent.integral + count])
        return;
    }
    resp.fail("代理商不存在")
}

h.post('/a/mj/agent/info/add') { req, resp, ctx ->
    req.jsonHandler {json->
        D d = ctx.d;
        def map = Utils.getMapInKeys(json, "id,username,passwd,name")
        map.mtime = new Date();
        if(map.id) {
            d.update(map, "agent")
        } else {
            map.ctime = new Date();
            map.status = 1;
            d.add(map, "agent")
            AdminLogService.addAdminLog(req.getLocal("user").id, "ADD_AGENT", map.username, 0, 0, map.username, new Date(), "增加代理商:${map.username}")
        }

        resp.ok()
    }
}

h.get('/a/mj/agent/status') { req, resp, ctx ->
    def id = req.params.id as int
    def status = req.params.status as int
    D d = ctx.d;
    d.update([id:id, status:status], "agent")
    if(status == 10) {
        AdminLogService.addAdminLog(req.getLocal("user").id, "DEL_AGENT", id, 0, 0, id, new Date(), "删除代理商,id:${id}")
    }
    resp.ok()
}


h.get('/a/mj/agent/day_report') { req, resp, ctx ->
    def startDate = req.params.startDate;
    def endDate = req.params.endDate;

    D d = ctx.d;
    def args = []

    def groupByDaySql = "SELECT DATE_FORMAT(deal_date, '%Y-%m-%d') as deal_date, SUM(IF(deal_type=1, deal_amount, 0)) AS store, SUM(IF(deal_type=1, 0, deal_amount)) AS sell FROM agent_fangka_log where 1=1 "
    def allSql = "SELECT SUM(IF(deal_type=1, deal_amount, 0)) AS store, SUM(IF(deal_type=1, 0, deal_amount)) AS sell FROM agent_fangka_log where 1=1 ";

    if(startDate) {
        args << startDate
        groupByDaySql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') >= ? "
        allSql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') >= ? "
    }
    if(endDate) {
        args << endDate
        groupByDaySql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') <= ? "
        allSql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') <= ? "
    }

    def rows = d.query(groupByDaySql + " GROUP BY DATE_FORMAT(deal_date, '%Y-%m-%d') ORDER BY deal_date desc limit 30", args)

    def total = d.queryMap(allSql, args)

    resp.ok([rows:rows, total:total])
}

h.get('/a/mj/agent/month_report') { req, resp, ctx ->
    D d = ctx.d;
    def args = []

    def sql = "SELECT agent_id, DATE_FORMAT(deal_date, '%Y-%m') AS mon, SUM(IF(deal_type=1, deal_amount, 0)) AS store, SUM(IF(deal_type=1, 0, deal_amount)) AS sell  FROM agent_fangka_log GROUP BY DATE_FORMAT(deal_date, '%Y-%m') ,agent_id ORDER BY mon desc"
    def list = d.query(sql, args)

    FieldMerger merger = ctx.fieldMerger;
    merger.merge(list, "agentId", "username,name", "agent")
    resp.ok(list)
}

h.get('/a/mj/agent/deal/logs') { req, resp, ctx ->
    int pageNum = req.params.pageNum as int
    int dealType = req.params.dealType as int
    def username = req.params.username
    def startDate = req.params.startDate
    def endDate = req.params.endDate

    D d = ctx.d;
    def args = [dealType]

    def sql = "select * from agent_fangka_log a where 1=1 and deal_type = ? "

    if(username) {
        sql += " and exists (select 1 from agent b where a.agent_id = b.id and b.username like ?) ";
        args << "%"+username+"%"
    }

    if(startDate) {
        sql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') >= ?";
        args << startDate
    }

    if(endDate) {
        sql += " and DATE_FORMAT(deal_date, '%Y-%m-%d') <= ? "
        args << endDate
    }

    sql += " order by deal_date desc "

    def pager = d.pagi(sql, args, pageNum, 20)

    FieldMerger merger = ctx.fieldMerger;
    merger.merge(pager.ll, "agentId", "username,name", "agent")
    resp.ok([pager:pager])
}

h.get('/a/mj/agent/notice/list/:pageNum') { req, resp, ctx ->
    int pageNum =  req.params.pageNum as int
    D d = ctx.d;
    def args = []

    def sql = "select * from agent_notice"
    def list = d.pagi(sql, args, pageNum, 20)

    resp.ok([pager:list])
}

h.get('/a/mj/agent/notice/del') { req, resp, ctx ->
    int id = req.params.id as int
    D d = ctx.d;
    d.del(id, "agent_notice")
    resp.ok()
}

h.post('/a/mj/agent/notice/save') { req, resp, ctx ->
   req.jsonHandler {json->
       def map = Utils.getMapInKeys(json, "title,content")
       map.ctime = new Date()
       D d = ctx.d;
       d.add(map, "agent_notice")
       resp.ok()
   }
}