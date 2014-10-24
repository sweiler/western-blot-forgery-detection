package forgery.web.model;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.tomcat.util.codec.binary.Base64;

import forgery.web.PasswordHashService;

/**
 * Represents a single file, uploaded by the user referenced by {@link #user}.
 * @author Simon Weiler <simon.weiler@stud.tu-darmstadt.de>
 */
public class UploadedFile {
	private int id;
	private User user;
	private UploadedFile parent;
	private FileType type;
	private FileState state;
	private Calendar created;
	private Calendar stateChange;
	private String hash;
	private String filename;
	private int failureCounter;
	private Set<UploadedFile> children = new HashSet<UploadedFile>();
	private Set<ImageRect> rects = new HashSet<ImageRect>();
	private Set<Report> reports = new HashSet<Report>();
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Calendar getCreated() {
		return created;
	}
	
	public int getId() {
		return id;
	}
	public UploadedFile getParent() {
		return parent;
	}
	public FileState getState() {
		return state;
	}
	
	public FileType getType() {
		return type;
	}
	public User getUser() {
		return user;
	}
	public void setCreated(Calendar created) {
		this.created = created;
	}

	public void setId(int id) {
		this.id = id;
	}
	public void setParent(UploadedFile parent) {
		this.parent = parent;
	}
	public void setState(FileState status) {
		this.state = status;
		this.setStateChange(new GregorianCalendar());
	}

	public void setType(FileType type) {
		this.type = type;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	public void setBinaryData(byte[] data) {
		PasswordHashService srvc = PasswordHashService.instance();
		
		this.hash = srvc.hashData(data);
	}
	
	public Calendar getStateChange() {
		return stateChange;
	}

	public void setStateChange(Calendar stateChange) {
		this.stateChange = stateChange;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Set<ImageRect> getRects() {
		return rects;
	}

	public void setRects(Set<ImageRect> rects) {
		this.rects = rects;
	}

	public int getFailureCounter() {
		return failureCounter;
	}

	public void setFailureCounter(int failureCounter) {
		this.failureCounter = failureCounter;
	}
	
	public void increaseFailureCounter() {
		failureCounter++;
	}

	public Set<Report> getReports() {
		return reports;
	}

	public void setReports(Set<Report> reports) {
		this.reports = reports;
	}

	public Set<UploadedFile> getChildren() {
		return children;
	}

	public void setChildren(Set<UploadedFile> children) {
		this.children = children;
	}
	
	
}
