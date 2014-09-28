/*
 * Copyright 2014 Siyuan Wang (easy0519@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siyuan.muzei.ample.settings.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.siyuan.preference.SummaryListPreference;

/**
 * ListPreference
 */
public class EntryListPreference extends SummaryListPreference {

	public EntryListPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		for (int i=0;i<attrs.getAttributeCount();i++) {
			final String attr = attrs.getAttributeName(i);
			String val  = attrs.getAttributeValue(i);

			if (attr.equalsIgnoreCase( "entryKey" )) {
				final int restId = context.getResources().getIdentifier( val, "string", context.getPackageName() );
				if( restId != 0 ) val = context.getString( restId );
				mEntryKey = val;
			}
		}
	}

	@Override
	protected void onDialogClosed( boolean positiveResult ){
		super.onDialogClosed( positiveResult );

		if( positiveResult ){
			String entry = getEntry().toString();
			SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
			prefEditor.putString(mEntryKey, entry);
			prefEditor.apply();
		}
	}


	String mEntryKey;




}
