package forgery.web.test;

import static org.junit.Assert.*;

import org.junit.Test;

import forgery.web.PasswordHashService;

public class PasswordHashServiceTest {

	@Test
	public void testGenerateSalt() {
		PasswordHashService uut = PasswordHashService.instance();
		
		String salt = uut.generateSalt();
		
		assertEquals(PasswordHashService.saltLength, salt.length());
		assertNotEquals(salt.substring(0, 3), salt.substring(3,6));
		assertNotEquals(salt, uut.generateSalt());
		System.out.println(salt);
	}

	@Test
	public void testGenerateHash() {
		PasswordHashService uut = PasswordHashService.instance();
		
		String salt = "bNf37fDh32";
		String password = "ThisIsATestPWD";
		
		String hash = uut.generateHash(password, salt);
		
		assertEquals(64, hash.length());
		assertEquals("3f2ec304b3a35fb4bd57ebc71140531924f5638a04b17b172c08e9ffe917f0a8", hash.toLowerCase());
	}

}
