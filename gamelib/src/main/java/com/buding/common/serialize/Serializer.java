package com.buding.common.serialize;

import java.util.List;
import java.util.Map;

/**
 * @author tiny qq_381360993
 * @Description:
 * 
 */
public interface Serializer {
	<T> byte[] serialize(T obj) throws Exception;
	<T> T deserialize(byte[] data, Class<T> cls) throws Exception;
	

	<T> byte[] serializeList(List<T> obj, Class<T> cls) throws Exception;
	<T> List<T> deserializeList(byte[] data, Class<T> cls) throws Exception;
	
	<K,V> byte[] serializeMap(Map<K, V> obj, Class<K> keyClas, Class<V> valCls) throws Exception;
	<K,V> Map<K,V> deserializeMap(byte[] data, Class<K> keyCls, Class<V> valCls) throws Exception;
}
