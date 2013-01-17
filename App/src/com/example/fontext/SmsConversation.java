package com.example.fontext;

import android.text.Spanned;

/**
 * Simple class to hold Sms conversation info.
 * @author Ben Kaiser
 */
public class SmsConversation {

	//Local member variables
	private String thread_id;
	private String contactName;
	private String contactId;
	private String contactNumber;
	private Spanned displayMsg;
	private long messageTime;

	/**
	 * Constructor for SmsConversation class
	 * @param tid	theread id
	 * @param cnam	contact name
	 * @param cid	contact id
	 * @param cnum	contact phone number
	 * @param dmsg	message to display in inbox (most recent message)
	 * @param mtm	sent time of most recent message
	 */
	public SmsConversation(String tid, String cnam, String cid, String cnum, Spanned dmsg, long mtm){
		thread_id = tid;
		contactName = cnam;
		contactId = cid;
		contactNumber = cnum;
		displayMsg = dmsg;
		messageTime = mtm;
	}

	//Modifier functions
	public void setThreadId(String tid){thread_id = tid;}
	public void setContactName(String cnam){contactName = cnam;}
	public void setContactId(String cid){contactId = cid;}
	public void setContactNumber(String cnum){contactNumber = cnum;}
	public void setMessageTime(long mtm){messageTime = mtm;}
	public void setDisplayMsg(Spanned dmsg){displayMsg = dmsg;}

	//Accessor functions
	public String getThreadId(){return thread_id;}
	public String getContactName() {return contactName;}
	public String getContactId(){return contactId;}
	public String getContactNumber(){return contactNumber;}
	public long getMessageTime(){return messageTime;}
	public Spanned getDisplayMsg(){return displayMsg;}
}