package org.bxo.address.api;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bxo.address.model.AddressInfo;
import org.bxo.address.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AddressController {

	@Autowired
	private AddressService addressService;

	@GetMapping(value = "/address")
	public ResponseEntity<Object> getAddress(@RequestParam(name = "addressId", required = true) String addressId) {
		// try {
		AddressInfo address = addressService.getAddress(UUID.fromString(addressId));
		if (null == address) {
			return new ResponseEntity<Object>("Address not found", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Object>(StringEscapeUtils.escapeHtml4(address.toString()), HttpStatus.OK);
		// } catch (Throwable t) {
		// return new ResponseEntity<String>("Address not found", HttpStatus.NOT_FOUND);
		// }
	}

	@GetMapping(value = "/search")
	public ResponseEntity<Object> search(@RequestParam(name = "query", required = true) String query) {
		// try {
		List<AddressInfo> addressList = addressService.search(query, 10);
		if (null == addressList || addressList.size() == 0) {
			return new ResponseEntity<Object>("Address not found", HttpStatus.NOT_FOUND);
		}
		String prefix = "\"";
		StringBuilder sb = new StringBuilder();
		sb.append("[\n");
		for (AddressInfo i : addressList) {
			sb.append(prefix + StringEscapeUtils.escapeHtml4(i.toString()) + "\"");
			prefix = ",\n\"";
		}
		sb.append("\n]");
		return new ResponseEntity<Object>(sb.toString(), HttpStatus.OK);
		// } catch (Throwable t) {
		// return new ResponseEntity<String>("Address not found", HttpStatus.NOT_FOUND);
		// }
	}

	@PostMapping(value = "/address")
	public ResponseEntity<Object> createAddress(@RequestParam(name = "line1", required = true) String line1,
			@RequestParam(name = "line2", required = false) String line2,
			@RequestParam(name = "city", required = true) String city,
			@RequestParam(name = "state", required = true) String state,
			@RequestParam(name = "zip", required = true) String zip) {

		// try {
		AddressInfo address = new AddressInfo(UUID.randomUUID());
		address.setLine1(line1);
		if (!StringUtils.isBlank(line2)) {
			address.setLine2(line2);
		}
		address.setCity(city);
		address.setState(state);
		address.setZip(zip);

		return new ResponseEntity<Object>(
				StringEscapeUtils.escapeHtml4(addressService.createAddress(address).toString()), HttpStatus.OK);
		// } catch (Throwable t) {
		// return invalidParam();
		// }
	}

	@PutMapping(value = "/address")
	public ResponseEntity<Object> updateAddress(@RequestParam(name = "addressId", required = true) String addressId,
			@RequestParam(name = "line1", required = false) String line1,
			@RequestParam(name = "line2", required = false) String line2,
			@RequestParam(name = "city", required = false) String city,
			@RequestParam(name = "state", required = false) String state,
			@RequestParam(name = "zip", required = false) String zip) {
		// try {
		AddressInfo address = addressService.getAddress(UUID.fromString(addressId));
		if (!StringUtils.isBlank(line1)) {
			address.setLine1(line1);
		}
		if (!StringUtils.isBlank(line2)) {
			address.setLine2(line2);
		}
		if (!StringUtils.isBlank(city)) {
			address.setCity(city);
		}
		if (!StringUtils.isBlank(state)) {
			address.setState(state);
		}
		if (!StringUtils.isBlank(zip)) {
			address.setZip(zip);
		}

		return new ResponseEntity<Object>(
				StringEscapeUtils.escapeHtml4(addressService.updateAddress(address).toString()), HttpStatus.OK);
		// } catch (Throwable e) {
		// return invalidParam();
		// }
	}

	@DeleteMapping(value = "/address")
	public ResponseEntity<Object> deleteAddress(@RequestParam(name = "addressId", required = true) String addressId) {
		addressService.deleteAddress(UUID.fromString(addressId));
		return new ResponseEntity<Object>(StringEscapeUtils.escapeHtml4("Deleted addressId " + addressId),
				HttpStatus.OK);
	}

}
