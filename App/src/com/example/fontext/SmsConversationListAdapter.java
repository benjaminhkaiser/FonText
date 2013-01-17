package com.example.fontext;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class ViewHolder{
	TextView text;
	ImageView icon;
}


/**
 * Custom adapter to convert list of SMS conversation into a listview.
 * The SMS messages are stored as SmsConversation objects (another custom class).
 * @author Ben Kaiser
 */
@SuppressWarnings("rawtypes")
public class SmsConversationListAdapter extends ArrayAdapter{

      private LayoutInflater inflater;
      private Context context;

      @SuppressWarnings("unchecked")
      public SmsConversationListAdapter(Context ctx, int resourceId, List objects) {
    	  super(ctx, resourceId, objects);
          inflater = LayoutInflater.from( ctx );
          context=ctx;
      }

      @Override
      public View getView (int position, View convertView, ViewGroup parent) {
    	  //Extract the conversation to display 
    	  SmsConversation convo = (SmsConversation) getItem(position);
    	  
    	  //Initialize viewholder
		  ViewHolder holder;

    	  //Apply layout (with view recycling)
    	  if (convertView == null){
    		  convertView = inflater.inflate(R.layout.smsconversationlistview_item_row, null);
    		  holder = new ViewHolder();
    		  
    		  holder.text = (TextView) convertView.findViewById(R.id.lblSnippet);		  
    		  holder.icon = (ImageView) convertView.findViewById(R.id.imgContactPhoto);
    		  
    		  convertView.setTag(holder);
    	  } else {
    		  holder = (ViewHolder) convertView.getTag();
    	  }
	  
    	  //Set message to text view
    	  holder.text.setText(convo.getDisplayMsg());
	
    	  //Set contact icon to image view
    	  long contactId = SmsMessageListAdapter.fetchContactId(convo.getContactNumber(), context);
    	  Uri uriContact = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactId));
    	  InputStream photo_stream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),uriContact);            
    	  BufferedInputStream buf = new BufferedInputStream(photo_stream);
    	  Bitmap my_btmp = BitmapFactory.decodeStream(buf);
    	  holder.icon.setImageBitmap(my_btmp);
    	  
    	  return convertView;
      }
}