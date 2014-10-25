package test.forgery.util;

import static org.junit.Assert.*;

import java.awt.Rectangle;

import org.junit.Test;

import forgery.util.Utility;

public class RemoveAllUnvalidsTest {
	
	private Utility util = Utility.instance();
	
	@Test
	public void testAllUnvalids() {
		
		double[] testarray = new double[14 * 3];
		
		for(int i = 0; i < 3; i++) {
			for(int x = 0; x < 12; x++) {
				testarray[i * 14 + x] = i * 14 + x;
			}
			if(i == 1)
				testarray[i * 14 + 12] = -1;
		}
		
		int offset = util.removeAllUnvalids(testarray, new Rectangle(300, 200, 3, 2));
		
		assertEquals(-14, offset);
		assertEquals(28, testarray[14], 0.1);
	}
	
	@Test
	public void testAppendToResults() {
		double[] testarray = new double[14 * 3];
		for(int i = 0; i < 3; i++) {
			for(int x = 0; x < 12; x++) {
				testarray[i * 14 + x] = i * 14 + x;
			}
			if(i == 1)
				testarray[i * 14 + 12] = -1;
		}
		
		int offset = util.removeAllUnvalids(testarray, new Rectangle(300, 200, 3, 2));
		
		double[] results = new double[14];
		
		for(int i = 0; i < 14; i++)
			results[i] = i + 0.5;
		
		results = util.appendToResults(results, testarray, offset);
		
		assertEquals(3.5, results[3], 0.1);
		assertEquals(14 * 3, results.length);
		assertEquals(14 * 2 + 11, results[14 * 2 + 11], 0.1);
	}
	
	@Test
	public void testExtractFeatures() {
		double[] testarray = new double[14 * 3];
		for(int i = 0; i < 3; i++) {
			for(int x = 0; x < 12; x++) {
				testarray[i * 14 + x] = i * 14 + x;
			}

			testarray[i * 14 + 12] = -i;
			testarray[i * 14 + 13] = -i * 200;
		}
		
		double[] results = util.extractFeatures(testarray);
		
		assertEquals(12 * 3, results.length);
		assertEquals(0, results[0], 0.1);
		assertEquals(14, results[12], 0.1);
	}
	
}
