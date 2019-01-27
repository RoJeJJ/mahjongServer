package com.buding.common.cache;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public interface CacheClient {
	void set(String key, Object val);
	<T> T get(String key, Class<T> cls);
	void del(String key);
	void hset(String key, String field, Object val);
	<T> T hget(String key, String field, Class<T> cls);
	void hdel(String key, String field);
}
