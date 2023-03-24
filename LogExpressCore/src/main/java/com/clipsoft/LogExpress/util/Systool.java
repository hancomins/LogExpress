package com.clipsoft.LogExpress.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Systool {
	
	private static long PID = -1; 
	private static String Hostname = null;
	
	public static long pid() {
		if(PID > 0) return PID;
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		String jvmName = runtimeBean.getName();
		long pid = Long.parseLong(jvmName.split("@")[0]);
		PID = pid;
		return pid;
	}
	
	
	public static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName.contains("win");
	}
	
	
	public static String hostname() {
		if(Hostname != null) return Hostname;
		

		try {
			String cmd = isWindows() ? "hostname.exe" : "hostname";
			Process proc = Runtime.getRuntime().exec(cmd);
			InputStream errorStream = proc.getErrorStream();
			InputStream stream = proc.getInputStream();
			OutputStream outStream = proc.getOutputStream();
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int cnt = 0;
			while((cnt = stream.read(buffer)) > 0) {
				baos.write(buffer, 0, cnt);
			}
			try {
				stream.close();
				outStream.close();
				errorStream.close();
			} catch(Exception ignored) {}
			
			String hostName = baos.toString();
			hostName = hostName.trim();
			hostName = hostName.replace("\\", "").replace("/", "").replace(":", "").replace("*", "").replace("\"", "").replace("?", "").replace("<", "")
			.replace(">", "").replace("|", "").replace("'", "").replace("$", "").replace("\f", "").replace("\b", "").replace("\n", "").replace("\r", "")
			.replace("\t", " ");
			if(hostName.isEmpty()) {
				hostName = getHostnameFromInetAddress();
			}
			Hostname = hostName;
			return hostName;
		} catch (Exception e) {
			Hostname = getHostnameFromInetAddress();
			return Hostname;
		}
	}
	
	private static String getHostnameFromInetAddress() {
		 try {
			 return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
			return "unkownhost";
		}
	}
	
	
	
}
