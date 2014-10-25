package test.forgery.util;

import static org.junit.Assert.*;

import java.awt.Rectangle;

import org.junit.Test;

import forgery.util.MatchPair;

public class MatchPairTest {

	@Test
	public void testLength() {
		Rectangle first = new Rectangle(30, 40, 16, 16);
		Rectangle second = new Rectangle(30, 60, 16, 16);
		
		MatchPair uut = new MatchPair(first, second);
		
		assertEquals(20.0, uut.getLength(), 0.01);
	}
	
	@Test
	public void testAngle() {
		Rectangle first = new Rectangle(30, 40, 16, 16);
		Rectangle second = new Rectangle(30, 60, 16, 16);
		
		MatchPair uut = new MatchPair(first, second);
		
		assertEquals(0.0, uut.getAngle(), 0.01);
		
		Rectangle third = new Rectangle(50, 40, 16, 16);
		
		MatchPair uut2 = new MatchPair(first, third);
		assertEquals(90.0, uut2.getAngle(), 0.01);
		
		MatchPair uut3 = new MatchPair(third, first);
		assertEquals(90.0, uut3.getAngle(), 0.01);
	}

}
