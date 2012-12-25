package com.example.fontext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

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

}
