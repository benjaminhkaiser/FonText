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
import android.database.Cursor;
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
                      
            //Get messages from bundle and commit to database
            for (int i=0; i<smsExtra.length; ++i){
                SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);                     
            	addSmsToDatabase(contentResolver, sms);
            }
            
            displayNotification(context);
                       
            //Stop SMS from being dispatched to other receivers
            this.abortBroadcast();
        }
    }
	
	/**
	 * Helper fn: inserts SMS into SMS database.
	 * @param contentResolver	contentResolver for base application context
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
	 * Notification is expandable to show call and view contact actions, but they
	 * don't do anything yet.
	 * Touching the notification simply opens up the compose view. Eventually
	 * it will take you to the thread the message is regarding.
	 * TODO: Add version for pre-JellyBean
	 * @param context	application context
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void displayNotification(Context context){
		//Query SMS inbox for unread messages
		Uri uriInbox = Uri.parse("content://sms/inbox");
        Cursor c = context.getContentResolver().query(uriInbox, null, "read = 0", null, null);
        
		//Create intent to be executed upon notification touch
		Intent intent = new Intent(context, Compose.class);
		PendingIntent pIntent = PendingIntent.getActivity(context, 1, intent, 0);
		
		//If there are no objects in the result set, return. This should never happen.
		if (!c.moveToNext()) return;
		
		if (c.getCount() == 1){		//if there is only one unread SMS
			//Get info from SMS
			String address = c.getString(c.getColumnIndex(ADDRESS));
		    String body = c.getString(c.getColumnIndex(BODY));
		    
			//Create and build notification
			Notification.Builder noti = new Notification.Builder(context)
	        	.setContentTitle("New SMS message from " + address)
	        	.setContentText(Html.fromHtml(Compose.decodeMessage(body)))
	        	.setSmallIcon(R.drawable.ic_launcher)
	        	.setContentIntent(pIntent)
	        	.addAction(R.drawable.ic_launcher, "Call", pIntent)
	        	.addAction(R.drawable.ic_launcher, "View Contact", pIntent)
	        	.setAutoCancel(true);
			
			//Instantiate notification manager
			NotificationManager notificationManager = 
					  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			//Post notification to status bar
			notificationManager.notify(1, noti.build());
		} else {					//if there are multiple unread SMS messages
			//Create and build notification
			Notification.Builder noti = new Notification.Builder(context)
	        	.setContentTitle("New SMS messages")
	        	.setContentText("You've received new SMS messages.")
	        	.setContentInfo(String.valueOf(c.getCount()))
	        	.setSmallIcon(R.drawable.ic_launcher)
	        	.setContentIntent(pIntent)
	        	.addAction(R.drawable.ic_launcher, "Call", pIntent)
	        	.addAction(R.drawable.ic_launcher, "View Contact", pIntent)
	        	.setAutoCancel(true);
			
			//Instantiate notification manager
			NotificationManager notificationManager = 
					  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			//Post notification to status bar
			notificationManager.notify(1, noti.build());
		}
		
	}
}

