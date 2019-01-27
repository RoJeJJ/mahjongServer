package com.buding.mj.constants;

public class MJConstants {


    public static final int MJPlayerCount = 4;


    /*
     * 状态值定义
     */
    public static final int MJStateReady = 1; //准备
    public static final int MJStateDeal = 2; //洗牌、发牌、处理底分、台费、宝牌
    public static final int MJStateRun = 3; //开始牌局
    public static final int MJStateFinish = 4; //结算

	/*
     * 玩家状态
	 */
//	public static final int Player_Off = 1; ///玩家离线
//	public static final int Player_On = 2; ///玩家在线
//	public static final int Player_Auto = 3; ///玩家托管


    /**
     * 状态非法
     */
    public static final int STATE_INVALID = 0;

    /**
     * 桌子状态非法
     */
    public static final int TABLE_STATE_INVALID = 0;

    /**
     * 玩家的麻将操作
     */
    public static final int MAHJONG_OPERTAION_CHI = 0x01;//吃
    public static final int MAHJONG_OPERTAION_PENG = 0x02;//碰
    public static final int MAHJONG_OPERTAION_PENG_CHI = 0x03;//碰吃
    public static final int MAHJONG_OPERTAION_AN_GANG = 0x04;//暗杠
    public static final int MAHJONG_OPERTAION_GANG_PENG = 0x100;//杠碰
    public static final int MAHJONG_OPERTAION_GANG_PENG_CHI = 0x200;//杠碰吃
    public static final int MAHJONG_OPERTAION_MING_GANG = 0x08;//明杠
    public static final int MAHJONG_OPERTAION_CHU = 0x10;//出牌
    public static final int MAHJONG_OPERTAION_HU = 0x20;//胡牌
    public static final int MAHJONG_OPERTAION_TING = 0x40;//听牌
    public static final int MAHJONG_OPERTAION_CANCEL = 0x80;//给玩家提示操作，玩家点取消

    public static final int MAHJONG_OPERTAION_CHI_TING = 0x40000;//吃了直接听，吃听
    public static final int MAHJONG_OPERTAION_PENG_TING = 0x2000000;//碰听
    public static final int MAHJONG_OPERTAION_ZD_TING = 0x20000000;// 支对听
    public static final int MAHJONG_OPERTAION_MO = 0x40000000;// 摸牌
    public static final int MAHJONG_OPERTAION_CHNAGE_BAO = 0x80000000;// 换宝

    //花色编码
    //49
    public static final int MAHJONG_CODE_ONE_WANG = 1;//1万
    public static final int MAHJONG_CODE_NINE_WANG = 9;//9万
    public static final int MAHJONG_CODE_ONE_TIAO = 17;//1条
    public static final int MAHJONG_CODE_NINE_TIAO = 25;//9条
    public static final int MAHJONG_CODE_ONE_TONG = 33;//1筒
    public static final int MAHJONG_CODE_NINE_TONG = 41;//9筒
    public static final byte MAHJONG_CODE_DONG_FENG = 0x41;//東
    public static final byte MAHJONG_CODE_NAN_FENG = 0x42;//南
    public static final byte MAHJONG_CODE_XI_FENG = 0x43;//西
    public static final byte MAHJONG_CODE_BEI_FENG = 0x44;//北
    public static final byte MAHJONG_CODE_HONG_ZHONG = 0x45;//红中的编码
    public static final byte MAHJONG_CODE_FA_CAI = 0x46;//发财
    public static final byte MAHJONG_CODE_BAI_BAN = 0x47;//白板
    public static final int MAHJONG_CODE_COLOR_SHIFTS = 4;//花色部分的移位，花色，【0，1，2】
    public static final int MAHJONG_CODE_COLOR_MASK = 0x30;//花色部分的掩码
    /**
     * 玩家牌局结果
     */
    public static final int MAHJONG_HU_CODE_MEN_QING = 0x0001;//门清
    public static final int MAHJONG_HU_CODE_DIAN_PAO = 0x0002;//点炮
    public static final int MAHJONG_HU_CODE_MYSELF_ZHUANG_JIA = 0x0004;//自己是不是庄家
    public static final int MAHJONG_HU_CODE_ZI_MO = 0x0008;//自摸
    public static final int MAHJONG_HU_CODE_JIA_HU = 0x0010;//夹胡
    public static final int MAHJONG_HU_CODE_MO_BAO = 0x0020;//摸宝：摸到宝牌和牌。
    public static final int MAHJONG_HU_CODE_BAO_BIAN = 0x0040;//宝边：在小胡的基础上，要胡的牌正好是宝牌，自摸到宝牌，则称宝边。
    public static final int MAHJONG_HU_CODE_BAO_ZHONG_BAO = 0x0080;//宝中宝：在大胡的基础上，要胡的牌正好是宝牌，自摸到宝牌，则成为宝中宝。
    public static final int MAHJONG_HU_CODE_TING = 0x0100;//是否听牌
    public static final int MAHJONG_HU_CODE_TARGET_ZHUANG_JIA = 0x0200;//输赢的对方是庄家
    public static final int MAHJONG_HU_CODE_GUADAFENG = 0x0400;// 刮大风
    public static final int MAHJONG_HU_CODE_KAIPAIZHA = 0x0800;// 开炸牌
    public static final int MAHJONG_HU_CODE_HONGZHONGMTF = 0x2000;// 红中漫天飞
    public static final int MAHJONG_HU_CODE_DAILOU = 0x4000;// 带漏胡
    public static final int MAHJONG_HU_CODE_WIN = 0x100000;//赢
    public static final int MAHJONG_HU_CODE_LOSE = 0x200000;//输
    public static final int MAHJONG_HU_CODE_LIUJU = 0x400000;//流局

    //胡牌番型
    public static final int MJ_HU_TYPE_BAO_ZHONG_BAO = MAHJONG_HU_CODE_ZI_MO | MAHJONG_HU_CODE_JIA_HU | MAHJONG_HU_CODE_MO_BAO | MAHJONG_HU_CODE_BAO_ZHONG_BAO;
    public static final int MJ_HU_TYPE_MO_BAO_HU = MAHJONG_HU_CODE_ZI_MO | MAHJONG_HU_CODE_MO_BAO;
    public static final int MJ_HU_TYPE_MO_BAO_JIA_HU = MAHJONG_HU_CODE_ZI_MO | MAHJONG_HU_CODE_JIA_HU | MAHJONG_HU_CODE_MO_BAO;
    public static final int MJ_HU_TYPE_ZIMO_JIA_HU = MAHJONG_HU_CODE_ZI_MO | MAHJONG_HU_CODE_JIA_HU;
    public static final int MJ_HU_TYPE_GUADAFENG = MAHJONG_HU_CODE_GUADAFENG | MJ_HU_TYPE_MO_BAO_HU;


    /**
     * 玩家已经满，准备开始
     **/
    public static final int GAME_TABLE_STATE_READY_GO = 2;

    /**
     * 客户端收到起手牌，播放动画，给客户端一定时间
     **/
    public static final int GAME_TABLE_WAITING_CLIENT_SHOW_INIT_CARDS = 3;

    /**
     * 玩家玩牌中
     **/
    public static final int GAME_TABLE_STATE_PLAYING = 4;
    /**
     * 游戏结束，等待客户端显示game over界面
     **/
    public static final int GAME_TABLE_STATE_SHOW_GAME_OVER_SCREEN = 5;

    /**
     * 有玩家离开桌子，暂停游戏
     */
    public static final int GAME_TABLE_STATE_PAUSED = 7;

    /**
     * 玩家吃碰之类的操作，服务器等客户端播个动画
     */
    public static final int GAME_TABLE_SUB_STATE_IDLE = 0;//无任何操作
    public static final int GAME_TABLE_SUB_STATE_PLAYING_CHI_PENG_ANIMATION = 1;//客户端在播吃碰牌动画
    public static final int GAME_TABLE_SUB_STATE_PLAYING_HU_ANIMATION = 2;//客户端在播胡牌动画
    public static final int GAME_TABLE_SUB_STATE_PLAYING_CHU_ANIMATION = 3;//客户端在播出牌动画
    public static final int GAME_TABLE_SUB_STATE_PLAYING_TING_ANIMATION = 4;//客户端在播听牌动画
    public static final int GAME_TABLE_SUB_STATE_SHOW_INIT_CARDS = 5;//客户端在播发牌动画
    public static final int GAME_TABLE_SUB_STATE_SHOW_CHANGE_BAO = 6;//客户端在播换宝动画
    public static final int GAME_TABLE_SUB_STATE_WAIT_CHANGE_BAO = 7;//准备推送上宝/换宝动画
    public static final int GAME_TABLE_SUB_STATE_PLAYING_GANG_ANIMATION = 8;//客户端在播杠牌

    public static final int SEND_TYPE_SINGLE = 1;//发给单人
    public static final int SEND_TYPE_ALL = 2;//发给所有人
    public static final int SEND_TYPE_EXCEPT_ONE = 3;//发给除某人外其他人

    public static final int MAHJONG_CANCEL_OPER_CHI_PENG = 0x01;
    public static final int MAHJONG_CANCEL_OPER_TING = 0x02;
}
