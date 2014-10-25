package test.forgery.util;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;

import org.junit.Test;

import forgery.util.Config;
import forgery.util.DoubleComplex;
import forgery.util.Utility;
import forgery.util.ZernikeCPU;

public class ZernikeCPUTest extends ZernikeCPU {

	public ZernikeCPUTest() {
		super(0,0,0,null,0,0,null,0,0);
	}


	@Test
	public void testGetZernikeRadialPolynomial() {
		assertEquals(0, ZernikeCPU.getZernikeRadialPolynomial(1, 0, 1), 0.01);
		assertEquals(0, ZernikeCPU.getZernikeRadialPolynomial(0, 1, 1), 0.01);
		assertEquals(0, ZernikeCPU.getZernikeRadialPolynomial(2, 3, 1), 0.01);
		assertEquals(1, ZernikeCPU.getZernikeRadialPolynomial(0, 0, 1), 0.01);
		assertEquals(1, ZernikeCPU.getZernikeRadialPolynomial(0, 0, 2), 0.01);
		assertEquals(1, ZernikeCPU.getZernikeRadialPolynomial(5, 1, 1), 0.01);
		assertEquals(32, ZernikeCPU.getZernikeRadialPolynomial(5, 5, 2), 0.01);
	}
	
	private void _testMoment(int n, int m, double rho, double theta, double expectedReal, double expectedImg) {
		DoubleComplex res = getZernikeMoment(n,m,rho,theta);
		assertEquals(expectedReal, res.real, 0.01);
		assertEquals(expectedImg, res.imaginary, 0.01);
	}
	
	@Test
	public void testGetZernikeMoment() {
		// First test: For odd n,m the moment remains zero
		_testMoment(1,2,0.0,0.0,0.0,0.0);
		_testMoment(2,1,0.0,0.0,0.0,0.0);
		_testMoment(3,2,0.0,0.0,0.0,0.0);
		_testMoment(4,1,0.0,0.0,0.0,0.0);
		// for n=m=0 is Rnm(rho) = 1 and e^0 = 1 therefore 1
		_testMoment(0,0,1.0,Math.PI,1.0,0.0);
	}

	@Test
	public void testgetAverageIntensity() {
		
		int x=0; int block_size = 16;
		int[] testArray = new int[block_size*block_size];
		for(; x < (block_size*block_size); x++) {
			testArray[x] = 0;
		}
		assertEquals(0, getAverageIntensity(block_size, testArray, 0, block_size), 0.01);
		testArray[50] = (byte) 200;
		assertEquals(0.78125, getAverageIntensity(block_size, testArray, 0, block_size), 0.01);
		testArray[51] = (byte) 200;
		assertEquals(1.5625, getAverageIntensity(block_size, testArray, 0, block_size), 0.01);
		x = 0;
		for(; x < (block_size*block_size); x++) {
			testArray[x] = (byte) 255;
		}
		assertEquals(255.0, getAverageIntensity(block_size, testArray, 0, block_size), 0.01);
	}

	@Test
	public void testZernikeCPUMonotonousBlockIsNotProcessed() {
	    Config config = new Config();
	    config.range_threshold = 1;
	    config.avg_intensity_threshold = 0;
	    config.block_size = 16;
	    
	    BufferedImage testimage = new BufferedImage(17, 16, BufferedImage.TYPE_BYTE_GRAY);
	    for(int x = 0; x < 17; x++) {
	    	for(int y = 0; y < 16; y++)
	    		testimage.getRaster().setSample(x, y, 0, 255);
	    }
	    Utility util = Utility.instance();
	    double[] results = util.getZernikeCPUResults(testimage, config);
	    assertEquals(-1, results[12], 0.01);
	    assertEquals(-1, results[13], 0.01);
	    assertEquals(-1, results[14 + 12], 0.01);
	    assertEquals(-1, results[14 + 13], 0.01);
	}
	
	@Test
	public void testZernikeCPUSecondMomentIsCorrectForSimpleImage() {
		Config config = new Config();
	    config.range_threshold = 1;
	    config.avg_intensity_threshold = 0;
	    config.block_size = 16;
	    BufferedImage testimage = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_GRAY);
	    for(int x = 0; x < 16; x++) {
	    	for(int y = 0; y < 16; y++) {
	    		if(x == 9 && y == 9)
	    			testimage.getRaster().setSample(x,y,0,255);
	    		else
	    			testimage.getRaster().setSample(x, y, 0, 0);
	    	}
	    }
	    Utility util = Utility.instance();
	    double[] results = util.getZernikeCPUResults(testimage, config);
	    assertEquals((1/Math.PI)*255,results[0], 0.01);
	    assertNotEquals(-1, results[12]);
	}

}
