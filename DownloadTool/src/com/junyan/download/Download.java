package com.junyan.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class Download {
	
	private static String path = "http://xmp.down.sandai.net/kankan/XMPSetup_5.2.1.4768-video.exe";
	private static String store_path = "E:\\workspace";
	private static final int THREAD_NUMBER = 3;
	
	public static void main(String[] args) {
		try {
			URL url = new URL(path);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setConnectTimeout(5000);
			int code = httpURLConnection.getResponseCode();
			if (code == 200) {
				long length = httpURLConnection.getContentLengthLong();
				httpURLConnection.disconnect();
				if (length!=-1) {
					long blockSize = length/THREAD_NUMBER;
					for (int i = 0; i < THREAD_NUMBER; i++) {
						long startIndex = i * blockSize;
						long endIndex = (i + 1) * blockSize;
						if (i == THREAD_NUMBER - 1) {
							endIndex = length - 1;
						}
						DownloadThread downloadThread = new DownloadThread(startIndex, endIndex, i);
						downloadThread.start();
					}
				}else {
					System.out.println("The file is too big to download.");
					return;
				}
			}else {
				System.out.println("Fail to access.");
				return;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getName(String path) {
		int index = path.lastIndexOf("/");
		return path.substring(index);
	}
	
	private static class DownloadThread extends Thread {
		
		private long startIndex;
		private long endIndex;
		private int threadID;
		
		public DownloadThread(long startIndex, long endIndex, int id) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			threadID = id;
		}
		
		public void run() {
			URL url;
			try {
				url = new URL(path);
				HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setConnectTimeout(5000);
				httpURLConnection.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
				int code = httpURLConnection.getResponseCode();
				if (code == 206) {
					File file = new File(store_path, threadID + ".txt");
					if (file.exists()&&file.length()>0) {
						BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
						String content = bufferedReader.readLine();
						bufferedReader.close();
						long index = Long.parseLong(content);
						startIndex = index;
					}
					RandomAccessFile randomAccessFile = new RandomAccessFile(store_path + "/" + Download.getName(path), "rw");
					randomAccessFile.seek(startIndex);
					byte[] bytes = new byte[1024*1024];
					InputStream inputStream = httpURLConnection.getInputStream();
					int len = 0;
					while ((len = inputStream.read(bytes)) != -1) {
						randomAccessFile.write(bytes, 0, len);
						startIndex += len;
						RandomAccessFile randomAccessFile2 = new RandomAccessFile(store_path + "/" + threadID + ".txt", "rwd");
						randomAccessFile2.write(String.valueOf(startIndex).getBytes());
						randomAccessFile2.close();
					}
					randomAccessFile.close();
					inputStream.close();
					file.delete();
					System.out.println("Thread " + threadID + " success.");
				}else {
					System.out.println("Thread " + threadID + " request failly.");
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
