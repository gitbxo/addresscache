package org.bxo.address.service;

import java.util.List;
import java.util.UUID;

import org.bxo.address.model.AddressInfo;

public interface AddressService {

	public AddressInfo getAddress(UUID addressId);

	public List<AddressInfo> search(String query, long maxResults);

	public AddressInfo createAddress(AddressInfo address);

	public AddressInfo updateAddress(AddressInfo address);

	public void deleteAddress(UUID addressId);

}
