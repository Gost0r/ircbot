package de.gost0r.ircbot;

import java.util.ArrayList;

public class Qauth {
	
	private static ArrayList<Qauth> qauthlist = new ArrayList<Qauth>();
	private String qauth;
	
	public Qauth(String qauth) {
		this.qauth = qauth;
		qauthlist.add(this);
	}
	
	@Override
	public String toString() {
		return qauth;
	}
	
	public String getValue() {
		return qauth.toString();
	}

	public static Qauth get(String qauth) {
		for (Qauth auth : qauthlist) {
			if (auth.toString().equals(qauth)) return auth;
		}
		return null;
	}
}
