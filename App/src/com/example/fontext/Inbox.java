package com.example.fontext;

import java.util.ArrayList;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Inbox extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inbox);
		refreshMsgs(this.getCurrentFocus());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_inbox, menu);
		return true;
	}
	
	/**
	 * Helper fn: launches compose activity
	 * @param view	view of the Inbox activity
	 */
	public void launchCompose(View view){
		Intent intent = new Intent(this, Compose.class);
		startActivity(intent);
	}

	//Global arraylist containing all SMSs.
    ArrayList<Spanned> conversationList = new ArrayList<Spanned>();
    
    /**
     * Refreshes listview with all SMS's in database. Called upon refresh btn press.
     * @param view view of the Inbox activity
     */
	public void refreshMsgs(View view) {
		
		//Instantiate a contentResolver to access SMS conversations
		ContentResolver contentResolver = getContentResolver();
		Cursor conversationCursor = contentResolver.query(Uri.parse("content://sms/conversations"), null, null, null, "date desc");
		
		//Clear list of existing messages
		conversationList.clear();
		
		//If there are no conversations, return.
		if (!conversationCursor.moveToFirst()) return;
			
		//For every converation in the database, ...
		do{
			//get thread id, then get inbox messages related to that id 
			String where = "thread_id=" + conversationCursor.getString(conversationCursor.getColumnIndex("thread_id"));
		    Cursor inboxCursor= getContentResolver().query(Uri.parse("content://sms/inbox"), null, where, null, "date desc"); 
		    String address = "";
		    
		    //get address of conversation partner
		    if (inboxCursor.moveToFirst()){
		    	address = inboxCursor.getString(inboxCursor.getColumnIndexOrThrow("address")).toString();

		    	//get message and sender and convert shortcode in body to HTML tags
		    	String body = Compose.decodeMessage(inboxCursor.getString(inboxCursor.getColumnIndex("body")));
				String sender = SmsReceiver.getContactbyNumber(address, this);
				String message = sender + ": " + body;
				
				//If message is unread, highlight in red
				//TODO: fix this... it's not working
				if (inboxCursor.getInt(inboxCursor.getColumnIndex("read")) == 0){
					message = "<font color=\"red\">" + message + "<\font>";
				}
				
				conversationList.add(Html.fromHtml(message));
		    }
		
			
		}
		while(conversationCursor.moveToNext());

		//Get the listView and create an Adapter to convert each string into a list item
		ListView smsListView = (ListView) findViewById(R.id.lstInbox);
		smsListView.setAdapter(new ArrayAdapter<Spanned>(this, android.R.layout.simple_list_item_1, conversationList));
		
		//Create the listener for list item clicks
		smsListView.setOnItemClickListener(new OnItemClickListener(){
			//On clicks, go to conversation view
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id){
            	//Find the conversation that was clicked
            	Uri uriConvo = Uri.parse("content://sms/conversations");
                Cursor conversationCursor = getContentResolver().query(uriConvo, null, null, null, "date desc");
                conversationCursor.moveToPosition(pos);
                
                //get Thread id
                String thread_id = conversationCursor.getString(conversationCursor.getColumnIndex("thread_id"));
                
                //get sender name
                String where = "thread_id=" + thread_id;
    		    Cursor inboxCursor= getContentResolver().query(Uri.parse("content://sms/inbox"), null, where, null, "date desc");     		    
    		    inboxCursor.moveToFirst();
    		    String sender = inboxCursor.getString(inboxCursor.getColumnIndexOrThrow("address")).toString();
    		    sender = SmsReceiver.getContactbyNumber(sender, getBaseContext());
    		    
                Intent conversation = new Intent(getBaseContext(), Conversation.class);
                conversation.putExtra("thread_id", thread_id);
                conversation.putExtra("sender", sender);
                startActivity(conversation);

                finish();
        	}	//close onItemClick()
        });	//close OnItemClickListener, then close setOnItemClickListener() and end line
	}
	
}

