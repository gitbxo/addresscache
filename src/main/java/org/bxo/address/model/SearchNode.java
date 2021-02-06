package org.bxo.address.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SearchNode {

	private String prefix;
	private Set<UUID> addressSet;
	private Map<String, AtomicLong> childCount;
	private Map<String, SearchNode> childMap;

	public SearchNode(String prefix) {
		this.prefix = prefix;
		this.addressSet = ConcurrentHashMap.newKeySet();
		this.childCount = new ConcurrentHashMap<>();
		this.childMap = new ConcurrentHashMap<>();
	}

	protected boolean addAddress(String query, UUID addressId) {
		if (!query.startsWith(prefix)) {
			return false;
		}
		if (query.equals(prefix)) {
			return addressSet.add(addressId);
		}

		String nextPrefix = query.substring(0, prefix.length() + 1);

		// Below synchronized block is to ensure that childMap is not
		// removed while executing
		synchronized (this) {
			if (!childMap.containsKey(nextPrefix)) {
				childCount.putIfAbsent(nextPrefix, new AtomicLong(0L));
				childMap.putIfAbsent(nextPrefix, new SearchNode(nextPrefix));
			}
			boolean childAdded = childMap.get(nextPrefix).addAddress(query, addressId);
			if (childAdded) {
				childCount.get(nextPrefix).incrementAndGet();
			}
			return childAdded;
		}
	}

	protected boolean removeAddress(String query, UUID addressId) {
		if (!query.startsWith(prefix)) {
			return false;
		}
		if (query.equals(prefix)) {
			return addressSet.remove(addressId);
		}

		String nextPrefix = query.substring(0, prefix.length() + 1);
		SearchNode child = childMap.get(nextPrefix);
		boolean childRemoved = (null == child ? false : child.removeAddress(query, addressId));
		if (childRemoved) {
			if (0 == childCount.get(nextPrefix).decrementAndGet()) {
				synchronized (this) {
					if (0 == childCount.get(nextPrefix).longValue()) {
						childMap.remove(nextPrefix);
						childCount.remove(nextPrefix);
					}
				}
			}
		}
		return childRemoved;
	}

	public void addAddress(AddressInfo address) {
		UUID addressId = address.getAddressId();
		Set<String> queries = ConcurrentHashMap.newKeySet();

		List<String> line1Words = Arrays.asList(address.getLine1().trim().toLowerCase().split("\\w+"));
		queries.addAll(line1Words);
		List<String> line2Words = Arrays.asList(address.getLine2().trim().toLowerCase().split("\\w+"));
		queries.addAll(line2Words);
		List<String> cityWords = Arrays.asList(address.getCity().trim().toLowerCase().split("\\w+"));
		queries.addAll(cityWords);
		List<String> stateWords = Arrays.asList(address.getState().trim().toLowerCase().split("\\w+"));
		queries.addAll(stateWords);
		List<String> zipWords = Arrays.asList(address.getZip().trim().toLowerCase().split("\\w+"));
		queries.addAll(zipWords);

		// StringUtils.isAlphanumeric(prefix)
		for (String query : queries) {
			addAddress(query, addressId);
		}
	}

	public void removeAddress(AddressInfo address) {
		UUID addressId = address.getAddressId();
		Set<String> queries = ConcurrentHashMap.newKeySet();

		List<String> line1Words = Arrays.asList(address.getLine1().trim().toLowerCase().split("\\w+"));
		queries.addAll(line1Words);
		List<String> line2Words = Arrays.asList(address.getLine2().trim().toLowerCase().split("\\w+"));
		queries.addAll(line2Words);
		List<String> cityWords = Arrays.asList(address.getCity().trim().toLowerCase().split("\\w+"));
		queries.addAll(cityWords);
		List<String> stateWords = Arrays.asList(address.getState().trim().toLowerCase().split("\\w+"));
		queries.addAll(stateWords);
		List<String> zipWords = Arrays.asList(address.getZip().trim().toLowerCase().split("\\w+"));
		queries.addAll(zipWords);

		for (String query : queries) {
			removeAddress(query, addressId);
		}
	}

	public Set<UUID> getAllAddresses(long maxResults) {
		Set<UUID> result = ConcurrentHashMap.newKeySet();
		result.addAll(addressSet);

		// Variable childList is to prevent locking childMap
		// while executing for loop
		List<SearchNode> childList = new ArrayList<>();
		childList.addAll(childMap.values());

		for (SearchNode c : childList) {
			long gap = maxResults - result.size();
			if (gap > 0) {
				result.addAll(c.getAllAddresses(gap));
			}
		}

		return result;
	}

	public Set<UUID> search(String query, long maxResults) {
		if (!query.startsWith(prefix)) {
			return null;
		}
		if (query.equals(prefix)) {
			return getAllAddresses(maxResults);
		}

		String nextPrefix = query.substring(0, prefix.length() + 1);
		SearchNode child = childMap.get(nextPrefix);
		return (null == child ? null : child.search(query, maxResults));
	}

}
