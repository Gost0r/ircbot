package de.gost0r.ircbot;

import java.io.IOException;
import java.util.ArrayList;

import de.gost0r.ircbot.Debug.TAG;

public class MessageHandle implements Runnable {

	public ArrayList<String> msg = new ArrayList<String>();
	private ArrayList<String> handled = new ArrayList<String>();
	private boolean ended;
	private int bytes;
	private int linespersecond;
	private IRCBot bot;
	private boolean toflush;
	
	private final int MAX_LINES_PER_SECOND = 3;
	
	public MessageHandle(IRCBot bot) {
		ended = false;
		toflush = false;
		bytes = 0;
		linespersecond = 0;
		this.bot = bot;
		Debug.Log(TAG.MSGHDL, "init");
	}
	
	@Override
	public void run() {
		long starttime = System.currentTimeMillis();
		while (!ended) {
			long time = System.currentTimeMillis() - starttime;
			while (bytes < 300 && time < 2000 && linespersecond < MAX_LINES_PER_SECOND){
				time = System.currentTimeMillis() - starttime;
				synchronized (msg) {
					if (msg.isEmpty()) break;
					for (String line : msg) {
						Debug.Log(TAG.MSGHDL, "current line: " + line);
						bytes += line.length();
						linespersecond++;
						Debug.Log(TAG.MSGHDL, "current bytes: " + bytes);
						if (bytes > 300) {
							Debug.Log(TAG.MSGHDL, "too many bytes -> break");
							break;
						} else if (linespersecond > MAX_LINES_PER_SECOND) {
							Debug.Log(TAG.MSGHDL, "too many lines -> break");
							break;
						} else {
							try {
								Debug.Log(TAG.MSGHDL, "write line");
								Debug.Log(TAG.OUT, line);
								bot.writer.write(line);
								toflush = true;
								handled.add(line);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					try {
						if (toflush) {
							Debug.Log(TAG.MSGHDL, "flushing lines");
							bot.writer.flush();
							toflush = false;
							bot.updatePing();
							Debug.Log(TAG.MSGHDL, "flushing lines success");
						}
					} catch (IOException e) {
						Debug.Log(TAG.MSGHDL, "flushing lines failed");
						e.printStackTrace();
					}
					for (String string : handled) {
						msg.remove(string);
					}
				}
			}
			while (bytes != 0) {
				try {
					bytes--;
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			time = System.currentTimeMillis() - starttime;
			
			if (time > 1000) {
				starttime = System.currentTimeMillis();
				linespersecond--;
				if (linespersecond < 0) linespersecond = 0;
			}
			Debug.Log(TAG.MSGHDL, String.valueOf(time) + " millis passed - rerun");
		}
	}
	
	public void stop() {
		ended = true;
	}
	
	public void add(String line) {
		synchronized(msg) {
			msg.add(line);
		}
	}

}
