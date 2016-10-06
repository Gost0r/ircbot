package de.gost0r.ircbot;

import java.util.ArrayList;

import de.gost0r.ircbot.Debug.TAG;

public class IRCChannel {
	
	private static ArrayList<IRCChannel> channellist = new ArrayList<IRCChannel>();
	
	private String name;
	private boolean hasJoined;
	private ArrayList<User> oplist = new ArrayList<User>();
	private ArrayList<User> voicelist = new ArrayList<User>();
	private ArrayList<User> userlist = new ArrayList<User>();
	
	public IRCChannel(String name) {
		this.name = name;
		channellist.add(this);
	}

	public String getName() {
		return name;
	}

	public boolean hasJoined() {
		return hasJoined;
	}

	public void joined() {
		hasJoined = true;
	}
	
	public void addOp(User user) {
		for (User opuser : oplist) {
			if (opuser.getNick().equals(user.getNick())) {
				return;
			}
		}
		oplist.add(user);
	}
	
	public void removeOp(User user) {
		for (User opuser : oplist) {
			if (opuser.getNick().equals(user.getNick())) {
				oplist.remove(opuser);
				break;
			}
		}
	}
	
	public void addVoice(User user) {
		for (User voiceuser : voicelist) {
			if (voiceuser.getNick().equals(user.getNick())) {
				return;
			}
		}
		voicelist.add(user);
	}
	
	public void removeVoice(User user) {
		for (User voiceuser : voicelist) {
			if (voiceuser.getNick().equals(user.getNick())) {
				voicelist.remove(voiceuser);
				break;
			}
		}
	}
	
	public void addUser(User user) {
		for (User uuser : userlist) {
			if (uuser.getNick().equals(user.getNick())) {
				return;
			}
		}
		userlist.add(user);
	}
	
	public void removeUser(User user) {
		for (User uuser : userlist) {
			if (uuser.getNick().equals(user.getNick())) {
				userlist.remove(uuser);
				break;
			}
		}
		removeVoice(user);
		removeOp(user);
	}

	public User getUser(String nick) {
		for (User user : userlist) {
			if(user.getNick().equals(nick)) return user;
		}
		return null;
	}

	public static IRCChannel get(String channel) {
		for (IRCChannel chan : channellist) {
			if (chan.getName().equals(channel))
				return chan;
		}
		return new IRCChannel(channel);
	}

	public boolean isOpped(User user) {
		for (User xuser : oplist) {
			if (xuser.getNick().equals(user.getNick())) return true;
		}
		return false;
	}

	public boolean isVoiced(User user) {
		for (User xuser : voicelist) {
			if (xuser.getNick().equals(user.getNick())) return true;
		}
		return false;
	}

	public void printUserList() {
		String msg = "Oplist " + getName() + ":";
		for (User user : oplist) {
			msg += " " + user.toString();
		}
		
		msg += "\r\nVoicelist " + getName() + ":";
		for (User user : voicelist) {
			msg += " " + user.toString();
		}
		
		msg += "\r\nUserlist " + getName() + ":";
		for (User user : userlist) {
			msg += " " + user.toString();
		}
		Debug.Log(TAG.MAIN, msg);
	}

}
