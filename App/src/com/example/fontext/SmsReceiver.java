package com.example.fontext;

import android.annotation.SuppressLint;
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
import android.provider.BaseColumns;
import android.provider.ContactsContract;
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
            
            //Display appropriate notification based on android version
           	displayNotification(context, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN));
            
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
	 * Helper fn: Displays notification for received SMS messages.
	 * For single msg, touching notification goes to the conversation thread.
	 * For multi msgs, it goes to the inbox.
	 * In 4.1+:
	 * Single msg noti is expandable to show (non-functional) options.
	 * TODO: implement expanded options (call + view contact)
	 * Multi msg noti is expandable to inbox-style view to show each msg
	 * @param context	application context
	 * @param isJB		boolean to store if api level is 4.1+ or not
	 */
	@SuppressLint("NewApi") @SuppressWarnings("deprecation")
	private void displayNotification(Context context, boolean isJB){
		Uri uriInbox = Uri.parse("content://sms/inbox");
		Cursor inboxCursor = context.getContentResolver().query(uriInbox, null, "read = 0", null, null);
		
		//initialize notification builder
		Notification.Builder noti = new Notification.Builder(context);
		
		if (inboxCursor.getCount() == 1){		//if there is only one unread SMS
			inboxCursor.moveToNext();
			
			//Get info from SMS
			String address = inboxCursor.getString(inboxCursor.getColumnIndex(ADDRESS));
		    String body = inboxCursor.getString(inboxCursor.getColumnIndex(BODY));
		    
		    //Get contact name
		    String name = getContactNamebyNumber(address, context);
		    
			//Create intent to be executed upon notification touch
			Intent intent = new Intent(context, Conversation.class);
			intent.putExtra("sender", name);
			intent.putExtra("thread_id", inboxCursor.getString(inboxCursor.getColumnIndex("thread_id")));
			PendingIntent pIntent = PendingIntent.getActivity(context, 1, intent, 0);
		    
			//Add info to notification
			noti.setContentTitle(name)
	        	.setContentText(Html.fromHtml(Compose.decodeMessage(body)))
	        	.setSmallIcon(R.drawable.ic_launcher)
	        	.setContentIntent(pIntent)
	        	.setAutoCancel(true);
			
			//If api lvl is 4.1+, add expandable options
			if (isJB){
				noti.addAction(R.drawable.ic_launcher, "Call", pIntent)
				.addAction(R.drawable.ic_launcher, "View Contact", pIntent);
			}
		} else {					//if there are multiple unread SMS messages
			//Create intent
			Intent intent = new Intent(context, Inbox.class);
			PendingIntent pIntent = PendingIntent.getActivity(context, 1, intent, 0);
			
			//add info to notification
			noti.setContentTitle("New SMS messages")
	        	.setContentText("You've received new SMS messages.")
	        	.setContentInfo(String.valueOf(inboxCursor.getCount()))
	        	.setSmallIcon(R.drawable.ic_launcher)
	        	.setContentIntent(pIntent)
	        	.setAutoCancel(true);
			
			if (isJB){
				Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
				inboxStyle.setBigContentTitle("New SMS messages");
				
				while(inboxCursor.moveToNext()){
					String body = inboxCursor.getString(inboxCursor.getColumnIndex(BODY));
					String name = getContactNamebyNumber(inboxCursor.getString(inboxCursor.getColumnIndex(ADDRESS)), context);
					inboxStyle.addLine(name + ": " + Html.fromHtml(Compose.decodeMessage(body)));
				}
			
				noti.setStyle(inboxStyle);
			}
		}
		
		//Instantiate notification manager
		NotificationManager notificationManager = 
				  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		//Post notification to status bar
		if (isJB) notificationManager.notify(1, noti.build());
		else notificationManager.notify(1, noti.getNotification());
	}
	

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void displayJBNotification(Context context){
		//Query SMS inbox for unread messages
		Uri uriInbox = Uri.parse("content://sms/inbox");
        Cursor inboxCursor = context.getContentResolver().query(uriInbox, null, "read = 0", null, null);
		
		if (inboxCursor.getCount() == 1){		//if there is only one unread SMS
			inboxCursor.moveToNext();
			
			//Get info from SMS
			String address = inboxCursor.getString(inboxCursor.getColumnIndex(ADDRESS));
		    String body = inboxCursor.getString(inboxCursor.getColumnIndex(BODY));
		    
		    //Get contact name
		    String name = getContactNamebyNumber(address, context);
		    
			//Create intent to be executed upon notification touch
			Intent intent = new Intent(context, Conversation.class);
			intent.putExtra("sender", name);
			intent.putExtra("thread_id", inboxCursor.getString(inboxCursor.getColumnIndex("thread_id")));
			PendingIntent pIntent = PendingIntent.getActivity(context, 1, intent, 0);
		    
			//Create notification
			Notification.Builder noti = new Notification.Builder(context)
	        	.setContentTitle(name)
	        	.setContentText(Html.fromHtml(Compose.decodeMessage(body)))
	        	.setSmallIcon(R.drawable.ic_launcher)
	        	.setContentIntent(pIntent)
	        	//.addAction(R.drawable.ic_launcher, "Call", pIntent)
	        	//.addAction(R.drawable.ic_launcher, "View Contact", pIntent)
	        	.setAutoCancel(true);
			
			//Instantiate notification manager
			NotificationManager notificationManager = 
					  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			//Post notification to status bar
			notificationManager.notify(1, noti.build());
		} else {					//if there are multiple unread SMS messages
			//Create intent
			Intent intent = new Intent(context, Inbox.class);
			PendingIntent pIntent = PendingIntent.getActivity(context, 1, intent, 0);
			
			//Create notification
			Notification.Builder noti = new Notification.Builder(context)
	        	.setContentTitle("New SMS messages")
	        	.setContentText("You've received new SMS messages.")
	        	.setContentInfo(String.valueOf(inboxCursor.getCount()))
	        	.setSmallIcon(R.drawable.ic_launcher)
	        	.setContentIntent(pIntent)
	        	.setAutoCancel(true);
			
			Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
			inboxStyle.setBigContentTitle("New SMS messages");
				
			while(inboxCursor.moveToNext()){
			    String body = inboxCursor.getString(inboxCursor.getColumnIndex(BODY));
			    String name = getContactNamebyNumber(inboxCursor.getString(inboxCursor.getColumnIndex(ADDRESS)), context);
				inboxStyle.addLine(name + ": " + Html.fromHtml(Compose.decodeMessage(body)));
			}
			
			noti.setStyle(inboxStyle);
			
			//Instantiate notification manager
			NotificationManager notificationManager = 
					  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			//Post notification to status bar
			notificationManager.notify(1, noti.build());
		}
	}
	
	/**
	 * Helper fn: given phone number, return matching contact name if it exists
	 * @param number	phone number to lookup
	 * @param context	base context of application
	 * @return	contact name if contact exists, else original number
	 */
	public static String getContactNamebyNumber(String number, Context context) {
	    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	    String name = number;
	    //String contactId = number;
	    
	    ContentResolver contentResolver = context.getContentResolver();
	    Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
	            ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

	    try {
	        if (contactLookup != null && contactLookup.getCount() > 0) {
	            contactLookup.moveToNext();
	            name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
	            //contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
	        }
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }

	    return name;
	}
	
}

