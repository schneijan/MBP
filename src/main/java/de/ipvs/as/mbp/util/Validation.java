package de.ipvs.as.mbp.util;

import org.apache.commons.validator.routines.InetAddressValidator;

import javax.measure.unit.Unit;

/**
 * This class provides methods for validating input data.
 */
public class Validation {
    //Regular expression for valid formatted MAC addresses (including separators)
    private static final String REGEX_MAC_ADDRESS = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
    //Regular expression for valid unformatted MAC addresses (without separators)
    private static final String REGEX_UNFORMATTED_MAC_ADDRESS = "^[0-9A-Fa-f]{12}$";
    //Regular expression for valid private RSA key strings
    private static final String REGEX_PRIVATE_RSA_KEY = "^-----BEGIN RSA PRIVATE KEY-----(?:[\\n\\r]{1,2}.{64})+[\\n\\r]{1,2}.{1,64}[\\n\\r]{1,2}-----END RSA PRIVATE KEY-----$";

    /**
     * Returns whether a given string is null or empty.
     *
     * @param value The string to check
     * @return True, if the provided string is null or empty; false otherwise
     */
    public static boolean isNullOrEmpty(String value) {
        return (value == null) || value.trim().isEmpty();
    }

    /**
     * Checks if a provided formatted MAC address (including separators) is valid.
     *
     * @param macAddress The MAC address to check
     * @return True, if the MAC address is valid; false otherwise
     */
    public static boolean isValidMACAddress(String macAddress) {
        if (macAddress == null) {
            return false;
        }
        //Test with regular expression
        return macAddress.matches(REGEX_MAC_ADDRESS);
    }

    /**
     * Checks if a provided unformatted MAC address (without separators) is valid.
     *
     * @param macAddress The MAC address to check
     * @return True, if the MAC address is valid; false otherwise
     */
    public static boolean isValidUnformattedMACAddress(String macAddress) {
        if (macAddress == null) {
            return false;
        }
        //Test with regular expression
        return macAddress.matches(REGEX_UNFORMATTED_MAC_ADDRESS);
    }

    /**
     * Checks if a provided IP address is valid, either for the IPV4 format or the IPV6 format.
     *
     * @param ipAddress The IP address to check
     * @return True, if the IP address is valid; false otherwise
     */
    public static boolean isValidIPAddress(String ipAddress) {
        if (ipAddress == null) {
            return false;
        }

        //Create new apache validator
        InetAddressValidator validator = new InetAddressValidator();

        //Test for both IPV4 and IPV6
        return (validator.isValidInet4Address(ipAddress)) || (validator.isValidInet6Address(ipAddress));
    }

    /**
     * Checks if a provided private RSA key string is of a valid format.
     *
     * @param rsaKey The key string to check
     * @return True, if the private RSA key is of a valid format; false otherwise
     */
    public static boolean isValidPrivateRSAKey(String rsaKey) {
        if (rsaKey == null) {
            return false;
        }

        //Test with regular expression
        return rsaKey.matches(REGEX_PRIVATE_RSA_KEY);
    }

    /**
     * Checks if a given string specifies a unit in a valid way.
     *
     * @param unitString The string to check
     * @return True, if the string is a valid unit; false otherwise
     */
    public static boolean isValidUnit(String unitString) {
        try {
            //Check if string can be parsed to unit
            Unit.valueOf(unitString);
        } catch (Exception e) {
            //Exception thrown, string is not valid
            return false;
        }
        return true;
    }
}
