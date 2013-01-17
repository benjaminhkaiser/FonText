package com.example.fontext;

import android.text.Spanned;

/**
 * Simple class to hold Sms message info.
 * @author Ben Kaiser
 */
public class SmsMessage {

	//Local member variables
	private Spanned body;
	private String senderNumber;
	private long timeSent;
	private boolean sent;	//true is sent, false is received
	private int id;

	/**
	 * Constructor for SmsMessage
	 * @param b		body of message
	 * @param snu	phone number of sender
	 * @param ts	time message was sent
	 * @param s		true if sent, false if received
	 * @param i		message id
	 */
	public SmsMessage(Spanned b, String snu, long ts, boolean s, int i){
		body = b;
		senderNumber = snu;
		timeSent = ts;
		sent = s;
		id = i;
	}

	//Modifier functions
	public void setBody(Spanned b){ body = b; }
	public void setSenderNum(String s){ senderNumber = s; }
	public void setTimeSent(long ts){ timeSent = ts; }
	public void setSent(boolean s){ sent = s; }
	public void setId(int i){ id = i; }

	//Accessor functions
	public Spanned getBody() {return body;}
	public String getSenderNum() {return senderNumber;}
	public long getTimeSent() {return timeSent;}
	public boolean getSent() {return sent;}
	public int getId() {return id;}
}