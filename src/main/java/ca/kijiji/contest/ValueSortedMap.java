package ca.kijiji.contest;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.SortedMap;

/*
 * A Key -> Integer map, sorted in descending order of its values.
 * The sorting is handled by the standard library's ConcurrentSkipListMap.
 * A HashMap is used to access the values by the keys, effectively giving
 * the users a map whose entries are sorted by value.
 */
public class ValueSortedMap<K> extends ConcurrentHashMap<K,Integer> implements SortedMap<K, Integer> {
	
	/* 
	 * Use the ConcurrentSkipListMap implementation to manage all the sorting
	 */
	private final ConcurrentSkipListMap<Integer,K> sortedByValue;
	
	/*
	 * Constructs a ValueSorted map that sorts in descending order
	 */
	public ValueSortedMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
		super(initialCapacity, loadFactor, concurrencyLevel);
		Comparator<Integer> descending = new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return o2.compareTo(o1);
			}
		};
		sortedByValue = new ConcurrentSkipListMap<Integer,K>(descending);
	}
	
	/*
	 * Adds the entry to the map, adding the value to the existing one if it already exists
	 */
	public synchronized void add(K key, Integer value) {
		Integer previousValue = get(key);
		if (previousValue == null) {
			put(key, value);
			sortedByValue.put(value, key);
		} else {
			int newValue = previousValue + value;
			put(key, newValue);
			sortedByValue.put(newValue, key);
		}
	}
	
	public K firstKey() {
		return sortedByValue.get(sortedByValue.firstKey());
	}
	
    public K lastKey(){
		return sortedByValue.get(sortedByValue.lastKey());
	}
    
	public Comparator<K> comparator() {
		Comparator<K> compareKeyByValue = new Comparator<K>() {

			@Override
			public int compare(K o1, K o2) {
				return get(o1).compareTo(get(o2));
			}
		};
		return compareKeyByValue;
	}
	
	/*
	 * A private constructor used to facilitate making submaps
	 * 
	 * @see
	 */
	private ValueSortedMap(ConcurrentNavigableMap<Integer,K> fromMap) {
		
		sortedByValue = new ConcurrentSkipListMap<Integer,K>();
		sortedByValue.putAll(fromMap);
		
		for (Integer value : sortedByValue.keySet()) {
			K key = sortedByValue.get(value);
			put(key, value);
		}
	}
	
	public SortedMap<K,Integer> subMap(K fromKey, K toKey){
		Integer fromValue = get(fromKey);
		Integer toValue = get(toKey);
		return new ValueSortedMap<K>(sortedByValue.subMap(fromValue, toValue));
	}
	
    public SortedMap<K,Integer> headMap(K toKey){
		Integer toValue = get(toKey);
		return new ValueSortedMap<K>(sortedByValue.headMap(toValue));
	}
    
    public SortedMap<K,Integer> tailMap(K fromKey){
		Integer fromValue = get(fromKey);
		return new ValueSortedMap<K>(sortedByValue.tailMap(fromValue));
	}
}
