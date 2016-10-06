package de.gost0r.ircbot;

import de.gost0r.ircbot.Debug.TAG;

public class PingHandle implements Runnable {
	
	private boolean stopped;
	private IRCBot bot;
	
	private long lastPing;

	public PingHandle(IRCBot ircBot) {
		this.stopped = false;
		this.bot = ircBot;
		updatePing();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(10000); // 10s delay after creation
			while (!stopped) {
				Debug.Log(TAG.PING, "TIME LEFT " + (System.currentTimeMillis() - lastPing) + "/" + (int) 1000 * 3.5 * 60);
				if (System.currentTimeMillis() > lastPing + 1000 * 3.5 * 60) {
					Debug.Log(TAG.PING, "Enforcing reconnect...");
					bot.reconnect();
					return;
				}
				Thread.sleep(2000);
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	public void stop() {
		stopped = true;
	}

	public void updatePing() {
		lastPing = System.currentTimeMillis();
	}

}
