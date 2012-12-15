package com.example.fontext;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

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
	
	public void sendMessage(View view){
		SmsManager smsMgr = SmsManager.getDefault();
		String destination = ((EditText) findViewById(R.id.txtPhone)).getText().toString();
		String msg = ((EditText) findViewById(R.id.txtMessage)).getText().toString();;
		smsMgr.sendTextMessage(destination,null,msg,null,null);
	}

}
