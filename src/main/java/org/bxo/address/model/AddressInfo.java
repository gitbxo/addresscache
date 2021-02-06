package org.bxo.address.model;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

public class AddressInfo {

	private static final String ERR_MSG_ADDRESS_ID = "AddressId is required";
	private static final String ERR_MSG_LINE1 = "line1 is required";
	private static final String ERR_MSG_CITY = "city is required";
	private static final String ERR_MSG_STATE = "state must be 2 characters";
	private static final String ERR_MSG_ZIP = "zip must be 5 digits";

	private UUID addressId;
	private String line1;
	private String line2;
	private String city;
	private String state;
	private String zip;

	public AddressInfo(UUID addressId) {
		this.addressId = validateAddressId(addressId);
		this.line1 = null;
		this.line2 = null;
		this.city = null;
		this.state = null;
		this.zip = null;
	}

	public AddressInfo(AddressInfo source) {
		this.addressId = validateAddressId(source.getAddressId());
		this.line1 = source.getLine1();
		this.line2 = source.getLine2();
		this.city = source.getCity();
		this.state = source.getState();
		this.zip = source.getZip();
	}

	public UUID getAddressId() {
		return validateAddressId(addressId);
	}

	public String getLine1() {
		return validateLine1(line1);
	}

	public String getLine2() {
		return validateLine2(line2);
	}

	public String getCity() {
		return validateCity(city);
	}

	public String getState() {
		return validateState(state);
	}

	public String getZip() {
		return validateZip(zip);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(null == addressId ? "<TBD>: " : (addressId.toString() + ": "));
		sb.append(StringUtils.isBlank(line1) ? "" : (line1 + ","));
		sb.append(StringUtils.isBlank(line2) ? "" : (line2 + ","));
		sb.append(StringUtils.isBlank(city) ? "" : (city + ","));
		sb.append(StringUtils.isBlank(state) ? "" : (state));
		sb.append(StringUtils.isBlank(zip) ? "" : (" " + zip));
		return sb.toString();
	}

	public void setLine1(String line1) {
		this.line1 = validateLine1(line1);
	}

	public void setLine2(String line2) {
		this.line2 = validateLine2(line2);
	}

	public void setCity(String city) {
		this.city = validateCity(city);
	}

	public void setState(String state) {
		this.state = validateState(state);
	}

	public void setZip(String zip) {
		this.zip = validateZip(zip);
	}

	private static UUID validateAddressId(UUID addressId) {
		if (null == addressId) {
			throw new IllegalArgumentException(ERR_MSG_ADDRESS_ID);
		}
		return addressId;
	}

	private static String validateLine1(String line1) {
		if (StringUtils.isBlank(line1)) {
			throw new IllegalArgumentException(ERR_MSG_LINE1);
		}
		return line1.trim();
	}

	private static String validateLine2(String line2) {
		return (StringUtils.isBlank(line2) ? "" : line2.trim());
	}

	private static String validateCity(String city) {
		if (StringUtils.isBlank(city)) {
			throw new IllegalArgumentException(ERR_MSG_CITY);
		}
		return city.trim();
	}

	private static String validateState(String state) {
		if (StringUtils.isBlank(state)) {
			throw new IllegalArgumentException(ERR_MSG_STATE);
		}
		if (state.trim().length() != 2) {
			throw new IllegalArgumentException(ERR_MSG_STATE);
		}
		return state.trim();
	}

	private static String validateZip(String zip) {
		if (StringUtils.isBlank(zip)) {
			throw new IllegalArgumentException(ERR_MSG_ZIP);
		}
		if (zip.trim().length() != 5) {
			throw new IllegalArgumentException(ERR_MSG_ZIP);
		}
		try {
			Integer zipInt = Integer.parseInt(zip);
			if (zipInt > 0) {
				return zip.trim();
			} else {
				throw new IllegalArgumentException(ERR_MSG_ZIP);
			}
		} catch (NumberFormatException nbfe) {
			throw new IllegalArgumentException(ERR_MSG_ZIP);
		}
	}

}
