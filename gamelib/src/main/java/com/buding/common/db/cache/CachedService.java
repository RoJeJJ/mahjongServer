package com.buding.common.db.cache;

public interface CachedService {
	Object put2EntityCache(String paramString, Object paramObject);

	Object put2EntityCache(String paramString, Object paramObject, long paramLong);

	Object getFromEntityCache(String paramString);

	void removeFromEntityCache(String paramString);

	void put2CommonCache(String paramString, Object paramObject);

	void put2CommonCache(String paramString, Object paramObject, long paramLong);

	void put2CommonHashCache(String paramString1, String paramString2, Object paramObject);

	Object put2CommonHashCacheIfAbsent(String paramString1, String paramString2, Object paramObject);

	void put2CommonHashCache(String paramString1, String paramString2, Object paramObject, long paramLong);

	Object getFromCommonCache(String paramString);

	Object getFromCommonCache(String paramString1, String paramString2);

	void removeFromCommonCache(String paramString);

	void removeFromCommonHashCache(String paramString1, String paramString2);
}