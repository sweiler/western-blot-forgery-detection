package test.forgery.util;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;

import org.junit.Test;

import forgery.util.Config;
import forgery.util.Utility;

public class ZernikeCUDATest {
	// UNCOMMENT FOR TESTING ON CUDA DEVICES
	/*
	@Test
	public void testZernikeCUDAMonotonousBlockIsNotProcessed() {
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
	    double[] results = util.getZernikeCUDAResults(testimage, config);
	    assertEquals(-1, results[12], 0.01);
	    assertEquals(-1, results[13], 0.01);
	    assertEquals(-1, results[14 + 12], 0.01);
	    assertEquals(-1, results[14 + 13], 0.01);
	}
	
	@Test
	public void testZernikeCUDASecondMomentIsCorrectForSimpleImage() {
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
	    double[] results = util.getZernikeCUDAResults(testimage, config);
	    assertEquals((1/Math.PI)*255,results[0], 0.01);
	    assertNotEquals(-1, results[12]);
	}
	*/
}
