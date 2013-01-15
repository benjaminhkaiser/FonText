package com.example.fontext;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Conversation extends Activity {

	public String thread_id;

	//Long click listener for bold button
	private OnLongClickListener lngclkBold = new OnLongClickListener() {
	    @Override
		public boolean onLongClick(View view) {
	    	//Get text box and string from text box
			EditText txtReply = (EditText) findViewById(R.id.txtReply);
			String text = Html.toHtml(txtReply.getText());
			
			//remove excess HTML tags
			text = text.replace("<p dir=ltr>", "").replace("</p>", "");
			text = text.replace("\n","");
			
			//remove bold tags
			text = Compose.removeFormatting(text, 'b');
			txtReply.setText(Html.fromHtml(text));
	    	return true;
		}
	};
	
	//Long click listener for italics button
	private OnLongClickListener lngclkItalics = new OnLongClickListener() {
	    @Override
		public boolean onLongClick(View view) {
	    	//Get text box and string from text box
			EditText txtReply = (EditText) findViewById(R.id.txtReply);
			String text = Html.toHtml(txtReply.getText());
			
			//remove excess HTML tags
			text = text.replace("<p dir=ltr>", "").replace("</p>", "");
			text = text.replace("\n","");
			
			//remove italics tags
			text = Compose.removeFormatting(text, 'i');
			txtReply.setText(Html.fromHtml(text));
	    	return true;
		}
	};
	
	//Long click listener for underline button
	private OnLongClickListener lngclkUnderline = new OnLongClickListener() {
	    @Override
		public boolean onLongClick(View view) {
	    	//Get text box and string from text box
			EditText txtReply = (EditText) findViewById(R.id.txtReply);
			String text = Html.toHtml(txtReply.getText());
			
			//remove excess HTML tags
			text = text.replace("<p dir=ltr>", "").replace("</p>", "");
			text = text.replace("\n","");
			
			//remove underline tags
			text = Compose.removeFormatting(text, 'u');
			txtReply.setText(Html.fromHtml(text));
	    	return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
		
		//register long click listeners
		Button btnBold = (Button)findViewById(R.id.btnReplyBold);
	    btnBold.setOnLongClickListener(lngclkBold);
	    Button btnItalics = (Button)findViewById(R.id.btnReplyItalics);
	    btnItalics.setOnLongClickListener(lngclkItalics);
	    Button btnUnderline = (Button)findViewById(R.id.btnReplyUnderline);
	    btnUnderline.setOnLongClickListener(lngclkUnderline);
		
		//Get extra info from intent
		Intent intent = getIntent();
		String sender = intent.getStringExtra("sender");
		thread_id = intent.getStringExtra("thread_id");
		
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
	    ArrayList<Sms> arylstSmsMessages = new ArrayList<Sms>();
	    
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
	    			//Get body and sender address, then create Sms object and add to list
	    			String body = Compose.decodeMessage(inboxCursor.getString(inboxCursor.getColumnIndex("body")));
	    			String senderNum = inboxCursor.getString(inboxCursor.getColumnIndex("address"));
	    			Sms msg = new Sms(Html.fromHtml(body), senderNum, recmsgTime, false);
	    			arylstSmsMessages.add(0, msg);
	    			inboxCursor.moveToNext();
	    		} catch (Exception e) {continue;}
	    	} else {
	    		try{
	    			//Get body and device phone number, then create Sms object and add to list
	    			String body = Compose.decodeMessage(sentCursor.getString(sentCursor.getColumnIndex("body")));
	    			TelephonyManager tMgr = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
	    			String senderNum = tMgr.getLine1Number();
	    			Sms msg = new Sms(Html.fromHtml(body), senderNum, sentmsgTime, true);
	    			arylstSmsMessages.add(0, msg);
	    			sentCursor.moveToNext();
	    		} catch (Exception e) {continue;}
	    	}
	    }
	    
	    //Set up the adapter for the list.
	    ListView conversationListView = (ListView) findViewById(R.id.lstConvoThread);
		conversationListView.setAdapter(new SmsListAdapter(this, 0, arylstSmsMessages));
		/* resourceId is being passed as 0 because it is determined on a line-by-line basis
		 * by the adapter. It has to be able to apply difference layout resources to sent
		 * messages versus received messages.
		 */
		
		//Scroll to the bottom of the list
		conversationListView.setSelection(arylstSmsMessages.size() - 1);
	}

	/**
	 * Apply chosen formatting to highlighted text.
	 * @param	view	view from compose activity. contains num, msg, and tag. 
	 * @return			void
	 */
	public void formatText(View view){
		char c = ' ';
		if (view.getTag().equals("b")) c = '*';
		if (view.getTag().equals("i")) c = '`';
		if (view.getTag().equals("u")) c = '_';
		
		//Get text box and string from text box
		EditText txtReply = (EditText) findViewById(R.id.txtReply);
		String text = Html.toHtml(txtReply.getText());
		
		//remove excess HTML tags
		text = text.replace("<p dir=ltr>", "").replace("</p>", "");
		text = text.replace("\n","");
				
		//insert * before and after selected text
		int selStart = txtReply.getSelectionStart();
		int selEnd = txtReply.getSelectionEnd();
		text = Compose.insertIntoFormattedText(text, c, selStart);
		text = Compose.insertIntoFormattedText(text, c, selEnd + 1);
				
		//convert to HTML and update textbox with formatted text
		txtReply.setText(Html.fromHtml(Compose.decodeMessage(text)));
		
		//rehighlight right text
		txtReply.setSelection(selStart, selEnd);	
	}
	
	/**
	 * Gets info from view and sends SMS with send confirmation.
	 * Gets message and phone nunmber, creates a receiver to
	 * confirm send, then sends SMS. If SMS succeeds, clears
	 * text field in view.
	 * @param  view view from conversation activity. contains num and msg.
	 * @return      void
	 */
	public void sendMessage(View view){
		String SENT = "SMS_SENT";
		
		//Get address of conversation partner
		String where = "thread_id=" + thread_id;
	    Cursor inboxCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, where, null, null);
	    inboxCursor.moveToFirst();
	    String destination = inboxCursor.getString(inboxCursor.getColumnIndexOrThrow("address")).toString();
        
		//get message to send
		EditText txtReply = (EditText) findViewById(R.id.txtReply);
		String msg = Html.toHtml(txtReply.getText());
		
		//remove excess HTML tags from message
		msg = msg.replace("<p dir=ltr>", "").replace("</p>", "");
		msg = msg.replace("\n","");
		
		//clear text fields
		txtReply.setText("");
		
		//initialize pendingintent for send confirmation
		PendingIntent piSent = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		
		//set up intent receiver for sending
		registerReceiver(new BroadcastReceiver(){
			//override the onReceive function to display a toast containing error message
            @Override
            public void onReceive(Context arg0, Intent arg1) {	//args are unused
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "Message sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Text failed", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;                    	
                }	//close switch
            }	//close onReceive()
        }, new IntentFilter(SENT));
		// on above line, } closes BroadcastReceiver constructor, ) closes registerReciever call
		
		//initialize smsmanager and send SMS
		SmsManager smsMgr = SmsManager.getDefault();
		try{
			smsMgr.sendTextMessage(destination,null,Compose.encodeMessage(msg),piSent,null);
		} catch (IllegalArgumentException e){
			Toast.makeText(getBaseContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
		}	// close catch	
		
	} //close sendMessage()
}
