package org.bxo.address.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bxo.address.model.AddressInfo;
import org.bxo.address.model.SearchNode;
import org.bxo.address.service.AddressService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl implements AddressService {

	private static final SearchNode rootNode = new SearchNode("");
	private static final ConcurrentHashMap<UUID, AddressInfo> addressMap = new ConcurrentHashMap<>();

	@Value("${org.bxo.address.allow-dups:true}")
	private Boolean allowDups;

	@Value("${org.bxo.address.default-max-results:10}")
	private Integer defaultMaxResults;

	@Override
	public AddressInfo getAddress(UUID addressId) {
		AddressInfo address = addressMap.get(addressId);
		return (null == address ? null : new AddressInfo(address));
	}

	@Override
	public AddressInfo createAddress(AddressInfo address) {
		UUID addressId = address.getAddressId();
		if (!allowDups) {
			AddressInfo found = this.findDuplicate(address.getTextAddress());
			if (null != found) {
				return found;
			}
		}

		addressMap.putIfAbsent(addressId, new AddressInfo(address));
		AddressInfo result = getAddress(addressId);
		rootNode.addAddress(result);
		return result;
	}

	@Override
	public List<AddressInfo> search(String query, int pageNumber, int resultsPerPage, int maxResults, boolean exactMatch, boolean requireAll) {
		List<AddressInfo> addressList = new ArrayList<>();
		if (pageNumber < 1)
			pageNumber = 1;
		if (pageNumber * resultsPerPage < maxResults)
			maxResults = pageNumber * resultsPerPage;

		if (requireAll && exactMatch) {
			List<String> words = SearchNode.getWords(query);
			if (words == null || words.size() == 0) {
				return addressList;
			}

			for (UUID a : rootNode.search(getLongestWord(words), (maxResults < 1 ? defaultMaxResults : maxResults),
					exactMatch)) {
				AddressInfo address = getAddress(a);
				if (null != address) {
					boolean found = true;
					List<String> addrWords = SearchNode.getWords(address.getTextAddress());
					for (String w : words) {
						if (!addrWords.contains(w)) {
							found = false;
							break;
						}
					}
					if (found) {
						addressList.add(address);
					}
				}
			}

		} else if (requireAll) {
			// requireAll can only be set when exactMatch is true
			throw new IllegalArgumentException("requireAll can only be set when exactMatch is true");

		} else {
			for (UUID a : rootNode.search(query, (maxResults < 1 ? defaultMaxResults : maxResults), exactMatch)) {
				AddressInfo address = getAddress(a);
				if (null != address) {
					addressList.add(address);
				}
			}
		}

		List<AddressInfo> resultList= new ArrayList<>();
		for (int i=0; i < resultsPerPage; i++) {
			int idx = (pageNumber - 1) * resultsPerPage + i;
			if (idx >= 0 && addressList.size() > idx)
				resultList.add(addressList.get(idx));
		}

		return resultList;
	}

	private String getLongestWord(List<String> words) {
		String longestWord = words.get(0);
		for (String w : words) {
			if (w.length() > longestWord.length()) {
				longestWord = w;
			}
		}
		return longestWord;
	}

	private AddressInfo findDuplicate(String addressText) {
		List<AddressInfo> found = this.search(addressText, 1, defaultMaxResults, defaultMaxResults, true, true);
		if (found != null && found.size() > 0) {
			for (AddressInfo a : found) {
				if (a.getTextAddress().equals(addressText)) {
					return a;
				}
			}
		}

		return null;
	}

	@Override
	public AddressInfo updateAddress(AddressInfo update) {
		AddressInfo updated = new AddressInfo(update);
		String updatedText = updated.getTextAddress();
		AddressInfo previous = getAddress(updated.getAddressId());
		String previousText = previous.getTextAddress();
		if (previousText.equals(updatedText)) {
			return updated;
		}

		if (!allowDups) {
			if (this.findDuplicate(updatedText) != null) {
				// Update matches existing address
				throw new IllegalArgumentException("Update matches existing address");
			}
		}

		addressMap.put(updated.getAddressId(), updated);
		rootNode.removeAddress(previous);
		rootNode.addAddress(updated);
		return new AddressInfo(updated);
	}

	@Override
	public AddressInfo deleteAddress(UUID addressId) {
		AddressInfo address = addressMap.remove(addressId);
		if (null != address) {
			rootNode.removeAddress(address);
		}
		return address;
	}

}
