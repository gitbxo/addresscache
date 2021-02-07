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

	@Value("${org.bxo.address.allowDups:true}")
	private Boolean allowDups;

	@Override
	public AddressInfo getAddress(UUID addressId) {
		AddressInfo address = addressMap.get(addressId);
		return (null == address ? null : new AddressInfo(address));
	}

	@Override
	public AddressInfo createAddress(AddressInfo address) {
		UUID addressId = address.getAddressId();
		if (!allowDups) {
			List<AddressInfo> found = this.search(address.getPrintableAddress(), 30L, true, true);
			if (found != null && found.size() > 0) {
				return new AddressInfo(found.get(0));
			}
		}
		addressMap.putIfAbsent(addressId, new AddressInfo(address));
		AddressInfo result = getAddress(addressId);
		rootNode.addAddress(result);
		return result;
	}

	@Override
	public List<AddressInfo> search(String query, long maxResults, boolean exactMatch, boolean requireAll) {
		List<AddressInfo> addressList = new ArrayList<>();

		if (requireAll && exactMatch) {
			List<String> words = SearchNode.getWords(query);
			if (words == null || words.size() == 0) {
				return addressList;
			}

			String longestWord = words.get(0);
			for (String w : words) {
				if (w.length() > longestWord.length()) {
					longestWord = w;
				}
			}

			for (UUID a : rootNode.search(longestWord, maxResults, exactMatch)) {
				AddressInfo address = getAddress(a);
				if (null != address) {
					boolean found = true;
					List<String> addrWords = SearchNode.getWords(address.getPrintableAddress());
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
			for (UUID a : rootNode.search(query, maxResults, exactMatch)) {
				AddressInfo address = getAddress(a);
				if (null != address) {
					addressList.add(address);
				}
			}
		}

		return addressList;
	}

	@Override
	public AddressInfo updateAddress(AddressInfo update) {
		UUID addressId = update.getAddressId();
		AddressInfo address = addressMap.get(addressId);
		rootNode.removeAddress(address);

		address.setLine1(update.getLine1());
		address.setLine2(update.getLine2());
		address.setCity(update.getCity());
		address.setState(update.getState());
		address.setZip(update.getZip());

		AddressInfo result = getAddress(addressId);
		rootNode.addAddress(result);
		return result;
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
