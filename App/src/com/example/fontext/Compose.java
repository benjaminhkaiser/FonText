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
	 * If text in txtMessage field is highlighted, place bold tags
	 * around text. Otherwise, display a toast telling the user to
	 * highlight text to be bolded.
	 * @param  view view from compose activity. contains num and msg. 
	 * @return      void
	 */
	public void boldText(View view){
		//Get text box and string from text box
		EditText txtMsg = (EditText) findViewById(R.id.txtMessage);
		String msg = txtMsg.getText().toString();
		
		if (txtMsg.getSelectionStart() == -1){	//if text has not been selected
			Toast.makeText(getBaseContext(), "Please select text to bold", Toast.LENGTH_SHORT).show();
		} else {
			//insert * before and after selected text
			int selStart = txtMsg.getSelectionStart();
			int selEnd = txtMsg.getSelectionEnd();
			StringBuffer strbufMsg = new StringBuffer(msg);
			strbufMsg.insert(selStart, "*");
			strbufMsg.insert(selEnd + 1, "*");
			txtMsg.setText(Html.fromHtml(decodeMessage(strbufMsg.toString())));
			
			txtMsg.setSelection(selStart, selEnd);
		}		
	}
	
	/**
	 * If text in txtMessage field is highlighted, place italics
	 * tags around text. Otherwise, display a toast telling the
	 * user to highlight text to be italicized.
	 * @param  view view from compose activity. contains num and msg. 
	 * @return      void
	 */
	public void italicizeText(View view){
		//Get text box and string from text box
		EditText txtMsg = (EditText) findViewById(R.id.txtMessage);
		String msg = txtMsg.getText().toString();
		
		if (txtMsg.getSelectionStart() == -1){	//if text has not been selected
			Toast.makeText(getBaseContext(), "Please select text to italicize", Toast.LENGTH_SHORT).show();
		} else {
			//insert * before and after selected text
			int selStart = txtMsg.getSelectionStart();
			int selEnd = txtMsg.getSelectionEnd();
			StringBuffer strbufMsg = new StringBuffer(msg);
			strbufMsg.insert(selStart, "`");
			strbufMsg.insert(selEnd + 1, "`");
			txtMsg.setText(Html.fromHtml(decodeMessage(strbufMsg.toString())));
			
			txtMsg.setSelection(selStart, selEnd);
		}
	}
	
	/**
	 * If text in txtMessage field is highlighted, place
	 * underline tags around text. Otherwise, display a
	 * toast telling the user to highlight text to be 
	 * underlined.
	 * @param  view view from compose activity. contains num and msg. 
	 * @return      void
	 */
	public void underlineText(View view){
		//Get text box and string from text box
		EditText txtMsg = (EditText) findViewById(R.id.txtMessage);
		String msg = txtMsg.getText().toString();
		
		if (txtMsg.getSelectionStart() == -1){	//if text has not been selected
			Toast.makeText(getBaseContext(), "Please select text to underline", Toast.LENGTH_SHORT).show();
		} else {
			//insert _ before and after selected text
			int selStart = txtMsg.getSelectionStart();
			int selEnd = txtMsg.getSelectionEnd();
			StringBuffer strbufMsg = new StringBuffer(msg);
			strbufMsg.insert(selStart, "_");
			strbufMsg.insert(selEnd + 1, "_");
			txtMsg.setText(Html.fromHtml(decodeMessage(strbufMsg.toString())));
			txtMsg.setSelection(selStart, selEnd);
		}
	}
} //close Compose class
