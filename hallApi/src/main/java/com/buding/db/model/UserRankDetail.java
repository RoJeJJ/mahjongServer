package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class UserRankDetail extends BaseModel<Long> implements Serializable {
    private Long id;

    private Integer userId;

    private String gameId;

    private Integer point;

    private Integer pointType;

    private Long groupDatetime;

    private Date ctime;

    private Date mtime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId == null ? null : gameId.trim();
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public Integer getPointType() {
        return pointType;
    }

    public void setPointType(Integer pointType) {
        this.pointType = pointType;
    }

    public Long getGroupDatetime() {
        return groupDatetime;
    }

    public void setGroupDatetime(Long groupDatetime) {
        this.groupDatetime = groupDatetime;
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
        UserRankDetail other = (UserRankDetail) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getGameId() == null ? other.getGameId() == null : this.getGameId().equals(other.getGameId()))
            && (this.getPoint() == null ? other.getPoint() == null : this.getPoint().equals(other.getPoint()))
            && (this.getPointType() == null ? other.getPointType() == null : this.getPointType().equals(other.getPointType()))
            && (this.getGroupDatetime() == null ? other.getGroupDatetime() == null : this.getGroupDatetime().equals(other.getGroupDatetime()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()))
            && (this.getMtime() == null ? other.getMtime() == null : this.getMtime().equals(other.getMtime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getGameId() == null) ? 0 : getGameId().hashCode());
        result = prime * result + ((getPoint() == null) ? 0 : getPoint().hashCode());
        result = prime * result + ((getPointType() == null) ? 0 : getPointType().hashCode());
        result = prime * result + ((getGroupDatetime() == null) ? 0 : getGroupDatetime().hashCode());
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
        sb.append(", userId=").append(userId);
        sb.append(", gameId=").append(gameId);
        sb.append(", point=").append(point);
        sb.append(", pointType=").append(pointType);
        sb.append(", groupDatetime=").append(groupDatetime);
        sb.append(", ctime=").append(ctime);
        sb.append(", mtime=").append(mtime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}