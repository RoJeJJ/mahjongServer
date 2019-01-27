package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class UserRoom extends BaseModel<Long> implements Serializable {
    private Long id;

    private Integer ownerId;

    private String roomCode;

    private String roomPwd;

    private String roomName;

    private Integer roomState;

    private String gameId;

    private String matchId;

    private String wanfa;

    private String roomConfId;

    private Date lastActiveTime;

    private Integer usedCount;

    private Integer totalCount;

    private String params;

    private Date ctime;

    private Date mtime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode == null ? null : roomCode.trim();
    }

    public String getRoomPwd() {
        return roomPwd;
    }

    public void setRoomPwd(String roomPwd) {
        this.roomPwd = roomPwd == null ? null : roomPwd.trim();
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName == null ? null : roomName.trim();
    }

    public Integer getRoomState() {
        return roomState;
    }

    public void setRoomState(Integer roomState) {
        this.roomState = roomState;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId == null ? null : gameId.trim();
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId == null ? null : matchId.trim();
    }

    public String getWanfa() {
        return wanfa;
    }

    public void setWanfa(String wanfa) {
        this.wanfa = wanfa == null ? null : wanfa.trim();
    }

    public String getRoomConfId() {
        return roomConfId;
    }

    public void setRoomConfId(String roomConfId) {
        this.roomConfId = roomConfId == null ? null : roomConfId.trim();
    }

    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params == null ? null : params.trim();
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
        UserRoom other = (UserRoom) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getOwnerId() == null ? other.getOwnerId() == null : this.getOwnerId().equals(other.getOwnerId()))
            && (this.getRoomCode() == null ? other.getRoomCode() == null : this.getRoomCode().equals(other.getRoomCode()))
            && (this.getRoomPwd() == null ? other.getRoomPwd() == null : this.getRoomPwd().equals(other.getRoomPwd()))
            && (this.getRoomName() == null ? other.getRoomName() == null : this.getRoomName().equals(other.getRoomName()))
            && (this.getRoomState() == null ? other.getRoomState() == null : this.getRoomState().equals(other.getRoomState()))
            && (this.getGameId() == null ? other.getGameId() == null : this.getGameId().equals(other.getGameId()))
            && (this.getMatchId() == null ? other.getMatchId() == null : this.getMatchId().equals(other.getMatchId()))
            && (this.getWanfa() == null ? other.getWanfa() == null : this.getWanfa().equals(other.getWanfa()))
            && (this.getRoomConfId() == null ? other.getRoomConfId() == null : this.getRoomConfId().equals(other.getRoomConfId()))
            && (this.getLastActiveTime() == null ? other.getLastActiveTime() == null : this.getLastActiveTime().equals(other.getLastActiveTime()))
            && (this.getUsedCount() == null ? other.getUsedCount() == null : this.getUsedCount().equals(other.getUsedCount()))
            && (this.getTotalCount() == null ? other.getTotalCount() == null : this.getTotalCount().equals(other.getTotalCount()))
            && (this.getParams() == null ? other.getParams() == null : this.getParams().equals(other.getParams()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()))
            && (this.getMtime() == null ? other.getMtime() == null : this.getMtime().equals(other.getMtime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getOwnerId() == null) ? 0 : getOwnerId().hashCode());
        result = prime * result + ((getRoomCode() == null) ? 0 : getRoomCode().hashCode());
        result = prime * result + ((getRoomPwd() == null) ? 0 : getRoomPwd().hashCode());
        result = prime * result + ((getRoomName() == null) ? 0 : getRoomName().hashCode());
        result = prime * result + ((getRoomState() == null) ? 0 : getRoomState().hashCode());
        result = prime * result + ((getGameId() == null) ? 0 : getGameId().hashCode());
        result = prime * result + ((getMatchId() == null) ? 0 : getMatchId().hashCode());
        result = prime * result + ((getWanfa() == null) ? 0 : getWanfa().hashCode());
        result = prime * result + ((getRoomConfId() == null) ? 0 : getRoomConfId().hashCode());
        result = prime * result + ((getLastActiveTime() == null) ? 0 : getLastActiveTime().hashCode());
        result = prime * result + ((getUsedCount() == null) ? 0 : getUsedCount().hashCode());
        result = prime * result + ((getTotalCount() == null) ? 0 : getTotalCount().hashCode());
        result = prime * result + ((getParams() == null) ? 0 : getParams().hashCode());
        result = prime * result + ((getCtime() == null) ? 0 : getCtime().hashCode());
        result = prime * result + ((getMtime() == null) ? 0 : getMtime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", ownerId=").append(ownerId);
        sb.append(", roomCode=").append(roomCode);
        sb.append(", roomPwd=").append(roomPwd);
        sb.append(", roomName=").append(roomName);
        sb.append(", roomState=").append(roomState);
        sb.append(", gameId=").append(gameId);
        sb.append(", matchId=").append(matchId);
        sb.append(", wanfa=").append(wanfa);
        sb.append(", roomConfId=").append(roomConfId);
        sb.append(", lastActiveTime=").append(lastActiveTime);
        sb.append(", usedCount=").append(usedCount);
        sb.append(", totalCount=").append(totalCount);
        sb.append(", params=").append(params);
        sb.append(", ctime=").append(ctime);
        sb.append(", mtime=").append(mtime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}