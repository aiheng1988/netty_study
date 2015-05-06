package com.ailinxi.netty.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间服务器
 * 功能：
 * 	通过 telnet 工具 连上8888端口
 * 	发送指令
 * 	 	date   ----->     2015-05-06 22:10:23
 * @author ahern88
 * @date 2015年5月6日
 * @version 1.0
 */
public class BioTimeServer {
	
	private final static int port = 8888;
	
	private final static SimpleDateFormat SDF_NORMAL;
	
	static {
		SDF_NORMAL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * @see java.net.Socket
	 * @see java.net.ServerSocket
	 * @param args
	 */
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			while(true) {
				System.out.println("server is ok! waiting for clients");
				Socket socket = serverSocket.accept();
				System.out.println("duang, one client come in");
				// syncHandleSocket(socket);
				asyncHandleSocket(socket);
			}
		} catch(IOException e) {
			System.err.println("connect to port 8888 error:" + e.getMessage());
		} catch(Exception e) {
			System.err.println("error:" + e.getMessage());
		}
	}
	
	/**
	 * 异步操作
	 * @param socket
	 */
	private static void asyncHandleSocket(Socket socket) {
		new TimeThread(socket).start();
		System.out.println("handle one client request");
	}
	
	/**
	 * 同步操作
	 * @param socket
	 */
	private static void syncHandleSocket(Socket socket) {
		boolean start = true;
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			while(start) {
				String command = br.readLine().trim();
				if("date".equalsIgnoreCase(command)) {
					String now = SDF_NORMAL.format(new Date());
					System.out.println("now:" + now);
					bw.write(now + "\r\n");
					bw.flush();
				} else if("bye".equalsIgnoreCase(command)) {
					bw.write("bye bye\r\n");
					bw.flush();
					start = false;
				} else {
					bw.write("=============================\r\n");
					bw.write("please using { date | bye }\r\n");
					bw.write("date : to show current date\r\n");
					bw.write("bye : quit current connect\r\n");
					bw.write("=============================\r\n");
					bw.flush();	
				}
			}
		} catch(Exception e) {
			System.err.println("server error:" + e.getMessage());
		} finally {
			try {
				if(br != null){
					br.close();
				}
			} catch(Exception ignore) {
			}
			try {
				if(bw != null){
					bw.close();
				}
			} catch(Exception ignore) {
			}
			try {
				socket.close();
			} catch (IOException ignore) {
			}
		}
	}

	private static class TimeThread extends Thread {
		
		private Socket socket;
		
		public TimeThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			syncHandleSocket(socket);
		}
		
	}

}
