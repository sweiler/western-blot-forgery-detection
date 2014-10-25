package forgery.util;

import java.awt.Rectangle;
import java.io.Serializable;

public class MatchPair implements Serializable {

	private static final long serialVersionUID = 1L;
	private Rectangle first;
	private Rectangle second;
	private float angle;
	private double length;

	public MatchPair(Rectangle first, Rectangle second) {
		this.first = first;
		this.second = second;
		int firstCenterX = first.x + first.width / 2;
		int firstCenterY = first.y + first.height / 2;
		int secondCenterX = second.x + second.width / 2;
		int secondCenterY = second.y + second.height / 2;

		int diffX = firstCenterX - secondCenterX;
		int diffY = firstCenterY - secondCenterY;

		length = Math.sqrt(diffX * diffX + diffY * diffY);
		angle = (float) (((Math.atan2(diffX, diffY) + Math.PI) * 180 / Math.PI) % 180);
	}

	public Rectangle getFirst() {
		return first;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MatchPair) {
			MatchPair other = (MatchPair) obj;
			return (first.equals(other.first) && second.equals(other.second))
					|| (first.equals(other.second) && second
							.equals(other.first));
		}
		return false;
	}

	public Rectangle getSecond() {
		return second;
	}

	public float getAngle() {
		return angle;
	}

	public double getLength() {
		return length;
	}
	
	public int getMaxX() {
		return Math.max(first.x, second.x);
	}
	
	public int getMaxY() {
		return Math.max(first.y, second.y);
	}
	
	public int getMinX() {
		return Math.min(first.x, second.x);
	}
	
	public int getMinY() {
		return Math.min(first.y, second.y);
	}

	public double dist(MatchPair other) {
		double angleDist = Math.abs(angle - other.angle);
		double lengthDist = Math.abs(length - other.length);
		double xDistMax = Math.abs(Math.max(first.x, second.x)
				- Math.max(other.first.x, other.second.x));
		double yDistMax = Math.abs(Math.max(first.y, second.y)
				- Math.max(other.first.y, other.second.y));
		double xDistMin = Math.abs(Math.min(first.x, second.x)
				- Math.min(other.first.x, other.second.x));
		double yDistMin = Math.abs(Math.min(first.y, second.y)
				- Math.min(other.first.y, other.second.y));
		return angleDist * angleDist * angleDist + lengthDist * lengthDist
				* lengthDist + xDistMax + yDistMax + xDistMin + yDistMin;
	}

}
