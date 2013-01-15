package com.example.fontext;

import android.text.Spanned;

/**
 * Simple class to hold Sms message info.
 * @author Ben
 */
public class Sms {
	
	//Local member variables
	private Spanned body;
	private String senderNumber;
	private long timeSent;
	private boolean sent;	//true is sent, false is received
	
	//Constructor
	public Sms(Spanned b, String snu, long ts, boolean s){
		body = b;
		senderNumber = snu;
		timeSent = ts;
		sent = s;
	}
	
	//Modifier functions
	public void setBody(Spanned b){ body = b; }
	public void setSenderNum(String s){ senderNumber = s; }
	public void setTimeSent(long ts){ timeSent = ts; }
	public void setSent(boolean s){ sent = s;}
	
	//Accessor functions
	public Spanned getBody() {return body;}
	public String getSenderNum() {return senderNumber;}
	public long getTimeSent() {return timeSent;}
	public boolean getSent() {return sent;}
}
