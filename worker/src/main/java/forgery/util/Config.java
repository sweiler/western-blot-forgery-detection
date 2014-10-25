package forgery.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "config")
public class Config {
	@XmlElement
	public double threshold_width;

	@XmlElement
	public double threshold_height;

	@XmlElement
	public double threshold_sum;
	
	@XmlElement
	public int nn_distance;
	
	@XmlElement
	public int range_threshold;
	
	@XmlElement
	public int avg_intensity_threshold;
	
	@XmlElement
	public int block_size;
	
	@XmlElement
	public int knn;
	
	@XmlElement
	public boolean useCUDA;
	
	@XmlElement
	public String baseUrl;
	
}
