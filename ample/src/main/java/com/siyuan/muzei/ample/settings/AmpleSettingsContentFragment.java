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

package com.siyuan.muzei.ample.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.siyuan.muzei.ample.R;
import com.siyuan.muzei.ample.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;


public class AmpleSettingsContentFragment extends PreferenceFragment{

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource( R.xml.settings );

		final SimpleDateFormat sdf = new SimpleDateFormat( getString( R.string.settings_refresh_summary ) );
		final Preference nextRefresh = findPreference( getString( R.string.prefs_refresh_next ) );
		final Date nextRefreshDate = Utils.getNextRefreshTime(this.getActivity().getApplicationContext());
		nextRefresh.setSummary( sdf.format( nextRefreshDate ) );
	}

}
