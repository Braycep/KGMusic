package com.braycep.demo.domain;

import java.util.ArrayList;
import java.util.List;

public class Music {
	private String fileName;
	private String singerName;
	private String albumName;
	private String fileHash;
	private String file320Hash;
	private String key;
	private String key320;
	private List<DownloadInfo> downloadInfo = new ArrayList<DownloadInfo>();
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getSingerName() {
		return singerName;
	}
	public void setSingerName(String singerName) {
		this.singerName = singerName;
	}
	public String getAlbumName() {
		return albumName;
	}
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}
	public String getFileHash() {
		return fileHash;
	}
	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}
	public String getFile320Hash() {
		return file320Hash;
	}
	public void setFile320Hash(String file320Hash) {
		this.file320Hash = file320Hash;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getKey320() {
		return key320;
	}
	public void setKey320(String key320) {
		this.key320 = key320;
	}
	public List<DownloadInfo> getDownloadInfo() {
		return downloadInfo;
	}
	public void addDownloadInfo(DownloadInfo info) {
		this.downloadInfo.add(info);
	}
	
}
