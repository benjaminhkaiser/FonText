package com.example.fontext;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Custom adapter to convert list of SMS messages into a listview.
 * The SMS messages are stored as Sms objects (another custom class).
 * @author Ben
 */
@SuppressWarnings("rawtypes")
public class SmsListAdapter extends ArrayAdapter{

      private LayoutInflater inflater;
      private Context context;

      @SuppressWarnings("unchecked")
      public SmsListAdapter(Context ctx, int resourceId, List objects) {
    	  super(ctx, resourceId, objects);
          inflater = LayoutInflater.from( ctx );
          context=ctx;
      }

      @Override
      public View getView (int position, View convertView, ViewGroup parent) {
    	  
    	  //Extract the message to display 
    	  Sms msg = (Sms) getItem(position);
    	  if (!msg.getSent()){
       		  //create a new view of the layout and inflate it in the row
           	  convertView = inflater.inflate(R.layout.smslistview_recd_item_row, null );
    	  } else {
       		  //create a new view of the layout and inflate it in the row
           	  convertView = inflater.inflate(R.layout.smslistview_sent_item_row, null );
    	  }   	  
	
    	  //Take the TextView from layout and set the message
    	  TextView lblMsg = (TextView) convertView.findViewById(R.id.lblMsg);
    	  lblMsg.setText(msg.getBody());
	
    	  //Take the ImageView from layout and set the contact image
    	  ImageView imgContactPhoto = (ImageView) convertView.findViewById(R.id.imgContactPhoto);
    	  long contactId = fetchContactId(msg.getSenderNum());
    	  Uri uriContact = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactId));
    	  InputStream photo_stream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),uriContact);            
    	  BufferedInputStream buf = new BufferedInputStream(photo_stream);
    	  Bitmap my_btmp = BitmapFactory.decodeStream(buf);
    	  imgContactPhoto.setImageBitmap(my_btmp);
    	  return convertView;
      }
      
      
      /**
       * Helper fn: given phone number, return contactId
       * @param phoneNumber	number to look up
       * @return			contactId of contact
       */
      public long fetchContactId(String phoneNumber) {
    	  Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
    	  Uri.encode(phoneNumber));
    	  Cursor cursor = context.getContentResolver().query(uri,
    	      new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup._ID },
    	      null, null, null);

    	  long contactId = 0;

    	  if (cursor.moveToFirst()) {
    	      do {
    	      contactId = cursor.getLong(cursor
    	          .getColumnIndex(PhoneLookup._ID));
    	      } while (cursor.moveToNext());
    	  }
    	  
    	  return contactId;
      }
      
      /**
       * Helper fn: given contactId, return Uri to contact photo
       * @param contactId	id of contact to look up
       * @return			Uri pointing to contact photo
       */
      public Uri getPhotoUri(long contactId) {
    	  ContentResolver contentResolver = context.getContentResolver();
    	  try {
    		  Cursor cursor = contentResolver
    				  .query(ContactsContract.Data.CONTENT_URI,
    	              null,
    	              ContactsContract.Data.CONTACT_ID
    	              	+ "="
    	                + contactId
    	                + " AND "
   	                    + ContactsContract.Data.MIMETYPE
  	                    + "='"
  	                    + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
  	                    + "'", null, null);

    		  if (cursor != null) {
    			  if (!cursor.moveToFirst()) {
    				  return null; // no photo
    			  }
    	      } else {
    	    	  return null; // error in cursor process
    	      }

    	  } catch (Exception e) {
    	        e.printStackTrace();
    	        return null;
    	  }
    	  
    	  Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
    	  return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
	  }
}
