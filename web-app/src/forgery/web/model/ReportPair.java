package forgery.web.model;

import java.awt.Rectangle;

public class ReportPair {

	private int id;
	private Report report;
	private Rectangle first;
	private Rectangle second;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Report getReport() {
		return report;
	}
	public void setReport(Report report) {
		this.report = report;
	}
	public Rectangle getFirst() {
		return first;
	}
	public void setFirst(Rectangle first) {
		this.first = first;
	}
	public Rectangle getSecond() {
		return second;
	}
	public void setSecond(Rectangle second) {
		this.second = second;
	}
}
