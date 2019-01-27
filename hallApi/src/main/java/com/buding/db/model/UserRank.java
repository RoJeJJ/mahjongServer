package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;

public class UserRank extends BaseModel<Long> implements Serializable {
    private Long id;

    private Long auditId;

    private Integer userId;

    private String userName;

    private Integer rankPoint;

    private Integer rank;

    private Boolean awardStatus;

    private Integer vipType;

    private String awardDesc;

    private String awards;

    private String groupId;

    private Long rankGrpTime;

    private Date mtime;

    private Date ctime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public Integer getRankPoint() {
        return rankPoint;
    }

    public void setRankPoint(Integer rankPoint) {
        this.rankPoint = rankPoint;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Boolean getAwardStatus() {
        return awardStatus;
    }

    public void setAwardStatus(Boolean awardStatus) {
        this.awardStatus = awardStatus;
    }

    public Integer getVipType() {
        return vipType;
    }

    public void setVipType(Integer vipType) {
        this.vipType = vipType;
    }

    public String getAwardDesc() {
        return awardDesc;
    }

    public void setAwardDesc(String awardDesc) {
        this.awardDesc = awardDesc == null ? null : awardDesc.trim();
    }

    public String getAwards() {
        return awards;
    }

    public void setAwards(String awards) {
        this.awards = awards == null ? null : awards.trim();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId == null ? null : groupId.trim();
    }

    public Long getRankGrpTime() {
        return rankGrpTime;
    }

    public void setRankGrpTime(Long rankGrpTime) {
        this.rankGrpTime = rankGrpTime;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
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
        UserRank other = (UserRank) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAuditId() == null ? other.getAuditId() == null : this.getAuditId().equals(other.getAuditId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUserName() == null ? other.getUserName() == null : this.getUserName().equals(other.getUserName()))
            && (this.getRankPoint() == null ? other.getRankPoint() == null : this.getRankPoint().equals(other.getRankPoint()))
            && (this.getRank() == null ? other.getRank() == null : this.getRank().equals(other.getRank()))
            && (this.getAwardStatus() == null ? other.getAwardStatus() == null : this.getAwardStatus().equals(other.getAwardStatus()))
            && (this.getVipType() == null ? other.getVipType() == null : this.getVipType().equals(other.getVipType()))
            && (this.getAwardDesc() == null ? other.getAwardDesc() == null : this.getAwardDesc().equals(other.getAwardDesc()))
            && (this.getAwards() == null ? other.getAwards() == null : this.getAwards().equals(other.getAwards()))
            && (this.getGroupId() == null ? other.getGroupId() == null : this.getGroupId().equals(other.getGroupId()))
            && (this.getRankGrpTime() == null ? other.getRankGrpTime() == null : this.getRankGrpTime().equals(other.getRankGrpTime()))
            && (this.getMtime() == null ? other.getMtime() == null : this.getMtime().equals(other.getMtime()))
            && (this.getCtime() == null ? other.getCtime() == null : this.getCtime().equals(other.getCtime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAuditId() == null) ? 0 : getAuditId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUserName() == null) ? 0 : getUserName().hashCode());
        result = prime * result + ((getRankPoint() == null) ? 0 : getRankPoint().hashCode());
        result = prime * result + ((getRank() == null) ? 0 : getRank().hashCode());
        result = prime * result + ((getAwardStatus() == null) ? 0 : getAwardStatus().hashCode());
        result = prime * result + ((getVipType() == null) ? 0 : getVipType().hashCode());
        result = prime * result + ((getAwardDesc() == null) ? 0 : getAwardDesc().hashCode());
        result = prime * result + ((getAwards() == null) ? 0 : getAwards().hashCode());
        result = prime * result + ((getGroupId() == null) ? 0 : getGroupId().hashCode());
        result = prime * result + ((getRankGrpTime() == null) ? 0 : getRankGrpTime().hashCode());
        result = prime * result + ((getMtime() == null) ? 0 : getMtime().hashCode());
        result = prime * result + ((getCtime() == null) ? 0 : getCtime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", auditId=").append(auditId);
        sb.append(", userId=").append(userId);
        sb.append(", userName=").append(userName);
        sb.append(", rankPoint=").append(rankPoint);
        sb.append(", rank=").append(rank);
        sb.append(", awardStatus=").append(awardStatus);
        sb.append(", vipType=").append(vipType);
        sb.append(", awardDesc=").append(awardDesc);
        sb.append(", awards=").append(awards);
        sb.append(", groupId=").append(groupId);
        sb.append(", rankGrpTime=").append(rankGrpTime);
        sb.append(", mtime=").append(mtime);
        sb.append(", ctime=").append(ctime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}