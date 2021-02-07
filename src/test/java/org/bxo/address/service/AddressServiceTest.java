package org.bxo.address.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.bxo.address.model.AddressInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AddressServiceTest {

	@Autowired
	private AddressService addressService;

	@Test
	public void createAddress_saves() {
		UUID addressId = UUID.randomUUID();
		AddressInfo address = new AddressInfo(addressId);
		address.setLine1("line1");
		address.setLine1("line2");
		address.setCity("city");
		address.setState("ST");
		address.setZip("12345");
		addressService.createAddress(address);

		assertThat(addressService.getAddress(addressId).toString(), is(address.toString()));
	}

	@Test
	public void updateAddress_saves() {
		UUID addressId = UUID.randomUUID();
		AddressInfo address1 = new AddressInfo(addressId);
		address1.setLine1("address 1a");
		address1.setLine1("address 1b");
		address1.setCity("city1");
		address1.setState("ST");
		address1.setZip("12345");
		addressService.createAddress(address1);
		assertThat(addressService.search("12345", 10, false, false).size(), is(1));
		assertThat(addressService.search("23456", 10, false, false).size(), is(0));

		AddressInfo address2 = addressService.getAddress(addressId);
		address2.setLine1("address 2a");
		address2.setLine2("address 2b");
		address2.setCity("city2");
		address2.setState("S2");
		address2.setZip("23456");
		addressService.updateAddress(address2);
		assertThat(addressService.search("12345", 10, false, false).size(), is(0));
		assertThat(addressService.search("23456", 10, false, false).size(), is(1));

		assertThat(addressService.getAddress(addressId).toString(), is(address2.toString()));
	}

	@Test
	public void deleteAddress_deletes() {
		UUID addressId = UUID.randomUUID();
		AddressInfo address = new AddressInfo(addressId);
		address.setLine1("line1");
		address.setLine1("line2");
		address.setCity("city");
		address.setState("ST");
		address.setZip("12345");
		addressService.createAddress(address);

		assertThat(addressService.getAddress(addressId).toString(), is(address.toString()));

		addressService.deleteAddress(addressId);

		assertThat(addressService.getAddress(addressId), is(nullValue()));
	}

}
