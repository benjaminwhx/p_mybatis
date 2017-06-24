package com.github.core.cache;

import org.apache.ibatis.cache.Cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: 吴海旭
 * Date: 2017-06-13
 * Time: 下午10:20
 */
public class MyCache implements Cache {

	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private ConcurrentHashMap<Object, Object> cache = new ConcurrentHashMap<Object, Object>();
	private String id;

	public MyCache() {
	}

	public MyCache(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void putObject(Object key, Object value) {
		System.out.println("putObject---key:" + key);
		cache.put(key, value);
	}

	@Override
	public Object getObject(Object key) {
		System.out.println("getObject---key:" + key);
		return cache.get(key);
	}

	@Override
	public Object removeObject(Object key) {
		System.out.println("remove:" + key);
		return cache.remove(key);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public int getSize() {
		System.out.println("getsize");
		return cache.size();
	}

	@Override
	public ReadWriteLock getReadWriteLock() {
		return lock;
	}
}
