package org.barrelorgandiscovery.bookimage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LRUCache<K,V> {
	
	public static class Data<K,V> {
		
		K key;
		V data;
		
		public Data(K key, V value) {
			this.key = key;
			this.data = value;
		}
		
		V getValue() {
			return data;
		}
	}
	
	
	private int size;
	private Map<K, Data<K,V>> cache;
	private LinkedList<Data<K,V>> dataList;
	public LRUCache(int size) {
		super();
		this.size = size;
		this.cache = new HashMap<>();
		this.dataList = new LinkedList<>();

	}
	
	public void clear() {
		this.cache = new HashMap<>();
		this.dataList = new LinkedList<>();
	}
	
	public V get(K key) {
		if (cache.containsKey(key)) {
			Data<K,V> data = cache.get(key);
			// Remove the data from its location
			dataList.remove(data);
			// Add it to the end of the list
			dataList.add(data);
			return data.getValue();
		}
		return null;
	}
	public void set(K key, V value) {
		if (cache.containsKey(key)) {
			Data<K,V> oldData = cache.get(key);
			// Remove old data from linkedlist
			dataList.remove(oldData);
			Data<K,V> newData = new Data<>(key, value);
			// Update the value
			cache.put(key, newData);
			// Add new data at the end of the linkedlist
			dataList.add(newData);
		} else {
			Data<K,V> data = new Data<>(key, value);
			if (cache.size() >= size) {
				// Remove the oldest value from both map and linkedlist
				cache.remove(dataList.pollFirst().key);
			}
			cache.put(key, data);
			dataList.add(data);
		}
	}
}