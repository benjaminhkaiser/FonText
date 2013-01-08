package com.example.fontext;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.Html;

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
		
	@Override
	public void onReceive(Context context, Intent intent) {
		// Get the SMS map from Intent
        Bundle extras = intent.getExtras();
        
        if (extras != null){
            //Get received SMS array
            Object[] smsExtra = (Object[]) extras.get( SMS_EXTRA_NAME );
            
            //Get ContentResolver object for pushing encrypted SMS to incoming folder
            ContentResolver contentResolver = context.getContentResolver();
            
            //Get messages from bundle
            for (int i=0; i<smsExtra.length; ++i){
            	
            	//Create SmsMessage object and commit message to database
                SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);                     
            	addSmsToDatabase(contentResolver, sms);
                
                //Display notification         
                displayNotification(sms, context);
            }
             
            //Stop SMS from being dispatched to other receivers
            this.abortBroadcast();
        }
    }
	
	/**
	 * Helper fn: inserts SMS into SMS database.
	 * @param contentResolver
	 * @param sms	SmsMessage to be inserted into database
	 */
	private void addSmsToDatabase(ContentResolver contentResolver, SmsMessage sms) {
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
        
        //Push row into SMS table
        contentResolver.insert(Uri.parse(SMS_URI), values);
	}

	/**
	 * Helper fn: Displays notification for received SMS's
	 * Notification is expandable to show call and reply actions, but they
	 * don't do anything yet.
	 * Touching the notification simply opens up the compose view. Eventually
	 * it will take you to the thread the message is regarding.
	 * TODO: Add version for pre-JellyBean
	 * TODO: Implement support for multiple notifications 
	 * @param sms	message to notify user about
	 * @param context	Context to notify in
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void displayNotification(SmsMessage sms, Context context){
		//Create intent to be executed upon notification touch
		Intent intent = new Intent(context, Compose.class);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
				
		String notiText = "SMS from " + sms.getDisplayOriginatingAddress() + "\n";
		notiText += sms.getDisplayMessageBody();
		
		//Create and build notification
		Notification noti = new Notification.Builder(context)
        	.setContentTitle("New SMS")
        	.setContentText(Html.fromHtml(notiText)).setSmallIcon(R.drawable.ic_launcher)
        	.setContentIntent(pIntent)
        	.addAction(R.drawable.ic_launcher, "Call", pIntent)
        	.addAction(R.drawable.ic_launcher, "Reply", pIntent).build();
		
		//Instantiate notification manager
		NotificationManager notificationManager = 
				  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		//Hide notification after it's touched
		noti.flags |= Notification.FLAG_AUTO_CANCEL;

		//Post notification to status bar
		notificationManager.notify(0, noti);
	}
}

