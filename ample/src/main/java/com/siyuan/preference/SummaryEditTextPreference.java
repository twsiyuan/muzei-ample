package com.siyuan.preference;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class SummaryEditTextPreference extends EditTextPreference {

	public SummaryEditTextPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setText(String value) {
		super.setText(value);
		setSummary(getSummary());
	}

	@Override
	public void setSummary(CharSequence summary) {
		super.setSummary(getSummary());
	}

	@Override
	public CharSequence getSummary(){
		return super.getText();
	}

}
