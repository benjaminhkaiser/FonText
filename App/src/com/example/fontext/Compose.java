package com.example.fontext;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Compose extends Activity {
	
	public String globalMsg = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_compose, menu);
		return true;
	}
	
	/**
	 * Helper fn: Converts shortcode to HTML tags.
	 * Takes string with formatting symbols and returns same
	 * string with parsable HTML formatting.
	 * @param	msg	string containing raw formatting symbols
	 * @return      string containing HTML formatting
	 */
	public String decodeMessage(String msg){
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
	public String encodeMessage(String msg){
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
	public String insertIntoFormattedText(String text, char c, int pos){
		int i = 0;	//tracks actual position in string
		int j = 0;	//tracks position in string not including tags
		boolean blnInTag = false;
		StringBuffer strbufText = new StringBuffer(text);
		
		while (i<text.length()-1){
			if (text.charAt(i) == '<') {
				blnInTag = true;	//set flag to indicate start of tag
				i++;	//increment i but not j because we're in a tag
				continue;	//skip to end of loop
			} else if (text.charAt(i) == '>'){
				blnInTag = false;	//set flag to indicate end of tag
				i++;	//increment i but not j	because we're in a tag
				continue;	//skip to end of loop
			}
			
			//if position has been reached by j, insert char and break
			if (j == pos && !blnInTag){
				strbufText.insert(i, c);
				break;
			}
			
			i++;	//increment i			
			if (!blnInTag)	j++;	//increment j if not in tag
		
		}

		return strbufText.toString();
	}
	
	/**
	 * Gets info from view, creates receivers to confirm send and
	 * delivery, then sends SMS. If SMS succeeds, clear text fields
	 * in view.
	 * @param  view view from compose activity. contains num and msg.
	 * @return      void
	 */
	public void sendMessage(View view){
		String SENT = "SMS_SENT";
        String DEL = "SMS_DELIVERED";
        
		//get text fields from view, then strings from text fields
		EditText txtDest = (EditText) findViewById(R.id.txtPhone);
		EditText txtMsg = (EditText) findViewById(R.id.txtMessage);
		String destination = txtDest.getText().toString();
		String msg = txtMsg.getText().toString();
		
		//clear text fields
		txtDest.setText("");
		txtMsg.setText("");
		
		//initialize pendingintents
		PendingIntent piSent = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		PendingIntent piDelivered = PendingIntent.getBroadcast(this, 0, new Intent(DEL), 0);
		
		//set up intent receiver for sending
		registerReceiver(new BroadcastReceiver(){
			//override the onReceive function to display a toast containing error message
            @Override
            public void onReceive(Context arg0, Intent arg1) {	//args are unused
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "Text sent", Toast.LENGTH_SHORT).show();
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
		
		//set up intent receiver for delivery
        registerReceiver(new BroadcastReceiver(){
			//override the onReceive function to display a toast containing error message
            @Override
            public void onReceive(Context arg0, Intent arg1) {	//args are unneeded
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered successfully", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;                        
                }	//close switch
            }	//close onReceive()
        }, new IntentFilter(DEL));   
        //on above line, } closes BroadcastReceiver constructor, ) closes registerReciever call
		
		//initialize smsmanager and send SMS
		SmsManager smsMgr = SmsManager.getDefault();
		try{
			smsMgr.sendTextMessage(destination,null,encodeMessage(msg),piSent,piDelivered);
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
		globalMsg = Html.toHtml(txtMsg.getText());
		
		//remove excess HTML tags
		globalMsg = globalMsg.replace("<p dir=ltr>", "").replace("</p>", "");
		globalMsg = globalMsg.replace("\n","");
				
		//insert * before and after selected text
		int selStart = txtMsg.getSelectionStart();
		int selEnd = txtMsg.getSelectionEnd();
		globalMsg = insertIntoFormattedText(globalMsg, c, selStart);
		globalMsg = insertIntoFormattedText(globalMsg, c, selEnd + 1);
			
		globalMsg = decodeMessage(globalMsg);
		
		//convert to HTML and update textbox with formatted text
		txtMsg.setText(Html.fromHtml(decodeMessage(globalMsg)));
		
		//rehighlight right text
		txtMsg.setSelection(selStart, selEnd);	
	}
	
} //close Compose class
