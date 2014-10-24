package forgery.web.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import forgery.web.FileStorageService;
import forgery.web.PasswordHashService;

public class FileStorageServiceTest {
	
	private static FileStorageService uut;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		uut = FileStorageService.instance();
	}

	@Test
	public void test() throws IOException {
		byte[] randomData = {0x3F, 0x28, 0x47, 0x2C};
		String hash = PasswordHashService.instance().hashData(randomData);
		
		uut.storeData(hash, randomData);
		
		byte[] readData = uut.loadData(hash);
		
		assertArrayEquals(randomData, readData);
		
	}

}
