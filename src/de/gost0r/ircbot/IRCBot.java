package de.gost0r.ircbot;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import de.gost0r.ircbot.Debug.TAG;

public class IRCBot implements Runnable {
	
	private String server;
	private int port;

	private String nick;
	private String ident = "IDENT";
	private String realname = "REALNAME";
	
	private ArrayList<IRCChannel> channels = new ArrayList<IRCChannel>();

	private String authname;
	private String authpass;
	
	private Socket socket;
	protected BufferedWriter writer;
	protected BufferedReader reader;

	private Thread msgThread;
	private MessageHandle msgHandle;

	private Thread pingThread;
	private PingHandle pingHandle;
	
	private boolean GETUSERS = false;

	public IRCBot(String server, int port) {
		this.server = server;
		this.port = port;
	}
	
	public void run() {
		connect();
		loop();		
	}
	
	protected void connect() {
		try {
			Debug.Log(TAG.MAIN, "connection request");
			this.socket = new Socket(this.server, this.port);
			this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			msgHandle = new MessageHandle(this);
			msgThread = new Thread(msgHandle);
			msgThread.setName("msgThread");
			msgThread.start();
			
			send("NICK " + nick);
			send("USER " + ident + " 0 0 " + realname);

			pingHandle = new PingHandle(this);
			pingThread = new Thread(pingHandle);
			pingThread.setName("pingThread");
			pingThread.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loop() {
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				
				// Visual reply
				Debug.Log(TAG.INC, line);
				
				// Split data
				String[] data = line.split(" ");
				if (data.length <= 1) data = new String[2];
				String command = data[1];
				
				// Ping check
				if (line.startsWith("PING ")) sendPong(line.substring(5));
				
				// command checking
				switch(command) {
				case "433": nickTaken(); break;
				case "513": sendPong(data[8]); break;
				case "353": getUserOnJoin(data[4], line.substring(line.indexOf(":", 1)+1)); break;
				case "001": setupBot(); break;
				case "PRIVMSG": recvmsg(data[0], data[2], line.substring(line.indexOf(":", 1)+1));break;
				case "NOTICE": recvnotice(data[0], line.substring(line.indexOf(":", 1)+1));break;
				case "QUIT": recvquit(data[0]);break;
				case "PART": recvpart(data[0], data[2]);break;
				case "KICK": recvkick(data[0], data[3], data[2]);break;
				case "JOIN": recvjoin(data[0], data[2]);break;
				case "NICK": recvnick(data[0], data[2]);break;
				case "MODE":
					if (!data[2].equals(nick)) {
						if (data.length == 4) {
							String prefix = data[3].substring(0,1);
							String mode = String.valueOf(data[3].charAt(1));
							recvmode(data[0], data[2], prefix + mode, null);
						} else if (data.length > 4) {
							for(int i = 1; i < data[3].length(); i++) {
								String prefix = data[3].substring(0,1);
								String mode = String.valueOf(data[3].charAt(i));
								recvmode(data[0], data[2], prefix + mode, data[4+i-1]);
							}
						}
					} break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			disconnect();
		}
	}
	
	private void getUserOnJoin(String channel, String userstring) {
		String[] users = userstring.split(" ");
		for (String user : users) {
			if (user.startsWith("@")) {
				IRCChannel.get(channel).addUser(User.get(user.substring(1)));
				IRCChannel.get(channel).addOp(User.get(user.substring(1)));
			} else if (user.startsWith("+")) {
				IRCChannel.get(channel).addUser(User.get(user.substring(1)));
				IRCChannel.get(channel).addVoice(User.get(user.substring(1)));
			} else {
				IRCChannel.get(channel).addUser(User.get(user));
			}
		}
		sendMsg("Q", "USERS " + channel);
	}

	protected void recvmode(String hostmark, String channel, String command, String nick) {
		if (command.equals("+o")) {
			IRCChannel.get(channel).addOp(IRCChannel.get(channel).getUser(nick));
		} else if (command.equals("-o")) {
			IRCChannel.get(channel).removeOp(IRCChannel.get(channel).getUser(nick));
		}
		if (command.equals("+v")) {
			IRCChannel.get(channel).addVoice(IRCChannel.get(channel).getUser(nick));
		} else if (command.equals("-v")) {
			IRCChannel.get(channel).removeVoice(IRCChannel.get(channel).getUser(nick));
		}
		IRCChannel.get(channel).printUserList();
	}
	
	protected void recvnick(String hostmark, String newnick) {
		User.get(IRCutil.getNickFromHostmark(hostmark)).setNick(newnick);
	}
	
	protected void recvjoin(String hostmark, String channel) {
		IRCChannel.get(channel).addUser(User.get(IRCutil.getNickFromHostmark(hostmark)));
		sendMsg("Q", "USERS " + channel);
	}
	
	protected void recvkick(String hostmark, String kicked, String channel) {
		IRCChannel.get(channel).removeUser(User.get(kicked));
	}
	
	protected void recvquit(String hostmark) {
		for (IRCChannel chan : channels) {
			if (chan.getUser(nick) != null) {
				chan.removeUser(User.get(IRCutil.getNickFromHostmark(hostmark)));
			}
		}
	}
	
	protected void recvpart(String hostmark, String channel) {
		IRCChannel.get(channel).removeUser(User.get(IRCutil.getNickFromHostmark(hostmark)));
	}

	protected void recvmsg(String hostmark, String channel, String message) {
		if (message.startsWith("!showop")) {
			IRCChannel.get(channel).printUserList();
		} else if (message.startsWith("!showuser")) {
			User.printUserList();
		} else if (message.startsWith("!stresstest")) {
			for (int i = 0; i <= 20; i++) {
				sendMsg(channel, "this is a stresstest " + i);
			}
		}
	}
	
	protected void recvnotice(String hostmark, String message) {
		try {
			if (IRCutil.getNickFromHostmark(hostmark).equals("Q")) {
				if (message.startsWith("Users currently on ")) {
					GETUSERS = true;
					return;
				}
				if (message.startsWith("End of list.")) {
					GETUSERS = false;
					return;
				}
				if (GETUSERS) {
					String[] data = message.split(" ");
					int count = 0;
					User user = null;
					for (String string : data) {
						if (!string.equals("")) {
							count++;
							if (count == 1) {
								string = string.replace("@", "");
								string = string.replace("+", "");
								user = User.get(string);
							} else if (count == 2) {
								if (user.getNick().length() == 1)
									string = user.getNick();
								user.setAuth(string);
							}
						}
					}
					Debug.Log(TAG.MAIN, "Setting auth: " + user.getNick() + " - " + user.getQauth());
				}
			} 
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	protected void setupBot() {
		if (authname != null && authpass != null) {
			send("PRIVMSG Q@CServe.quakenet.org :AUTH " + authname + " " + authpass);
			send("MODE " + nick + " :+x");
			IRCutil.sleep(1);
		}
		for (IRCChannel chan : channels) {
			send("JOIN " + chan.getName());
			chan.joined();
		}
	}
	
	protected void nickTaken() {
		nick += "_";
		send("NICK " + nick);
	}
	
	private void sendPong(String pong) {
		send("PONG " + pong);
	}
	
	public void setQauth(String auth, String password) {
		this.authname = auth;
		this.authpass = password;
	}

	public void setNick(String nick) {
		this.nick = nick;		
	}

	public void setIdent(String ident) {
		this.ident = ident;		
	}

	public void setRealName(String realname) {
		this.realname = realname;		
	}

	public void addChannel(String chan) {
		channels.add(new IRCChannel(chan));
	}

	public void removeChannel(String chan) {
		for(IRCChannel channel : channels) {
			if (channel.getName().equals(chan)) {
				if (channel.hasJoined()) send("PART " + chan);
				channels.remove(channel);
			}
		}
	}
	
	public void send(String msg) {
		try {
			msgHandle.add(msg + "\r\n");
			Debug.Log(TAG.MAIN, "Pushed Msg: " + msg);
			/*
			writer.write(msg + "\r\n");
			writer.flush();
			Debug.Log("Sending: " + msg);
			IRCutil.sleep(1);*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMsg(String channel, String msg) {
		send("PRIVMSG " + channel + " :" + msg);
	}

	public void sendNotice(String channel, String msg) {
		send("NOTICE " + channel + " :" + msg);
	}

	public void sendMode(String channel, String nick, String mode) {
		send("MODE " + channel + " " + mode + " " + nick);
	}
	
	public void updatePing() {
		try {
			pingHandle.updatePing();
		} catch (NullPointerException e) {
			Debug.Error(e.toString());
			// not that important
		}
	}
	
	public void disconnect() {
		try {
			Debug.Log(TAG.MAIN, "disconnect request");
			this.socket.close();
			this.writer.close();
			this.reader.close();
			msgHandle.stop();
			pingHandle.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reconnect() {
		Debug.Log(TAG.MAIN, "reconnect request");
		disconnect();
		IRCutil.sleep(3000);
		Debug.Log(TAG.MAIN, "reconnecting...");
		connect();
		loop();
	}
}
