package com.buding.hall.module.task.dao;

import java.util.List;

import com.buding.db.model.UserTask;

public interface TaskDao {
	/**
	 * 插入任务
	 * @param task
	 * @return
	 */
	long insert(UserTask task);
	
	/**
	 * 更新任务
	 * @param task
	 */
	void update(UserTask task);
	
	UserTask get(long id);
	
	/**
	 * 
	 * @param taskType
	 * @param userId
	 * @return
	 */
	List<UserTask> getUnderingTask(int taskType, int userId);
	UserTask getLatestUserTask(int userId, String taskId);
	UserTask getLatestUserTask(int userId, String taskId, int day);
	List<UserTask> getUserTaskList(int userId, String taskId, int day);
	List<UserTask> getUserUnderingTask(int userId);
}
