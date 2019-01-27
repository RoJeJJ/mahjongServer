package com.buding.common.db.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface CommonDao {

	@SuppressWarnings("unchecked")
	<T> void save(T... paramArrayOfT);

	@SuppressWarnings("unchecked")
	<T> void update(T... paramArrayOfT);

	<T> void update(Collection<T> paramCollection);

	<T> T get(Serializable paramSerializable, Class<T> paramClass);

	<T> void delete(Serializable paramSerializable, Class<T> paramClass);
	
	<T> T selectOne(String sql, Class<T> resultType, Object... args);
	
	<T> List<T> selectList(String sql, Class<T> resultType, Object... args);
	
	<T> int count(String sql, Object... args) ;
}