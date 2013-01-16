package com.example.fontext;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Compose extends Activity {
	
	//Long click listener for bold button
	private OnLongClickListener lngclkBold = new OnLongClickListener() {
	    @Override
		public boolean onLongClick(View view) {
	    	//Get text box and string from text box
			EditText txtMsg = (EditText) findViewById(R.id.txtMessage);
			String text = Html.toHtml(txtMsg.getText());
			
			//remove excess HTML tags
			text = text.replace("<p dir=ltr>", "").replace("</p>", "");
			text = text.replace("\n","");
			
			//remove bold tags
			text = removeFormatting(text, 'b');
			txtMsg.setText(Html.fromHtml(text));
	    	return true;
		}
	};
	
	//Long click listener for italics button
	private OnLongClickListener lngclkItalics = new OnLongClickListener() {
	    @Override
		public boolean onLongClick(View view) {
	    	//Get text box and string from text box
			EditText txtMsg = (EditText) findViewById(R.id.txtMessage);
			String text = Html.toHtml(txtMsg.getText());
			
			//remove excess HTML tags
			text = text.replace("<p dir=ltr>", "").replace("</p>", "");
			text = text.replace("\n","");
			
			//remove italics tags
			text = removeFormatting(text, 'i');
			txtMsg.setText(Html.fromHtml(text));
	    	return true;
		}
	};
	
	//Long click listener for underline button
	private OnLongClickListener lngclkUnderline = new OnLongClickListener() {
	    @Override
		public boolean onLongClick(View view) {
	    	//Get text box and string from text box
			EditText txtMsg = (EditText) findViewById(R.id.txtMessage);
			String text = Html.toHtml(txtMsg.getText());
			
			//remove excess HTML tags
			text = text.replace("<p dir=ltr>", "").replace("</p>", "");
			text = text.replace("\n","");
			
			//remove underline tags
			text = removeFormatting(text, 'u');
			txtMsg.setText(Html.fromHtml(text));
	    	return true;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);
		
		//Register long click listeners defined above
		Button btnBold = (Button)findViewById(R.id.btnCompBold);
	    btnBold.setOnLongClickListener(lngclkBold);
	    Button btnItalics = (Button)findViewById(R.id.btnCompItalics);
	    btnItalics.setOnLongClickListener(lngclkItalics);
	    Button btnUnderline = (Button)findViewById(R.id.btnCompUnderline);
	    btnUnderline.setOnLongClickListener(lngclkUnderline);
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();  // Always call the superclass
	    
	    // Stop method tracing that the activity started during onCreate()
	    android.os.Debug.stopMethodTracing();
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_compose, menu);
		return true;
	}
	
	/**
	 * Helper fn: Removes formatting from text
	 * @param text			text to be deformatted
	 * @param formatType	type of formatting to be removed
	 * @return				deformatted text
	 */
	public static String removeFormatting(String text, char formatType){
		if (formatType == 'b')
			text = text.replaceAll("<b>", "").replaceAll("</b>", "");
		else if (formatType == 'i')
			text = text.replaceAll("<i>", "").replaceAll("</i>", "");
		else if (formatType == 'u')
			text = text.replaceAll("<u>", "").replaceAll("</u>", "");
		
		return text;
	}
	
	/**
	 * Helper fn: Converts shortcode to HTML tags.
	 * Takes string with formatting symbols and returns same
	 * string with parsable HTML formatting.
	 * @param	msg	string containing raw formatting symbols
	 * @return      string containing HTML formatting
	 */
	public static String decodeMessage(String msg){
		while (msg.matches("(.*)(\\*)(.*)(\\*)(.*)")){
			msg = msg.replaceFirst("\\*","<b>");
			msg = msg.replaceFirst("\\*","</b>");
		}
		while (msg.matches("(.*)(\\`)(.*)(\\`)(.*)")){
			msg = msg.replaceFirst("\\`","<i>");
			msg = msg.replaceFirst("\\`","</i>");
		}
		while (msg.matches("(.*)(\\_)(.*)(\\_)(.*)")){
			msg = msg.replaceFirst("\\_","<u>");
			msg = msg.replaceFirst("\\_","</u>");
		}
				
		return msg;
	}
	
	/**
	 * Helper fn: Convert HTML tags to shortcode.
	 * Takes string with HTML formatting tags and returns
	 * same string with shortcode formatting tags. This is
	 * done to reduce size of message before sending.
	 * @param	msg	string containing HTML tags
	 * @return      string containing shortcode tags
	 */
	public static String encodeMessage(String msg){
		msg = msg.replace("<b>","*").replace("</b>","*");
		msg = msg.replace("<i>","`").replace("</i>","`");
		msg = msg.replace("<u>","_").replace("</u>","_");

		return msg;
	}
	
	/**
	 * Helper fn: Insert character into text, ignoring HTML tags.
	 * @param	text	text to be inserted into
	 * @param	c		character to be inserted
	 * @param	pos		index to insert character at	
	 * @return			string with character inserted
	 */
	public static String insertIntoFormattedText(String text, char c, int pos){
		int i = 0;	//tracks actual position in string
		int j = 0;	//tracks position in string not including tags
		boolean blnInTag = false;
		StringBuffer strbufText = new StringBuffer(text);
		
		while (i <= text.length()){

			//if position has been reached by j, insert char and break
			if (j == pos && !blnInTag){
				strbufText.insert(i, c);
				break;
			}			
			if (text.charAt(i) == '<') {
				blnInTag = true;	//set flag to indicate start of tag
				i++;	//increment i but not j because we're in a tag
				continue;	//skip to end of loop
			} else if (text.charAt(i) == '>'){
				blnInTag = false;	//set flag to indicate end of tag
				i++;	//increment i but not j	because we're in a tag
				continue;	//skip to end of loop
			}
					
			i++;	//increment i			
			if (!blnInTag)	j++;	//increment j if not in tag
		
		}

		return strbufText.toString();
	}
	
	/**
	 * Gets info from view and sends SMS with send confirmation.
	 * Gets message and phone nunmber from view, creates a receiver
	 * to confirm send, then sends SMS. If SMS succeeds, clears
	 * text fields in view.
	 * @param  view view from compose activity. contains num and msg.
	 * @return      void
	 */
	public void sendMessage(View view){
		String SENT = "SMS_SENT";
        
		//get text fields from view, then strings from text fields
		EditText txtDest = (EditText) findViewById(R.id.txtPhone);
		EditText txtMsg = (EditText) findViewById(R.id.txtMessage);
		String destination = txtDest.getText().toString();
		String msg = Html.toHtml(txtMsg.getText());
		
		//remove excess HTML tags from message
		msg = msg.replace("<p dir=ltr>", "").replace("</p>", "");
		msg = msg.replace("\n","");
		
		//clear text fields
		txtDest.setText("");
		txtMsg.setText("");
		
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
		
		//initialize smsmanager, send SMS, and add to database
		SmsManager smsMgr = SmsManager.getDefault();
		try{
			smsMgr.sendTextMessage(destination,null,encodeMessage(msg),piSent,null);
			
			//Create contentvalues to insert into contentresolver
			ContentValues values = new ContentValues();
		    values.put("address", destination);
		    values.put("body", msg);
		    
			//Find conversation, then get thread_id if there is one. If not, one will be generated automatically.
        	Uri uriConvo = Uri.parse("content://sms/conversations");
            Cursor conversationCursor = getContentResolver().query(uriConvo, null, "address='"+destination+"'", null, "date desc");
            if (conversationCursor.moveToFirst()){
            	String thread_id = conversationCursor.getString(conversationCursor.getColumnIndex("thread_id"));
    		    values.put("thread_id", thread_id);
            }
            
            //insert row into table
		    getContentResolver().insert(Uri.parse("content://sms/sent"), values); 
		} catch (IllegalArgumentException e){
			Toast.makeText(getBaseContext(), "Please enter a number and message", Toast.LENGTH_SHORT).show();
		}	// close catch	
		
	} //close sendMessage()
	
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
		EditText txtMsg = (EditText) findViewById(R.id.txtMessage);
		String text = Html.toHtml(txtMsg.getText());
		
		//remove excess HTML tags
		text = text.replace("<p dir=ltr>", "").replace("</p>", "");
		text = text.replace("\n","");
				
		//insert * before and after selected text
		int selStart = txtMsg.getSelectionStart();
		int selEnd = txtMsg.getSelectionEnd();
		text = insertIntoFormattedText(text, c, selStart);
		text = insertIntoFormattedText(text, c, selEnd + 1);
				
		//convert to HTML and update textbox with formatted text
		txtMsg.setText(Html.fromHtml(decodeMessage(text)));
		
		//rehighlight right text
		txtMsg.setSelection(selStart, selEnd);	
	}
	
} //close Compose class
