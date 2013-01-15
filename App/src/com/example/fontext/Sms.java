package com.example.fontext;

import android.text.Spanned;

/**
 * Simple class to hold Sms message info.
 * @author Ben
 */
public class Sms {
	Spanned body;
	String senderNumber;
	//String senderName;
	long timeSent;
	boolean sent;	//true is sent, false is received
	
	public Sms(Spanned b, String snu, /*String sna,*/ long ts, boolean s){
		body = b;
		senderNumber = snu;
		//senderName = sna;
		timeSent = ts;
		sent = s;
	}
	
	public void setBody(Spanned b){ body = b; }
	public void setSenderNum(String s){ senderNumber = s; }
	//public void setSenderName(String s){ senderName = s; }
	public void setTimeSent(long ts){ timeSent = ts; }
	public void setSent(boolean s){ sent = s;}
	
	public Spanned getBody() {return body;}
	public String getSenderNum() {return senderNumber;}
	//public String getSenderName() {return senderName;}
	public long getTimeSent() {return timeSent;}
	public boolean getSent() {return sent;}
}
