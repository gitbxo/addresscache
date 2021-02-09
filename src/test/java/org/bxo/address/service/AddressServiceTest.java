package org.bxo.address.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		address.setLine1("create_line1");
		address.setLine1("create_line2");
		address.setCity("create_city");
		address.setState("ST");
		address.setZip("12345");
		addressService.createAddress(address);

		assertThat(addressService.getAddress(addressId).toString(), is(address.toString()));
	}

	@Test
	public void createAddress_duplicate_fails() {
		UUID addressId1 = UUID.randomUUID();
		UUID addressId2 = UUID.randomUUID();
		AddressInfo address1 = new AddressInfo(addressId1);
		AddressInfo address2 = new AddressInfo(addressId2);
		address1.setLine1("duplicate_line1");
		address2.setLine1("duplicate_line1");
		address1.setLine2("duplicate_line2");
		address2.setLine2("duplicate_line2");
		address1.setCity("duplicate_city");
		address2.setCity("duplicate_city");
		address1.setState("ST");
		address2.setState("ST");
		address1.setZip("12345");
		address2.setZip("12345");

		addressService.createAddress(address1);
		assertThat(addressService.getAddress(addressId1).toString(), is(address1.toString()));

		addressService.createAddress(address2);
		assertThat(addressService.getAddress(addressId2), is(nullValue()));
	}

	@Test
	public void createAddress_duplicate_allows_different() {
		UUID addressId1 = UUID.randomUUID();
		UUID addressId2 = UUID.randomUUID();
		AddressInfo address1 = new AddressInfo(addressId1);
		AddressInfo address2 = new AddressInfo(addressId2);
		address1.setLine1("allows_line1");
		address2.setLine1("allows_line2");
		address1.setLine2("allows_line2");
		address2.setLine2("allows_line1");
		address1.setCity("allows_city");
		address2.setCity("allows_city");
		address1.setState("ST");
		address2.setState("ST");
		address1.setZip("12345");
		address2.setZip("12345");

		addressService.createAddress(address1);
		assertThat(addressService.getAddress(addressId1).toString(), is(address1.toString()));

		addressService.createAddress(address2);
		assertThat(addressService.getAddress(addressId2).toString(), is(address2.toString()));
	}

	@Test
	public void updateAddress_saves() {
		UUID addressId = UUID.randomUUID();
		AddressInfo address1 = new AddressInfo(addressId);
		address1.setLine1("address 1a");
		address1.setLine1("address 1b");
		address1.setCity("update_city1");
		address1.setState("ST");
		address1.setZip("12345");
		addressService.createAddress(address1);

		assertThat(addressService.search("update_city1", 10, false, false).size(), is(1));
		assertThat(addressService.search("update_city2", 10, false, false).size(), is(0));
		assertThat(addressService.getAddress(addressId).toString(), is(address1.toString()));

		AddressInfo address2 = addressService.getAddress(addressId);
		address2.setLine1("address 2a");
		address2.setLine2("address 2b");
		address2.setCity("update_city2");
		address2.setState("S2");
		address2.setZip("23456");
		addressService.updateAddress(address2);

		assertThat(addressService.search("update_city1", 10, false, false).size(), is(0));
		assertThat(addressService.search("update_city2", 10, false, false).size(), is(1));
		assertThat(addressService.getAddress(addressId).toString(), is(address2.toString()));
	}

	@Test
	public void updateAddress_duplicate_fails() {
		UUID addressId1 = UUID.randomUUID();
		UUID addressId2 = UUID.randomUUID();
		AddressInfo address1 = new AddressInfo(addressId1);
		AddressInfo address2 = new AddressInfo(addressId2);
		address1.setLine1("duplicate2_line1");
		address2.setLine1("duplicate2_line1");
		address1.setLine2("duplicate2_line2");
		address1.setCity("duplicate2_city");
		address2.setCity("duplicate2_city");
		address1.setState("ST");
		address2.setState("ST");
		address1.setZip("12345");
		address2.setZip("12345");

		addressService.createAddress(address1);
		assertThat(addressService.getAddress(addressId1).toString(), is(address1.toString()));

		addressService.createAddress(address2);
		assertThat(addressService.getAddress(addressId2).toString(), is(address2.toString()));

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			address2.setLine2("duplicate2_line2");
			addressService.updateAddress(address2);
		});

		assertThat(exception.getMessage(), is("Update matches existing address"));
	}

	@Test
	public void deleteAddress_deletes() {
		UUID addressId = UUID.randomUUID();
		AddressInfo address = new AddressInfo(addressId);
		address.setLine1("delete_line1");
		address.setLine1("delete_line2");
		address.setCity("delete_city");
		address.setState("ST");
		address.setZip("12345");
		addressService.createAddress(address);

		assertThat(addressService.getAddress(addressId).toString(), is(address.toString()));

		addressService.deleteAddress(addressId);

		assertThat(addressService.getAddress(addressId), is(nullValue()));
	}

}
