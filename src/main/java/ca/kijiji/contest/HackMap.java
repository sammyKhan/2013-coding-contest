package ca.kijiji.contest;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class HackMap extends ConcurrentHashMap<String, Integer> implements SortedMap<String, Integer>{
   
	private Integer highestValue;
	private String keyOfHighestValue;
	
	public HackMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
		super(initialCapacity, loadFactor, concurrencyLevel);
		highestValue = 0;
	}
	
	public synchronized void addToValue(String key, Integer value) {
		Integer previousValue = this.get(key);
		if (previousValue == null) {
			this.put(key, value);
		} else {
			this.put(key, previousValue + value);
		}
	}
	
	public synchronized Integer put(String key, Integer value) {
		super.put(key, value);
		if (value.compareTo(highestValue) > 0) {
			highestValue = value;
			keyOfHighestValue = key;
		}
		return value;
	}
	
	public String firstKey() {
		return keyOfHighestValue;
	}
	
	public Comparator<String> comparator() {
		return null;
	}

	public SortedMap<String,Integer> subMap(String fromKey, String toKey){
		return null;
	}
    public SortedMap<String,Integer> headMap(String toKey){
		return null;
	}
    public SortedMap<String,Integer> tailMap(String fromKey){
		return null;
	}
    public String lastKey(){
		return null;
	}
    public Set<String> keySet(){
		return null;
	}
    public Collection<Integer> values(){
		return null;
	}
    public Set<Map.Entry<String, Integer>> entrySet(){
		return null;
	}
}
