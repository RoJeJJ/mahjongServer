package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;

public class User extends BaseModel<Integer> implements Serializable {
    private Integer id;

    private Integer userType;

    private String userName;

    private String passwd;

    private String nickname;

    private Integer gender;

    private Date lastLogin;

    private Date lastOffline;

    private Integer continueLogin;

    private String phone;

    private String weixin;

    private Integer coin;

    private Integer fanka;

    private Integer integral;

    private String headImg;

    private String bindedMobile;

    private String bindedMatch;

    private String token;

    private String wxopenid;

    private String qqopenid;

    private String deviceId;

    private Integer deviceType;

    private Integer role;

    private String goodPlayer;

    private Date ctime;

    private Date mtime;

    private Date authTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd == null ? null : passwd.trim();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getLastOffline() {
        return lastOffline;
    }

    public void setLastOffline(Date lastOffline) {
        this.lastOffline = lastOffline;
    }

    public Integer getContinueLogin() {
        return continueLogin;
    }

    public void setContinueLogin(Integer continueLogin) {
        this.continueLogin = continueLogin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getWeixin() {
        return weixin;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin == null ? null : weixin.trim();
    }

    public Integer getCoin() {
        return coin;
    }

    public void setCoin(Integer coin) {
        this.coin = coin;
    }

    public Integer getFanka() {
        return fanka;
    }

    public void setFanka(Integer fanka) {
        this.fanka = fanka;
    }

    public Integer getIntegral() {
        return integral;
    }

    public void setIntegral(Integer integral) {
        this.integral = integral;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg == null ? null : headImg.trim();
    }

    public String getBindedMobile() {
        return bindedMobile;
    }

    public void setBindedMobile(String bindedMobile) {
        this.bindedMobile = bindedMobile == null ? null : bindedMobile.trim();
    }

    public String getBindedMatch() {
        return bindedMatch;
    }

    public void setBindedMatch(String bindedMatch) {
        this.bindedMatch = bindedMatch == null ? null : bindedMatch.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getWxopenid() {
        return wxopenid;
    }

    public void setWxopenid(String wxopenid) {
        this.wxopenid = wxopenid == null ? null : wxopenid.trim();
    }

    public String getQqopenid() {
        return qqopenid;
    }

    public void setQqopenid(String qqopenid) {
        this.qqopenid = qqopenid == null ? null : qqopenid.trim();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId == null ? null : deviceId.trim();
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }

    public Date getAuthTime() {
        return authTime;
    }

    public void setAuthTime(Date authTime) {
        this.authTime = authTime;
    }

    public String getGoodPlayer() {
        return goodPlayer;
    }

    public void setGoodPlayer(String goodPlayer) {
        this.goodPlayer = goodPlayer;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        User other = (User) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserType() == null ? other.getUserType() == null : this.getUserType().equals(other.getUserType()))
            && (this.getUserName() == null ? other.getUserName() == null : this.getUserName().equals(other.getUserName()))
            && (this.getPasswd() == null ? other.getPasswd() == null : this.getPasswd().equals(other.getPasswd()))
            && (this.getNickname() == null ? other.getNickname() == null : this.getNickname().equals(other.getNickname()))
            && (this.getGender() == null ? other.getGender() == null : this.getGender().equals(other.getGender()))
            && (this.getLastLogin() == null ? other.getLastLogin() == null : this.getLastLogin().equals(other.getLastLogin()))
            && (this.getLastOffline() == null ? other.getLastOffline() == null : this.getLastOffline().equals(other.getLastOffline()))
            && (this.getContinueLogin() == null ? other.getContinueLogin() == null : this.getContinueLogin().equals(other.getContinueLogin()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getWeixin() == null ? other.getWeixin() == null : this.getWeixin().equals(other.getWeixin()))
            && (this.getCoin() == null ? other.getCoin() == null : this.getCoin().equals(other.getCoin()))
            && (this.getFanka() == null ? other.getFanka() == null : this.getFanka().equals(other.getFanka()))
            && (this.getIntegral() == null ? other.getIntegral() == null : this.getIntegral().equals(other.getIntegral()))
            && (this.getHeadImg() == null ? other.getHeadImg() == null : this.getHeadImg().equals(other.getHeadImg()))
            && (this.getBindedMobile() == null ? other.getBindedMobile() == null : this.getBindedMobile().equals(other.getBindedMobile()))
            && (this.getBindedMatch() == null ? other.getBindedMatch() == null : this.getBindedMatch().equals(other.getBindedMatch()))
            && (this.getToken() == null ? other.getToken() == null : this.getToken().equals(other.getToken()))
            && (this.getWxopenid() == null ? other.getWxopenid() == null : this.getWxopenid().equals(other.getWxopenid()))
            && (this.getQqopenid() == null ? other.getQqopenid() == null : this.getQqopenid().equals(other.getQqopenid()))
            && (this.getDeviceId() == null ? other.getDeviceId() == null : this.getDeviceId().equals(other.getDeviceId()))
            && (this.getDeviceType() == null ? other.getDeviceType() == null : this.getDeviceType().equals(other.getDeviceType()))
            && (this.getRole() == null ? other.getRole() == null : this.getRole().equals(other.getRole()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()))
            && (this.getMtime() == null ? other.getMtime() == null : this.getMtime().equals(other.getMtime()))
            && (this.getAuthTime() == null ? other.getAuthTime() == null : this.getAuthTime().equals(other.getAuthTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserType() == null) ? 0 : getUserType().hashCode());
        result = prime * result + ((getUserName() == null) ? 0 : getUserName().hashCode());
        result = prime * result + ((getPasswd() == null) ? 0 : getPasswd().hashCode());
        result = prime * result + ((getNickname() == null) ? 0 : getNickname().hashCode());
        result = prime * result + ((getGender() == null) ? 0 : getGender().hashCode());
        result = prime * result + ((getLastLogin() == null) ? 0 : getLastLogin().hashCode());
        result = prime * result + ((getLastOffline() == null) ? 0 : getLastOffline().hashCode());
        result = prime * result + ((getContinueLogin() == null) ? 0 : getContinueLogin().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getWeixin() == null) ? 0 : getWeixin().hashCode());
        result = prime * result + ((getCoin() == null) ? 0 : getCoin().hashCode());
        result = prime * result + ((getFanka() == null) ? 0 : getFanka().hashCode());
        result = prime * result + ((getIntegral() == null) ? 0 : getIntegral().hashCode());
        result = prime * result + ((getHeadImg() == null) ? 0 : getHeadImg().hashCode());
        result = prime * result + ((getBindedMobile() == null) ? 0 : getBindedMobile().hashCode());
        result = prime * result + ((getBindedMatch() == null) ? 0 : getBindedMatch().hashCode());
        result = prime * result + ((getToken() == null) ? 0 : getToken().hashCode());
        result = prime * result + ((getWxopenid() == null) ? 0 : getWxopenid().hashCode());
        result = prime * result + ((getQqopenid() == null) ? 0 : getQqopenid().hashCode());
        result = prime * result + ((getDeviceId() == null) ? 0 : getDeviceId().hashCode());
        result = prime * result + ((getDeviceType() == null) ? 0 : getDeviceType().hashCode());
        result = prime * result + ((getRole() == null) ? 0 : getRole().hashCode());
        result = prime * result + ((getCtime() == null) ? 0 : getCtime().hashCode());
        result = prime * result + ((getMtime() == null) ? 0 : getMtime().hashCode());
        result = prime * result + ((getAuthTime() == null) ? 0 : getAuthTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userType=").append(userType);
        sb.append(", userName=").append(userName);
        sb.append(", passwd=").append(passwd);
        sb.append(", nickname=").append(nickname);
        sb.append(", gender=").append(gender);
        sb.append(", lastLogin=").append(lastLogin);
        sb.append(", lastOffline=").append(lastOffline);
        sb.append(", continueLogin=").append(continueLogin);
        sb.append(", phone=").append(phone);
        sb.append(", weixin=").append(weixin);
        sb.append(", coin=").append(coin);
        sb.append(", fanka=").append(fanka);
        sb.append(", integral=").append(integral);
        sb.append(", headImg=").append(headImg);
        sb.append(", bindedMobile=").append(bindedMobile);
        sb.append(", bindedMatch=").append(bindedMatch);
        sb.append(", token=").append(token);
        sb.append(", wxopenid=").append(wxopenid);
        sb.append(", qqopenid=").append(qqopenid);
        sb.append(", deviceId=").append(deviceId);
        sb.append(", deviceType=").append(deviceType);
        sb.append(", role=").append(role);
        sb.append(", ctime=").append(ctime);
        sb.append(", mtime=").append(mtime);
        sb.append(", authTime=").append(authTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    public boolean isGoodPlayer() {
        boolean result = false;
        if (StringUtils.equals("1", goodPlayer)){
            result = true;
        }
        return result;
    }
}