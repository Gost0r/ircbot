package de.gost0r.ircbot;

public class Debug {
	
	private static boolean debug = false;
	
	public enum TAG {
		INC,
		OUT,
		LOGIC,
		MAIN,
		PING,
		MSGHDL
	}

	public static void Log(TAG tag, String string) {
		if (tag == TAG.PING) {
			return;
		}
		if (tag == TAG.OUT) {
			System.out.print(Thread.currentThread().getName() + " - " + tag.name() + ": " + string);
			return;
		} else {
			System.out.println(Thread.currentThread().getName() + " - " + tag.name() + ": " + string);
		}
	}
	
	public static void Error(String debugline) {
		System.out.println("ERROR: " + debugline);
	}
	
	public static void enableDebug() {
		debug = true;
	}
	
	public static void disableDebug() {
		debug = false;
	}
	
	public static boolean isDebugging() {
		return debug;
	}

}