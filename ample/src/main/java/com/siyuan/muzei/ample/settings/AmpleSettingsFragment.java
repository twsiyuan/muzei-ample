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

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;

import com.siyuan.muzei.ample.AmpleService;
import com.siyuan.muzei.ample.AmpleSettings;
import com.siyuan.muzei.ample.R;
import com.siyuan.muzei.ample.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;


public class AmpleSettingsFragment extends PreferenceFragment{

	static final int FITLERS_REQUEST_CODE = 998521;

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource( R.xml.settings );

		setupRefreshTime();
		setupFilters();
		setupFiltersEnabled();
		setupFiltersSummary();
		setupFiltersClear();
	}

	public void refresh(){
		setupFiltersSummary();
	}

	private void setupFilters(){
		findPreference( getString( R.string.prefs_filters ) ).setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						startActivityForResult(preference.getIntent(), FITLERS_REQUEST_CODE);
						return true;
					}
				}
		);
	}

	private void setupFiltersEnabled(){
		final ListPreference source = (ListPreference)findPreference( getString( R.string.prefs_source_from ) );
		final Preference filter = findPreference( getString( R.string.prefs_filters ));

		Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object o) {
				int value = Integer.parseInt(( String)o );
				boolean enabled = value == AmpleService.THUMBNAIL_SOURCE_CONTENT;
				filter.setEnabled( enabled );
				return true;
			}
		};

		source.setOnPreferenceChangeListener( listener );
		listener.onPreferenceChange( source, source.getValue() );

	}

	private void setupFiltersClear(){

	}

	private void setupFiltersSummary(){
		final Context context = getActivity();
		String smmary = "";

		if( AmpleSettings.getColorFilter( context ).length() > 0 ){
			smmary += ( smmary.length() == 0? "" : "\n" ) + getString( R.string.settings_filter_color ) + ": " + AmpleSettings.getColorFilterEntry(context);
		}

		if( AmpleSettings.getLocalFilter(context).length() > 0 ){
			smmary += ( smmary.length() == 0? "" : "\n" ) + getString( R.string.settings_filter_locale ) + ": " + AmpleSettings.getLocaleFilterEntry(context);
		}

		if( AmpleSettings.getSeriesFilter(context).length() > 0 ){
			smmary += ( smmary.length() == 0? "" : "\n" ) + getString( R.string.settings_filter_series ) + ": " + AmpleSettings.getSeriesFilterEntry(context);
		}

		if( smmary.length() <= 0 )
			smmary += getString( R.string.settings_filter_none_use );

		findPreference( getString( R.string.prefs_filters ) ).setSummary( smmary );

	}

	private void setupRefreshTime(){
		final SimpleDateFormat sdf = new SimpleDateFormat( getString( R.string.settings_refresh_summary ) );
		final Preference nextRefresh = findPreference( getString( R.string.prefs_refresh_next ) );
		final Date nextRefreshDate = Utils.getNextRefreshTime(this.getActivity().getApplicationContext());
		nextRefresh.setSummary( sdf.format( nextRefreshDate ) );
	}

}
