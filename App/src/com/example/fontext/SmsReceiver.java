package com.example.fontext;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver{
	public static final String SMS_EXTRA_NAME = "pdus";
	public static final String SMS_URI = "content://sms";
	
	public static final String ADDRESS = "address";
    public static final String PERSON = "person";
    public static final String DATE = "date";
    public static final String READ = "read";
    public static final String STATUS = "status";
    public static final String TYPE = "type";
    public static final String BODY = "body";
    public static final String SEEN = "seen";
    
    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;
    
    public static final int MESSAGE_IS_NOT_READ = 0;
    public static final int MESSAGE_IS_READ = 1;
    
    public static final int MESSAGE_IS_NOT_SEEN = 0;
    public static final int MESSAGE_IS_SEEN = 1;
		
	//TODO: After figuring out how to disable stock SMS app, remove warning supression    
	@SuppressWarnings("unused")
	@Override
	public void onReceive(Context context, Intent intent) {
		// Get the SMS map from Intent
        Bundle extras = intent.getExtras();
        String messages = "";
        
        if (extras != null){
            //Get received SMS array
            Object[] smsExtra = (Object[]) extras.get( SMS_EXTRA_NAME );
            
            //Get ContentResolver object for pushing encrypted SMS to incoming folder
            ContentResolver contentResolver = context.getContentResolver();
            
            //Get messages from bundle
            for (int i=0; i<smsExtra.length; ++i){
                SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
                 
                String body = sms.getMessageBody().toString();
                String address = sms.getOriginatingAddress();
                 
                messages += "SMS from " + address + " :\n";                    
                messages += body + "\n";
                
                /*TODO: Stop stock SMS app from receiving SMS's and storing to database.
                 * For the meantime, or maybe permanently if that's a better solution, just
                 * don't do it here.        
                 */
                //putSmsToDatabase(contentResolver, sms);
            }
             
            //Display toast containing message
            //TODO: make this a notification instead of a toast
            Toast.makeText(context, messages, Toast.LENGTH_SHORT ).show();
        }
    }
	
	//TODO: After figuring out how to disable stock SMS app, remove warning supression
	@SuppressWarnings("unused")
	private void putSmsToDatabase(ContentResolver contentResolver, SmsMessage sms) {
		// Create SMS row
        ContentValues values = new ContentValues();
        values.put(ADDRESS, sms.getOriginatingAddress());
        values.put(DATE, sms.getTimestampMillis());
        values.put(READ, MESSAGE_IS_NOT_READ);
        values.put(STATUS, sms.getStatus());
        values.put(TYPE, MESSAGE_TYPE_INBOX);
        values.put(SEEN, MESSAGE_IS_NOT_SEEN);
        
        try { values.put(BODY, sms.getMessageBody()); } 
        catch (Exception e) { e.printStackTrace(); }
        
        // Push row into the SMS table
        contentResolver.insert(Uri.parse(SMS_URI), values);
	}
}

