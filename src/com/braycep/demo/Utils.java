package com.braycep.demo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.braycep.demo.domain.DownloadInfo;
import com.braycep.demo.domain.Music;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Utils {
	protected static boolean stop = false;
	/**
	 * 获取hash+kgcloud的md5值
	 * @param s 传入hash+kgcloud
	 * @return	返回key
	 */
	public static String getMD5(String s) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("md5");
			byte[] b = md5.digest(s.getBytes());
			return toHex(b);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 将byte[] b转化为16进制数
	 * @param b	待转换的byte[]
	 * @return	返回16进制字符串
	 */
	public static String toHex(byte[] b){
		final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
		StringBuilder ret = new StringBuilder(b.length * 2);
	    for (int i=0; i<b.length; i++) {
	        ret.append(HEX_DIGITS[(b[i] >> 4) & 0x0f]);
	        ret.append(HEX_DIGITS[b[i] & 0x0f]);
	    }
	    return ret.toString();
	}
	
	/**
	 * 获取查询的结果，json数据
	 * @param searchValue 查询关键字
	 * @return	返回json数据
	 */
	public static StringBuilder query(String searchValue)
	{
		String url = "http://mobilecdn.kugou.com/api/v3/search/song?format=json&keyword=" + searchValue + "&page=1&pagesize=90";
		try{
			InputStream is = new URL(url).openStream();
			InputStreamReader isr = new InputStreamReader(is,"utf-8");
			//BufferedReader br = new BufferedReader(isr);
			int len = 0;
			char[] cbuff = new char[10240];
			StringBuilder sb = new StringBuilder();
			while((len = isr.read(cbuff)) > 0){
				sb.append(String.valueOf(cbuff,0,len));
			}
			isr.close();
			is.close();
			return sb;
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 从Json中解析出音乐列表
	 * @param json 传入查询结果的json
	 * @return	返回音乐列表
	 */
	public static List<Music> getMusicList(String json){
		String jsonString = json.substring(json.indexOf("\"info\":"));
		JSONArray jsonArray = JSONArray.fromObject(jsonString.substring(jsonString.indexOf("[{")));
		ArrayList<Music> list = new ArrayList<Music>();
		JSONObject jsonObject = null;
		String hash = null;
		String hash320 = null;
		for (int i = 0; i < jsonArray.size(); i++) {
			Music music = new Music();
			jsonObject = jsonArray.getJSONObject(i);
			music.setFileName(jsonObject.getString("filename"));
			music.setAlbumName(jsonObject.getString("album_name"));
			hash = jsonObject.getString("hash");
			music.setFileHash(hash);
			hash320 = jsonObject.getString("320hash");
			music.setFile320Hash(hash320);
			music.setKey(Utils.getMD5(hash+"kgcloud").toLowerCase());
			music.setKey320(Utils.getMD5(hash320+"kgcloud").toLowerCase());
			music.setSingerName(jsonObject.getString("singername"));
			list.add(music);
		}
		//获取每首音乐的下载信息
		int sum = 0;
		Vector<String> vdata;
		boolean flag = true;
		for (int i = 0; i < list.size() && !stop; i++) {
			Music music = list.get(i);
			music.addDownloadInfo(Utils.getDownloadInfo(Utils.getDownloadJson(music.getFileHash(), music.getKey()).toString()));
			music.addDownloadInfo(Utils.getDownloadInfo(Utils.getDownloadJson(music.getFile320Hash(), music.getKey320()).toString()));
			for (DownloadInfo info : music.getDownloadInfo()) {
				if (!info.getUrl().equals("null")) {
					sum++;
					vdata = new Vector<String>();
					int timeLen = Integer.parseInt(info.getTimeLength());
					int fileSize = Integer.parseInt(info.getFileSize());
					int bitRate = Integer.parseInt(info.getBitRate());
					vdata.add(music.getFileName());
					vdata.add(music.getSingerName());
					vdata.add(music.getAlbumName());
					vdata.add(info.getFileName());
					vdata.add(timeLen / 60 + ":" + timeLen % 60 + " S");
					vdata.add(new DecimalFormat("#.00").format(fileSize / 1024.0 / 1024) + "Mb");
					vdata.add(bitRate / 1000 + "Kbs");
					vdata.add(info.getUrl());
					MainFrame.model.addRow(vdata);
					MainFrame.table.setModel(MainFrame.model);
					if (flag) {
						flag = false;
						updateUI(false);
					}
				}
			}
			MainFrame.resulInfo.setText("共有" + sum + "个结果");
		}
		updateUI(true);
		return list;
	}
	
	public static void updateUI(boolean stopUpdate){
		new Thread(()->{
			while(!stopUpdate && stop){
				MainFrame.table.updateUI();
				try {
					Thread.sleep(17);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	/**
	 * 获取音乐的下载链接
	 * @param hash 传入音乐的hash值
	 * @param key 传入音乐的key(md5(hash+kgcloud))
	 * @return 返回下载信息json
	 */
	public static StringBuilder getDownloadJson(String hash, String key) {
		String url = "http://trackercdn.kugou.com/i/?cmd=4&hash="+hash+"&key="+key+"&pid=1&forceDown=0&vip=1";
		try{
			InputStream is = new URL(url).openStream();
			InputStreamReader isr = new InputStreamReader(is,"utf-8");
			int len = 0;
			char[] cbuff = new char[10240];
			StringBuilder sb = new StringBuilder();
			while((len = isr.read(cbuff)) > 0){
				sb.append(String.valueOf(cbuff,0,len));
			}
			isr.close();
			is.close();
			return sb;
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 解析下载json
	 * @param downloadJson 传入获取到的下载信息Json数据
	 * @return 返回下载信息Bean
	 */
	public static DownloadInfo getDownloadInfo(String downloadJson){
		String json = downloadJson.replace("{", "[{");
		json = json.replace("}","}]");
		JSONArray jsonArray = JSONArray.fromObject(json);
		DownloadInfo info = new DownloadInfo();
		JSONObject jsonObject = null;
		for (int i = 0; i < jsonArray.size(); i++) {
			jsonObject = jsonArray.getJSONObject(i);
			//file size
			if(jsonObject.containsKey("fileSize")){
				info.setFileSize(jsonObject.getString("fileSize"));
			}else {
				info.setFileSize("0");
			}
			//file name
			if (jsonObject.containsKey("fileName")) {
				info.setFileName(jsonObject.getString("fileName"));
			}else {
				info.setFileName("Null");
			}
			//extends name
			if (jsonObject.containsKey("extName")) {
				info.setExtName(jsonObject.getString("extName"));
			}else {
				info.setExtName("null");
			}
			//download link
			if (jsonObject.containsKey("url")) {
				info.setUrl(jsonObject.getString("url").replace("\\/", "/"));
			}else {
				info.setUrl("null");
			}
			//bitRate
			if (jsonObject.containsKey("bitRate")) {
				info.setBitRate(jsonObject.getString("bitRate"));
			}else {
				info.setBitRate("0");
			}
			//time length
			if (jsonObject.containsKey("timeLength")) {
				info.setTimeLength(jsonObject.getString("timeLength"));
			}else {
				info.setTimeLength("0");
			}
		}
		return info;
	}
}
