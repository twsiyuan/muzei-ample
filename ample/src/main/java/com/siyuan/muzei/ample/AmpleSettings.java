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

package com.siyuan.muzei.ample;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * AmpleSettings, wrapper for preferences
 */
public class AmpleSettings {

	public static boolean getWifiOnly( final Context context ){
		final boolean defaultValue = context.getResources().getBoolean( R.bool.prefs_wifionly_default );
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		return settings.getBoolean( context.getString( R.string.prefs_wifionly ), defaultValue );
	}

	public static long getRefreshInterval(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		final int defaultValue = context.getResources().getInteger( R.integer.prefs_refresh_interval_default );
		final String stringValue = settings.getString( context.getString( R.string.prefs_refresh_interval ), "");

		try {
			if( stringValue.length() == 0 )
				return defaultValue;
			return Long.parseLong( stringValue );
		}catch ( NumberFormatException e ){
			return defaultValue;
		}
	}

	public static int getTopCosplays(final Context context){
		return 150;
	}

	public static int getSourceFrom(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		final int defaultValue = context.getResources().getInteger(R.integer.prefs_source_from_default);
		final String stringValue = settings.getString(context.getString( R.string.prefs_source_from ), "" );

		try{
			if( stringValue.length() == 0 )
				return defaultValue;
			return Integer.parseInt( stringValue );
		}catch ( NumberFormatException e ){
			return defaultValue;
		}
	}

	public static String getFilterArgs(final Context context){
		String args = "";

		if( getColorFilter( context ).length() > 0 )
			args += (args.length() == 0? "" : "&") + getColorFilter( context );

		if( getLocalFilter( context ).length() > 0 )
			args += (args.length() == 0? "" : "&") + getLocalFilter( context );

		if( getSeriesFilter( context ).length() > 0 )
			args += (args.length() == 0? "" : "&") + getSeriesFilter( context );

		return args;
	}

	public static String getColorFilter(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );

		return settings.getString(context.getString( R.string.prefs_filter_color ),
				context.getString( R.string.prefs_filter_color_default ) );
	}

	public static String getColorFilterEntry(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );

		return settings.getString(context.getString( R.string.prefs_filter_color_entry),
				context.getString( R.string.settings_filter_none_use ) );
	}

	public static String getLocalFilter(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );

		return settings.getString(context.getString( R.string.prefs_filter_locale),
				context.getString( R.string.prefs_filter_locale_default) );
	}

	public static String getLocaleFilterEntry(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );

		return settings.getString(context.getString( R.string.prefs_filter_locale_entry),
				context.getString( R.string.settings_filter_none_use ) );
	}

	public static String getSeriesFilter(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );

		return settings.getString(context.getString( R.string.prefs_filter_series ),
				context.getString( R.string.prefs_filter_series_default ) );
	}

	public static String getSeriesFilterEntry(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );

		return settings.getString(context.getString( R.string.prefs_filter_series_entry),
				context.getString( R.string.settings_filter_none_use ) );
	}
}
