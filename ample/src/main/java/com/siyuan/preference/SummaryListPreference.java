package com.siyuan.preference;

import android.preference.ListPreference;
import android.content.Context;
import android.util.AttributeSet;

public class SummaryListPreference extends ListPreference {
	public SummaryListPreference(final Context context) {
		this(context, null);
	}

	public SummaryListPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		setSummary(value);
	}

	@Override
	public void setSummary(CharSequence summary) {
		super.setSummary(getEntry());
	}

	@Override
	public java.lang.CharSequence getEntry(){
		try{
			return super.getEntry();
		}catch ( NullPointerException e ){
			return "";
		}
	}

}
