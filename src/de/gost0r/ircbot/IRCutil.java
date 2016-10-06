package de.gost0r.ircbot;

public class IRCutil {

	public static String getNickFromHostmark(String hostmark) {
		return hostmark.substring(1, hostmark.indexOf("!"));
	}
	
	public static String getHostFromHostmark(String hostmark) {
		return hostmark.substring(hostmark.indexOf("@"));
	}
	
	public static String getIdentFromHostmark(String hostmark) {
		return hostmark.substring(hostmark.indexOf("!"), hostmark.indexOf("@"));
	}

	public static void sleep(long i) {
		try {
			Thread.sleep(i*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
}
