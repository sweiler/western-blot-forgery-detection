package forgery.web;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * This is a Service for generating salted password hashes. Instances can be
 * gained through instance() according to the Singleton-Pattern.
 * 
 * @author Simon Weiler <simon.weiler@stud.tu-darmstadt.de>
 */
public class PasswordHashService {
	private static PasswordHashService _instance;
	/**
	 * The length of generated salts and also the asserted length for the param
	 * "salt" of {@link #generateHash(String, String) generateHash}.
	 */
	public static final int saltLength = 10;
	private SecureRandom random = new SecureRandom();
	private MessageDigest sha = null;
	private MessageDigest md5 = null;
	private static final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private PasswordHashService() {
		try {
			sha = MessageDigest.getInstance("SHA-256");
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(
					"Hash algorithm SHA-256 is not available but required!", e);
		}

	}

	/**
	 * Creates a instance of PasswordHashService, if there isn't already one.
	 * 
	 * @return A newly or already created instance of this class
	 */
	public static PasswordHashService instance() {
		if (_instance == null)
			_instance = new PasswordHashService();
		return _instance;
	}
	
	public String generateRandomString(int length) {
		StringBuffer sb = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			int position = random.nextInt(characters.length());
			sb.append(characters.charAt(position));
		}
		return sb.toString();
	}

	/**
	 * Generates a random salt for use with
	 * {@link #generateHash(String, String) generateHash}.
	 * 
	 * @return A random sequence of alpha-numeric characters with ength
	 *         {@link #saltLength}.
	 */
	public String generateSalt() {
		return generateRandomString(saltLength);
	}
	
	/**
	 * Generates a random auth token with doubled the length of {@link #saltLength}.
	 * @return A random sequence of alpha-numeric characters with length {@code 2 * saltLength}
	 */
	public String generateAuthToken() {
		return generateRandomString(saltLength * 2);
	}

	/**
	 * Generates the SHA-256 hash of {@code salt + password}.
	 * 
	 * @param password
	 *            The password you want to hash
	 * @param salt
	 *            The salt, generated with {@link #generateSalt()}. Must have
	 *            {@link #saltLength} characters
	 * @return The computed hash as hexadecimal String
	 */
	public String generateHash(String password, String salt) {
		if (salt.length() != saltLength)
			throw new IllegalArgumentException(
					"Salt length of parameter 'salt' equals not saltLength = "
							+ saltLength);

		String value = salt + password;

		sha.reset();
		byte[] hash = sha.digest(value.getBytes());

		return bytesToHex(hash);
	}

	
	private String bytesToHex(byte[] b) {
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}
		return buf.toString();
	}
	
	/**
	 * Generates a MD5-Hash of data
	 * @param data The data to hash as byte array
	 * @return MD5-Hash of Data as Hex string
	 */
	public String hashData(byte[] data) {
		md5.reset();
		byte[] hash = md5.digest(data);
		return bytesToHex(hash);
	}

	public String generateReportId() {
		return generateRandomString(20);
	}
}
