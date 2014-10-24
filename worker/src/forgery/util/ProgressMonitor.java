package forgery.util;

public class ProgressMonitor {

	private int current, max;
	private String note;
	private String currentLine = "";
	
	public ProgressMonitor(String note, int current,
			int max) {
		this.note = note;
		this.current = current;
		this.max = max;
		execute();
	}

	public void setNote(String note) {
		this.note = note;
		execute();
	}

	public int getMaximum() {
		return max;
	}

	public void setProgress(int current) {
		this.current = current;
		execute();
	}
	
	private void execute() {
		int percent = Math.round(current * 100f / max);
		/*
		for(int i = 0; i < currentLine.length(); i++) {
			System.out.print('\b');
		}*/
		currentLine = percent + "% completed: " + note;
		System.out.println(currentLine);
	}

}
