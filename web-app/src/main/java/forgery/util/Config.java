package forgery.util;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "config")
public class Config {
	@XmlElement
	public String mailServer = "";
	
	@XmlElement
	public String mailUser = "";
	
	@XmlElement
	public String mailPassword = "";
	
	@XmlElement
	public String fromMail = "";

	@XmlElement
	public double threshold_width;

	@XmlElement
	public double threshold_height;
	
	@XmlElement
	public double threshold_sum;
	
}
