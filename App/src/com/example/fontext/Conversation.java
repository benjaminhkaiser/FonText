package com.example.fontext;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Conversation extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_conversation, menu);
		return true;
	}

}
