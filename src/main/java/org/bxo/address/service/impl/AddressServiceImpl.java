package org.bxo.address.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//import org.springframework.web.bind.annotation.RequestParam;
import org.bxo.address.model.AddressInfo;
import org.bxo.address.model.SearchNode;
import org.bxo.address.service.AddressService;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl implements AddressService {

	private static final SearchNode rootNode = new SearchNode("");
	private static final ConcurrentHashMap<UUID, AddressInfo> addressMap = new ConcurrentHashMap<>();

	@Override
	public AddressInfo getAddress(UUID addressId) {
		AddressInfo address = addressMap.get(addressId);
		return (null == address ? null : new AddressInfo(address));
	}

	@Override
	public AddressInfo createAddress(AddressInfo address) {
		UUID addressId = address.getAddressId();
		addressMap.putIfAbsent(addressId, new AddressInfo(address));
		AddressInfo result = getAddress(addressId);
		rootNode.addAddress(result);
		return result;
	}

	@Override
	public List<AddressInfo> search(String query, long maxResults) {
		List<AddressInfo> addressList = new ArrayList<>();
		for (UUID a : rootNode.search(query, maxResults)) {
			AddressInfo address = getAddress(a);
			if (null != address) {
				addressList.add(address);
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
	public void deleteAddress(UUID addressId) {
		AddressInfo address = addressMap.remove(addressId);
		if (null != address) {
			rootNode.removeAddress(address);
		}
	}

}
