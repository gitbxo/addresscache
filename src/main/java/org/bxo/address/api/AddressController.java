package org.bxo.address.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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

	private static final ConcurrentHashMap<String, AtomicLong> requestCount = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, AtomicLong> requestMillis = new ConcurrentHashMap<>();

	@Autowired
	private AddressService addressService;

	@GetMapping(value = "/address")
	public ResponseEntity<Object> getAddress(@RequestParam(name = "addressId", required = true) String addressId) {

		Long startMillis = System.currentTimeMillis();
		HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
		try {
			AddressInfo address = addressService.getAddress(UUID.fromString(addressId));
			if (null == address) {
				statusCode = HttpStatus.NOT_FOUND;
				return new ResponseEntity<Object>("Address not found", statusCode);
			}
			statusCode = HttpStatus.OK;
			return new ResponseEntity<Object>(StringEscapeUtils.escapeHtml4(address.toString()), statusCode);

		} finally {
			updateStats("getAddress " + String.valueOf(statusCode), System.currentTimeMillis() - startMillis);
		}
	}

	@GetMapping(value = "/search")
	public ResponseEntity<Object> search(@RequestParam(name = "query", required = true) String query,
			@RequestParam(name = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(name = "resultsPerPage", required = false) Integer resultsPerPage,
			@RequestParam(name = "maxResults", required = false) Integer maxResults,
			@RequestParam(name = "exactMatch", required = false) Boolean exactMatch,
			@RequestParam(name = "requireAll", required = false) Boolean requireAll) {

		Long startMillis = System.currentTimeMillis();
		HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
		try {
			if (null == pageNumber || pageNumber < 1) {
				pageNumber = 1;
			}
			if (null == resultsPerPage || resultsPerPage < 2) {
				resultsPerPage = 5;
			}
			if (null == maxResults) {
				maxResults = -1;
			}
			if (null == exactMatch) {
				exactMatch = false;
			}
			if (null == requireAll) {
				requireAll = false;
			}
			List<AddressInfo> addressList = addressService.search(query, pageNumber, resultsPerPage, maxResults, exactMatch, requireAll);
			if (null == addressList || addressList.size() == 0) {
				statusCode = HttpStatus.NOT_FOUND;
				return new ResponseEntity<Object>("Address not found", statusCode);
			}
			String prefix = "\"";
			StringBuilder sb = new StringBuilder();
			sb.append("[\n");
			for (AddressInfo i : addressList) {
				sb.append(prefix + StringEscapeUtils.escapeHtml4(i.toString()) + "\"");
				prefix = ",\n\"";
			}
			sb.append("\n]");
			statusCode = HttpStatus.OK;
			return new ResponseEntity<Object>(sb.toString(), statusCode);

		} finally {
			updateStats("search " + String.valueOf(statusCode), System.currentTimeMillis() - startMillis);
		}
	}

	@PostMapping(value = "/address")
	public ResponseEntity<Object> createAddress(@RequestParam(name = "line1", required = true) String line1,
			@RequestParam(name = "line2", required = false) String line2,
			@RequestParam(name = "city", required = true) String city,
			@RequestParam(name = "state", required = true) String state,
			@RequestParam(name = "zip", required = true) String zip) {

		Long startMillis = System.currentTimeMillis();
		HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
		try {
			AddressInfo address = new AddressInfo(UUID.randomUUID());
			address.setLine1(line1.trim());
			if (!StringUtils.isBlank(line2)) {
				address.setLine2(line2.trim());
			}
			address.setCity(city.trim());
			address.setState(state.trim());
			address.setZip(zip.trim());

			statusCode = HttpStatus.OK;
			return new ResponseEntity<Object>(
					StringEscapeUtils.escapeHtml4(addressService.createAddress(address).toString()), statusCode);

		} finally {
			updateStats("createAddress " + String.valueOf(statusCode), System.currentTimeMillis() - startMillis);
		}
	}

	@PutMapping(value = "/address")
	public ResponseEntity<Object> updateAddress(@RequestParam(name = "addressId", required = true) String addressId,
			@RequestParam(name = "line1", required = false) String line1,
			@RequestParam(name = "line2", required = false) String line2,
			@RequestParam(name = "city", required = false) String city,
			@RequestParam(name = "state", required = false) String state,
			@RequestParam(name = "zip", required = false) String zip) {

		Long startMillis = System.currentTimeMillis();
		HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
		try {
			AddressInfo address = addressService.getAddress(UUID.fromString(addressId));
			String previousAddress = address.getTextAddress();
			if (!StringUtils.isBlank(line1)) {
				address.setLine1(line1.trim());
			}
			if (!StringUtils.isBlank(line2)) {
				address.setLine2(line2.trim());
			}
			if (!StringUtils.isBlank(city)) {
				address.setCity(city.trim());
			}
			if (!StringUtils.isBlank(state)) {
				address.setState(state.trim());
			}
			if (!StringUtils.isBlank(zip)) {
				address.setZip(zip.trim());
			}
			if (StringUtils.isBlank(line2) && address.getTextAddress().equals(previousAddress)) {
				address.setLine2("");
			}

			statusCode = HttpStatus.OK;
			return new ResponseEntity<Object>(
					StringEscapeUtils.escapeHtml4(addressService.updateAddress(address).toString()), statusCode);

		} finally {
			updateStats("updateAddress " + String.valueOf(statusCode), System.currentTimeMillis() - startMillis);
		}
	}

	@DeleteMapping(value = "/address")
	public ResponseEntity<Object> deleteAddress(@RequestParam(name = "addressId", required = true) String addressId) {

		Long startMillis = System.currentTimeMillis();
		HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
		try {
			AddressInfo address = addressService.deleteAddress(UUID.fromString(addressId));
			if (null == address) {
				statusCode = HttpStatus.NOT_FOUND;
				return new ResponseEntity<Object>("Address not found", statusCode);
			} else if (!address.getAddressId().toString().equals(addressId)) {
				statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
				return new ResponseEntity<Object>("Internal Error", statusCode);
			}
			statusCode = HttpStatus.OK;
			return new ResponseEntity<Object>(StringEscapeUtils.escapeHtml4("Deleted addressId " + addressId),
					statusCode);

		} finally {
			updateStats("deleteAddress " + String.valueOf(statusCode), System.currentTimeMillis() - startMillis);
		}
	}

	@GetMapping(value = "/stats")
	public ResponseEntity<Object> getStats() {

		Long startMillis = System.currentTimeMillis();
		HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
		try {
			List<String> requestList = new ArrayList<>();
			requestList.addAll(requestCount.keySet());
			String prefix = "  \"";
			StringBuilder sb = new StringBuilder();
			sb.append("[\n");

			for (String requestName : requestList) {
				if (!requestMillis.containsKey(requestName)) {
					continue;
				}
				long count = requestCount.get(requestName).get();
				long avgMillis = requestMillis.get(requestName).get() / count;
				sb.append(prefix + StringEscapeUtils.escapeHtml4(requestName) + "\": {\n");
				sb.append("    \"count\": " + String.valueOf(count) + ",\n");
				sb.append("    \"averageMillis\": " + String.valueOf(avgMillis) + "\n");
				sb.append("  }");
				prefix = ",\n  \"";
			}
			sb.append("\n]");

			statusCode = HttpStatus.OK;
			return new ResponseEntity<Object>(sb.toString(), statusCode);

		} finally {
			updateStats("getStats " + String.valueOf(statusCode), System.currentTimeMillis() - startMillis);
		}
	}

	private static void updateStats(String requestName, Long requestTimeMillis) {
		if (!requestCount.containsKey(requestName)) {
			requestCount.putIfAbsent(requestName, new AtomicLong(0L));
		}
		if (!requestMillis.containsKey(requestName)) {
			requestMillis.putIfAbsent(requestName, new AtomicLong(0L));
		}
		requestCount.get(requestName).incrementAndGet();
		requestMillis.get(requestName).addAndGet(requestTimeMillis < 1 ? 1 : requestTimeMillis);
	}

}
