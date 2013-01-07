package com.example.fontext;

import java.util.ArrayList;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Receive extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receive);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_receive, menu);
		return true;
	}
	
	/**
	 * Helper fn: launches compose activity
	 * @param view	view of the Receive activity
	 */
	public void launchCompose(View view){
		Intent intent = new Intent(this, Compose.class);
		startActivity(intent);
	}

	//Global arraylist containing all SMSs.
    ArrayList<String> smsList = new ArrayList<String>();
    
    
    /**
     * Refreshes listview with all SMS's in database. Called upon refresh btn press.
     * @param view view of the Receive activity
     */
	public void refreshMsgs(View view) {
		//Instantiate a contentResolver to access SMS's stored in sms database
		ContentResolver contentResolver = getContentResolver();
		Cursor cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);

		//Get indexes of message body and sender address
		int indexBody = cursor.getColumnIndex(SmsReceiver.BODY);
		int indexAddr = cursor.getColumnIndex(SmsReceiver.ADDRESS);
		
		//If there are no messages, return
		if (indexBody<0 || !cursor.moveToFirst()) return;
		
		//Clear list of existing messages
		smsList.clear();
		
		//For every message in the database, create a string and add it to the smsList
		do{
			String message = "Sender: " + cursor.getString( indexAddr ) + "\n" + cursor.getString( indexBody );
			smsList.add(message);
		}
		while(cursor.moveToNext());

		//Get the listView and create an Adapter to convert each string into a list item
		ListView smsListView = (ListView) findViewById(R.id.lstInbox);
		smsListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsList));
		
		//Create the listener for list item clicks
		smsListView.setOnItemClickListener(new OnItemClickListener(){
			//On clicks, separate string into body and sender and print toast
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id){
        		try{
        		    String[] splitted = smsList.get(pos).split("\n"); 
        			String data = splitted[0] + "\n";
        			for (int i=1; i<splitted.length; ++i)
        			    data += splitted[i];
        			
        			Toast.makeText(getBaseContext(), data, Toast.LENGTH_SHORT ).show();
        		}	//close try
        		catch (Exception e) { e.printStackTrace(); }
        	}	//close onItemClick()
        });	//close OnItemClickListener, then close setOnItemClickListener() and end line
	}
	
}
