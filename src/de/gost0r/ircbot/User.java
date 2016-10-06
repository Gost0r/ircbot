package de.gost0r.ircbot;

import java.util.ArrayList;

import de.gost0r.ircbot.Debug.TAG;

public class User {
	
	public static ArrayList<User> userlist = new ArrayList<User>();
	
	private String nick;
	private Qauth qauth;
	
	public User(String nick) {
		this.nick = nick;
		userlist.add(this);
	}
	
	public void setAuth(String qauth) {
		this.qauth = Qauth.get(qauth);
		if (this.qauth == null) this.qauth = new Qauth(qauth);
	}
	
	public void setNick(String nick) {
		this.nick = nick;
	}
	
	public Qauth getQauth() {
		return qauth;
	}

	public static User get(String nick) {
		for (User user : userlist) {
			if (user.getNick().equals(nick))
				return user;
		}
		return new User(nick);
	}

	public static User get(Qauth qauth) {
		for (User user : userlist) {
			if (user.getQauth().equals(qauth))
				return user;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return nick;
	}

	public String getNick() {
		return nick;
	}
	
	public static void printUserList() {
		for (User user : userlist) {
			Debug.Log(TAG.MAIN, user.getNick() + " - " + user.getQauth());
		}
	}
}
