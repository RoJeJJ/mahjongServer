package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class UserRoomResultDetail extends BaseModel<Long> implements Serializable {
    private Long id;

    private Long roomId;

    private String roomName;

    private Date startTime;

    private Date endTime;

    private Integer bankerPos;

    private Integer winerPos;

    private String detail;

    private String playBackPath;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName == null ? null : roomName.trim();
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

    public Integer getBankerPos() {
        return bankerPos;
    }

    public void setBankerPos(Integer bankerPos) {
        this.bankerPos = bankerPos;
    }

    public Integer getWinerPos() {
        return winerPos;
    }

    public void setWinerPos(Integer winerPos) {
        this.winerPos = winerPos;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail == null ? null : detail.trim();
    }

    public String getPlayBackPath() {
        return playBackPath;
    }

    public void setPlayBackPath(String playBackPath) {
        this.playBackPath = playBackPath;
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
        UserRoomResultDetail other = (UserRoomResultDetail) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getRoomId() == null ? other.getRoomId() == null : this.getRoomId().equals(other.getRoomId()))
            && (this.getRoomName() == null ? other.getRoomName() == null : this.getRoomName().equals(other.getRoomName()))
            && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
            && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
            && (this.getBankerPos() == null ? other.getBankerPos() == null : this.getBankerPos().equals(other.getBankerPos()))
            && (this.getWinerPos() == null ? other.getWinerPos() == null : this.getWinerPos().equals(other.getWinerPos()))
            && (this.getDetail() == null ? other.getDetail() == null : this.getDetail().equals(other.getDetail())
            && (this.getPlayBackPath() == null ? other.getPlayBackPath() == null : this.getPlayBackPath().equals(other.getPlayBackPath())));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getRoomId() == null) ? 0 : getRoomId().hashCode());
        result = prime * result + ((getRoomName() == null) ? 0 : getRoomName().hashCode());
        result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
        result = prime * result + ((getBankerPos() == null) ? 0 : getBankerPos().hashCode());
        result = prime * result + ((getWinerPos() == null) ? 0 : getWinerPos().hashCode());
        result = prime * result + ((getDetail() == null) ? 0 : getDetail().hashCode());
        result = prime * result + ((getPlayBackPath() == null) ? 0 : getPlayBackPath().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", roomId=").append(roomId);
        sb.append(", roomName=").append(roomName);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", bankerPos=").append(bankerPos);
        sb.append(", winerPos=").append(winerPos);
        sb.append(", detail=").append(detail);
        sb.append(", playBackPath=").append(playBackPath);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}