package com.example.fontext;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Conversation extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
		
		//Get extra info from intent
		Intent intent = getIntent();
		String sender = intent.getStringExtra("sender");
		String thread_id = intent.getStringExtra("thread_id");
		
		//Set contact name to text view at top of page
		TextView lblPerson = (TextView) findViewById(R.id.lblPerson);
		lblPerson.setText(sender);
		
		createConversationThread(sender, thread_id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_conversation, menu);
		return true;
	}
	
	/**
	 * Create and display conversation thread of contact
	 * @param sender	name of contact
	 * @param thread_id	thread_id of conversation thread
	 */
	public void createConversationThread(String sender, String thread_id){
		
		//Get cursors for sent and received messages
		String where = "thread_id=" + thread_id;
	    Cursor inboxCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, where, null, "date desc");
	    inboxCursor.moveToFirst();
	    Cursor sentCursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, where, null, "date desc");
	    sentCursor.moveToFirst();
	    
	    //Initialize list to hold messages in chronological order
	    ArrayList<String> arylstConversation = new ArrayList<String>();
	    
	    //For every message in both cursor sets
	    for (int i = 0; i < (inboxCursor.getCount() + sentCursor.getCount()); i++){    	
	    	long recmsgTime = 0, sentmsgTime = 0;
	    	
	    	//If there are remaining messages, get the sent times for each message
	    	if (!inboxCursor.isAfterLast())
	    		recmsgTime= inboxCursor.getLong(inboxCursor.getColumnIndex("date"));
	    	if (!sentCursor.isAfterLast())
	    		sentmsgTime = sentCursor.getLong(sentCursor.getColumnIndex("date"));
	    	
	    	//Add whichever message is less recent to the beginning of the list
	    	if (recmsgTime > sentmsgTime){
	    		try{
	    			arylstConversation.add(0, sender + ": " + inboxCursor.getString(inboxCursor.getColumnIndex("body")));
	    			inboxCursor.moveToNext();
	    		} catch (Exception e) {continue;}
	    	} else {
	    		try{
	    			arylstConversation.add(0, "Me: " + sentCursor.getString(sentCursor.getColumnIndex("body")));
	    			sentCursor.moveToNext();
	    		} catch (Exception e) {continue;}
	    	}
	    }
	    
	    //Set up the adapter for the list and scroll to the bottom
	    ListView conversationListView = (ListView) findViewById(R.id.lstConvoThread);
		conversationListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arylstConversation));
		conversationListView.setSelection(arylstConversation.size() - 1);
	}

}
