package com.example.fontext;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * A custom list view class that overrides onSizeChanged
 * The list is autoscrolled to the bottom when size is changed
 * @author Ben Kaiser
 */
public class CustomListView extends ListView{
	public CustomListView (Context context) {
	    super(context);
	}

	public CustomListView (Context context, AttributeSet attrs) {
	    super(context, attrs);
	}

	public CustomListView (Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
	    super.onSizeChanged(xNew, yNew, xOld, yOld);

	    post(new Runnable() {
	        public void run() {
	            setSelection(getCount());
	        }
	    });
	}
}
