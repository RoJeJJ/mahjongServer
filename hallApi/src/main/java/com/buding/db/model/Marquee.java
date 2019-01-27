package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class Marquee extends BaseModel<Long> implements Serializable {
    private Long id;

    private String msgContent;

    private Integer loopPlayCount;

    private Date startTime;

    private Date endTime;

    private Integer marqueeType;

    private Integer loopPushCount;

    private Integer loopPushInterval;

    private Integer userGroup;

    private Boolean pushOnLogin;

    private Integer status;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent == null ? null : msgContent.trim();
    }

    public Integer getLoopPlayCount() {
        return loopPlayCount;
    }

    public void setLoopPlayCount(Integer loopPlayCount) {
        this.loopPlayCount = loopPlayCount;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getMarqueeType() {
        return marqueeType;
    }

    public void setMarqueeType(Integer marqueeType) {
        this.marqueeType = marqueeType;
    }

    public Integer getLoopPushCount() {
        return loopPushCount;
    }

    public void setLoopPushCount(Integer loopPushCount) {
        this.loopPushCount = loopPushCount;
    }

    public Integer getLoopPushInterval() {
        return loopPushInterval;
    }

    public void setLoopPushInterval(Integer loopPushInterval) {
        this.loopPushInterval = loopPushInterval;
    }

    public Integer getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(Integer userGroup) {
        this.userGroup = userGroup;
    }

    public Boolean getPushOnLogin() {
        return pushOnLogin;
    }

    public void setPushOnLogin(Boolean pushOnLogin) {
        this.pushOnLogin = pushOnLogin;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
        Marquee other = (Marquee) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getMsgContent() == null ? other.getMsgContent() == null : this.getMsgContent().equals(other.getMsgContent()))
            && (this.getLoopPlayCount() == null ? other.getLoopPlayCount() == null : this.getLoopPlayCount().equals(other.getLoopPlayCount()))
            && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
            && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
            && (this.getMarqueeType() == null ? other.getMarqueeType() == null : this.getMarqueeType().equals(other.getMarqueeType()))
            && (this.getLoopPushCount() == null ? other.getLoopPushCount() == null : this.getLoopPushCount().equals(other.getLoopPushCount()))
            && (this.getLoopPushInterval() == null ? other.getLoopPushInterval() == null : this.getLoopPushInterval().equals(other.getLoopPushInterval()))
            && (this.getUserGroup() == null ? other.getUserGroup() == null : this.getUserGroup().equals(other.getUserGroup()))
            && (this.getPushOnLogin() == null ? other.getPushOnLogin() == null : this.getPushOnLogin().equals(other.getPushOnLogin()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getMsgContent() == null) ? 0 : getMsgContent().hashCode());
        result = prime * result + ((getLoopPlayCount() == null) ? 0 : getLoopPlayCount().hashCode());
        result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
        result = prime * result + ((getMarqueeType() == null) ? 0 : getMarqueeType().hashCode());
        result = prime * result + ((getLoopPushCount() == null) ? 0 : getLoopPushCount().hashCode());
        result = prime * result + ((getLoopPushInterval() == null) ? 0 : getLoopPushInterval().hashCode());
        result = prime * result + ((getUserGroup() == null) ? 0 : getUserGroup().hashCode());
        result = prime * result + ((getPushOnLogin() == null) ? 0 : getPushOnLogin().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", msgContent=").append(msgContent);
        sb.append(", loopPlayCount=").append(loopPlayCount);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", marqueeType=").append(marqueeType);
        sb.append(", loopPushCount=").append(loopPushCount);
        sb.append(", loopPushInterval=").append(loopPushInterval);
        sb.append(", userGroup=").append(userGroup);
        sb.append(", pushOnLogin=").append(pushOnLogin);
        sb.append(", status=").append(status);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}