package forgery.web.model;

/**
 * Enumeration of all statuses a uploaded file can have.
 * 
 * @author Simon Weiler <simon.weiler@stud.tu-darmstadt.de>
 * 
 */
public enum FileState {
	/**
	 * File is newly created and nothing is happend since creation.
	 */
	NEWLY_CREATED,

	/**
	 * Only used for ZIP archives. The file has been extracted and new
	 * UploadedFile objects are created. Corresponds to {@link #FINISHED} for
	 * non-zipped files.
	 */
	EXTRACTED,
	
	/**
	 * The user has started the analyze process.
	 */
	START_ANALYZE,

	/**
	 * File is in process on a worker machine.
	 */
	PROCESSING,

	/**
	 * Processing is finished and a report is available.
	 */
	FINISHED,
	
	/**
	 * An unknown error is occurred. See log files for more information.
	 */
	UNKNOWN_ERROR
}
