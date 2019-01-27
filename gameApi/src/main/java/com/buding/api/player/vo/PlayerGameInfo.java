package com.buding.api.player.vo;

/**
 * Created by huangbp on 2017/12/15.
 * desc:
 */
public class PlayerGameInfo {
    public PlayerGameInfo() {

    }

    public PlayerGameInfo(int playerId, boolean isBuy, int piaoNum) {
        this.playerId = playerId;
        this.isBuy = isBuy;
        this.piaoNum = piaoNum;
    }


    private int playerId;
    private boolean isBuy;  //买:输赢 分都要 翻一倍,如果放炮与胡牌玩家都买(对火) 放炮方翻4倍
    private int piaoNum;    //飘:自己买的飘分 加上 胡牌者的飘分,
    private int card;

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public boolean isBuy() {
        return isBuy;
    }

    public void setBuy(boolean buy) {
        isBuy = buy;
    }

    public int getPiaoNum() {
        return piaoNum;
    }

    public void setPiaoNum(int piaoNum) {
        this.piaoNum = piaoNum;
    }

    public int getCard() {
        return card;
    }

    public void setCard(int card) {
        this.card = card;
    }
}