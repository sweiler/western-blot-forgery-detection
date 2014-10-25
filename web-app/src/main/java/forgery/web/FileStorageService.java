package forgery.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class FileStorageService {
	private static FileStorageService _instance;
	private static final String path = "/opt/forgery/uploadedFiles";
	
	private FileStorageService() {
		
	}
	
	public Process convertProcessPDFtoPNG(String hash) throws IOException {
		String foldername = hash.substring(0, 2);
		File folder = new File(path + "/thumbs/" + foldername);
		if(!folder.exists()) {
			folder.mkdirs();
		}
		ProcessBuilder pdfToPng = new ProcessBuilder("convert",
				"-density",
				"100",
				filePathForHash(hash, "complete") + "[0]",
				filePathForHash(hash, "thumbs") + ".png");
		
		
		return pdfToPng.start();
	}
	
	public Process extractImagesProcess(String hash) throws IOException {
		File folder = new File(path + "/extracted/" + hash);
		if(!folder.exists()) {
			folder.mkdirs();
		}
		ProcessBuilder procBuilder = new ProcessBuilder("pdfimages",
				filePathForHash(hash, "complete"),
				path + "/extracted/" + hash + "/extracted");
		
		return procBuilder.start();
	}
	
	public Process convertExtracted(String hash) throws IOException {
		ProcessBuilder procBuilder = new ProcessBuilder("convert",
				path + "/extracted/" + hash + "/*.ppm",
				path + "/extracted/" + hash + "/extracted.png");
		
		return procBuilder.start();
	}
	
	public String filePathExtracted(String hash) {
		return path + "/extracted/" + hash + "/";
	}
	
	public static FileStorageService instance() {
		if(_instance == null)
			_instance = new FileStorageService();
		
		return _instance;
	}
	
	public byte[] loadData(String hash) throws IOException {
		return loadData(hash, "complete");
	}
	
	public byte[] loadThumb(String hash) throws IOException {
		return loadData(hash, "thumbs");
	}
	
	private String filePathForHash(String hash, String pathprefix) {
		String foldername = hash.substring(0, 2);
		String filename = hash.substring(2);
		if(pathprefix.equals("thumbs_png")) {
			return path + "/thumbs/" + foldername + "/" + filename + ".png";
		}
		return path + "/" + pathprefix + "/" + foldername + "/" + filename;
	}
	
	private byte[] loadData(String hash, String pathprefix) throws IOException {
		File file = new File(filePathForHash(hash, pathprefix));
		if(file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				IOUtils.copy(fis, bos);
				return bos.toByteArray();
			} catch (FileNotFoundException e) {
				throw new IllegalStateException("This should never happen", e);
			}
			
		} else {
			return null;
		}
	}
	
	public String storeData(byte[] data) throws IOException {
		PasswordHashService srv = PasswordHashService.instance();
		String hash = srv.hashData(data);
		storeData(hash, data);
		return hash;
	}
	
	private void storeData(String hash, byte[] data, String pathprefix) throws IOException {
		String foldername = hash.substring(0, 2);
		File folder = new File(path + "/" + pathprefix + "/" + foldername);
		File file = new File(filePathForHash(hash, pathprefix));
		if(!folder.exists()) {
			folder.mkdirs();
		}
		if(!file.exists()) {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data);
			fos.close();
		}
	}
	
	public void storeData(String hash, byte[] data) throws IOException {
		storeData(hash, data, "complete");
	}
	
	public void storeThumb(String hash, byte[] data) throws IOException {
		storeData(hash, data, "thumbs");
	}

	public byte[] loadGeneratedPNG(String hash) throws IOException {
		return loadData(hash, "thumbs_png");
	}

	

	
}
