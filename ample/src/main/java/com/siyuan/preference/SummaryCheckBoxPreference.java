package com.siyuan.preference;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class SummaryCheckBoxPreference extends CheckBoxPreference {
	public SummaryCheckBoxPreference(final Context context) {
		this(context, null);
	}

	public SummaryCheckBoxPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		for (int i=0;i<attrs.getAttributeCount();i++) {
			final String attr = attrs.getAttributeName(i);
			String val  = attrs.getAttributeValue(i);

			if (attr.equalsIgnoreCase( "trueSummary" )) {
				final int restId = context.getResources().getIdentifier( val, "string", context.getPackageName() );
				if( restId != 0 ) val = context.getString( restId );
				setTrueSummary( val );
			}else if( attr.equalsIgnoreCase( "falseSummary" ) ){
				final int restId = context.getResources().getIdentifier( val, "string", context.getPackageName() );
				if( restId != 0 ) val = context.getString( restId );
				setFalseSummary( val );
			}
		}
	}

	@Override
	public void setChecked(boolean value) {
		super.setChecked(value);
		setSummary( null );
	}

	@Override
	public void setSummary(CharSequence summary) {
		super.setSummary( isChecked()? getTrueSummary() : getFalseSummary() );
	}

	public String getTrueSummary(){
		return mTrueSummary;
	}

	public void setTrueSummary(String value){
		mTrueSummary = value;
	}

	public String getFalseSummary(){
		return mFalseSummary;
	}

	public void setFalseSummary(String value){
		mFalseSummary = value;
	}

	String mTrueSummary;
	String mFalseSummary;


}
