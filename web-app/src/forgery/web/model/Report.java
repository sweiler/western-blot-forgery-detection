package forgery.web.model;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import forgery.web.PasswordHashService;

public class Report implements Comparable<Report> {
	private String id;
	private UploadedFile file;
	private int version;
	private String options;
	private Report linked;
	private boolean published;
	private Calendar created;
	private Set<ReportPair> pairs = new HashSet<ReportPair>();

	

	public Report() {
		
	}
	
	public Report(UploadedFile file, int version,
			String options) {
		this.file = file;
		this.version = version;
		this.options = options;
	}
	
	public Report(String id, UploadedFile file, int version,
			String options) {
		this.id = id;
		this.file = file;
		this.version = version;
		this.options = options;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public UploadedFile getFile() {
		return file;
	}
	public void setFile(UploadedFile file) {
		this.file = file;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getOptions() {
		return options;
	}
	public void setOptions(String options) {
		this.options = options;
	}
	
	public Report getLinked() {
		return linked;
	}

	public void setLinked(Report linked) {
		this.linked = linked;
	}
	
	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public Set<ReportPair> getPairs() {
		return pairs;
	}

	public void setPairs(Set<ReportPair> pairs) {
		this.pairs = pairs;
	}
	
	public String generateId() {
		PasswordHashService hs = PasswordHashService.instance();
		this.id = hs.generateReportId();
		return this.id;
	}

	public Calendar getCreated() {
		return created;
	}

	public void setCreated(Calendar created) {
		this.created = created;
	}

	@Override
	public int compareTo(Report o) {
		return -created.compareTo(o.created);
	}
	
}
