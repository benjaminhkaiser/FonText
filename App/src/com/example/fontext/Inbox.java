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
import android.widget.Toast;

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
				
				conversationList.add(Html.fromHtml(message));
		    }
		
			
		}
		while(conversationCursor.moveToNext());

		//Get the listView and create an Adapter to convert each string into a list item
		ListView smsListView = (ListView) findViewById(R.id.lstInbox);
		smsListView.setAdapter(new ArrayAdapter<Spanned>(this, android.R.layout.simple_list_item_1, conversationList));
		
		//Create the listener for list item clicks
		smsListView.setOnItemClickListener(new OnItemClickListener(){
			//On clicks, separate string into body and sender and print toast
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id){
        		try{
        			String message = Html.toHtml(conversationList.get(pos)).replace("<p dir=ltr>", "").replace("</p>", "");
        		    String[] splitted = message.split("\n"); 
        			String data = splitted[0] + "\n";
        			for (int i=1; i<splitted.length; ++i)
        			    data += splitted[i];
        			
        			Toast.makeText(getBaseContext(), Html.fromHtml(data), Toast.LENGTH_SHORT ).show();
        		}	//close try
        		catch (Exception e) { e.printStackTrace(); }
        	}	//close onItemClick()
        });	//close OnItemClickListener, then close setOnItemClickListener() and end line
	}
	
}

