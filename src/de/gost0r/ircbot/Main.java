package de.gost0r.ircbot;

public class Main {
	
	public static void main(String[] args) {
		IRCBot irc = new IRCBot("irc.quakenet.org", 6667);
		irc.setNick("IRCBot");
		irc.setIdent("Ident");
		irc.setRealName("Realname");
		irc.addChannel("#example");
		irc.setQauth("example", "password");
		
		Thread ircThread = new Thread(irc);
		ircThread.setName("ircbotThread");
		ircThread.start();
	}
}
