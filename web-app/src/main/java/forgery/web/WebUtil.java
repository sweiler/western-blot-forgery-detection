package forgery.web;

public class WebUtil {
	public static String shorten(String input) {
		if (input.length() > 20) {
			return input.substring(0, 19) + "&hellip;";
		}
		return input;
	}
}
