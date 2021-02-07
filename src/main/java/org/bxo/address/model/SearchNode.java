package org.bxo.address.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

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

		queries.addAll(getWords(address.getLine1()));
		queries.addAll(getWords(address.getLine2()));
		queries.addAll(getWords(address.getCity()));
		queries.addAll(getWords(address.getState()));
		queries.addAll(getWords(address.getZip()));

		// StringUtils.isAlphanumeric(prefix)
		for (String query : queries) {
			addAddress(query, addressId);
		}
	}

	public void removeAddress(AddressInfo address) {
		UUID addressId = address.getAddressId();
		Set<String> queries = ConcurrentHashMap.newKeySet();

		queries.addAll(getWords(address.getLine1()));
		queries.addAll(getWords(address.getLine2()));
		queries.addAll(getWords(address.getCity()));
		queries.addAll(getWords(address.getState()));
		queries.addAll(getWords(address.getZip()));

		for (String query : queries) {
			removeAddress(query, addressId);
		}
	}

	private Set<UUID> getAllAddresses(int maxResults, boolean excludeChildren) {
		Set<UUID> result = ConcurrentHashMap.newKeySet();
		result.addAll(addressSet);

		if (!excludeChildren) {
			// Variable childList is to prevent locking childMap
			// while executing for loop
			List<SearchNode> childList = new ArrayList<>();
			childList.addAll(childMap.values());

			for (SearchNode c : childList) {
				int gap = maxResults - result.size();
				if (gap <= 0) {
					break;
				}
				Set<UUID> childAddresses = c.getAllAddresses(gap, false);
				result.addAll(childAddresses);
				if (childAddresses.size() == 0) {
					synchronized (this) {
						if (c.getAllAddresses(gap, false).size() == 0) {
							childMap.remove(c.prefix);
							childCount.remove(c.prefix);
						}
					}
				}
			}
		}

		return result;
	}

	public static List<String> getWords(String line) {
		List<String> result = null;
		if (!StringUtils.isBlank(line)) {
			result = Arrays.asList(line.trim().toLowerCase().split("[^\\w]+"));
		}
		if (result == null || (result.size() == 1 && StringUtils.isBlank(result.get(0)))) {
			result = new ArrayList<String>();
		}
		return result;
	}

	public Set<UUID> search(String query, int maxResults, boolean exactMatch) {
		Set<UUID> result = ConcurrentHashMap.newKeySet();

		List<String> words = getWords(query);
		if (words == null || words.size() == 0) {
			return result;
		}

		for (String w : words) {
			int gap = maxResults - result.size();
			if (gap > 0 && !StringUtils.isBlank(w)) {
				Set<UUID> found = searchHelper(w, exactMatch, gap);
				result.addAll(found);
			}
			if (result.size() > maxResults) {
				break;
			}
		}

		return result;
	}

	private Set<UUID> searchHelper(String query, boolean exactMatch, int maxResults) {
		if (!query.startsWith(prefix)) {
			return ConcurrentHashMap.newKeySet();
		}
		if (query.equals(prefix)) {
			return getAllAddresses(maxResults, exactMatch);
		}

		String nextPrefix = query.substring(0, prefix.length() + 1);
		SearchNode child = childMap.get(nextPrefix);
		return (null == child ? ConcurrentHashMap.newKeySet() : child.searchHelper(query, exactMatch, maxResults));
	}

}
