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

package com.siyuan.muzei.ample.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.siyuan.muzei.ample.ArtSource;
import com.siyuan.muzei.ample.R;
import com.siyuan.muzei.ample.data.DataService;
import com.siyuan.muzei.ample.data.Item;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;


/**
 * Preference utils
 */
public class PreferenceUtils {

	public static boolean getWifiOnly( final Context context ){
		final boolean defaultValue = context.getResources().getBoolean(R.bool.prefs_wifionly_default);
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		return settings.getBoolean(context.getString(R.string.prefs_wifionly), defaultValue);
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

		args = concatFilterArgs( args, getColorFilter(context) );
		args = concatFilterArgs( args, getLocalFilter(context) );
		args = concatFilterArgs( args, getSeriesFilter(context) );
		args = concatFilterArgs( args, getUserFilter(context) );
		args = concatFilterArgs( args, getKeywordFilter(context) );

		return args;
	}

	private static String concatFilterArgs( String args, final String newArgs  ){
		if( newArgs.length() > 0 ){
			args += (args.length() <= 0? "" : "&") + newArgs;
		}
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
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		return settings.getString(context.getString( R.string.prefs_filter_series_entry),
				context.getString( R.string.settings_filter_none_use ) );
	}

	public static String getUserFilter(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );

		return settings.getString(context.getString( R.string.prefs_filter_user),
				context.getString( R.string.prefs_filter_user_default) );
	}

	public static String getUserFilterEntry(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );

		return settings.getString(context.getString( R.string.prefs_filter_user_entry),
				context.getString( R.string.settings_filter_none_use ) );
	}

	public static String getKeywordFilter(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );

		String args = settings.getString(context.getString( R.string.prefs_filter_keyword),
				context.getString( R.string.prefs_filter_keyword_default) );
		if( args.length() > 0 ){
			try{
				args = java.net.URLEncoder.encode( args, "UTF-8" );
			}catch ( UnsupportedEncodingException e ){
			}
			return DataService.FILTER_ARG_NAME_KEYWORD + "=" + args;
		}
		return args;
	}

	public static String getKeywordFilterEntry(final Context context) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );

		return settings.getString(context.getString( R.string.prefs_filter_keyword),
				context.getString( R.string.prefs_filter_keyword_default) );
	}

	public static String getLastQueryThumbnailsArgs(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		return settings.getString( context.getString( R.string.prefs_query_thumbnails_args), "" );
	}

	public static void setLastQueryThumbnailsArgs(final Context context, String value){
		final SharedPreferences.Editor settings = PreferenceManager.getDefaultSharedPreferences( context ).edit();
		settings.putString( context.getString( R.string.prefs_query_thumbnails_args), value );
		settings.apply();
	}

	public static int getLastQueryThumbnailsMaxPageCount(final Context context) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		return settings.getInt( context.getString( R.string.prefs_query_thumbnails_max_page), Integer.MAX_VALUE );
	}

	public static void setLastQueryThumbnailsMaxPageCount(final Context context, int value) {
		final SharedPreferences.Editor settings = PreferenceManager.getDefaultSharedPreferences( context ).edit();
		settings.putInt( context.getString( R.string.prefs_query_thumbnails_max_page), value );
		settings.apply();
	}

	public static DataService.ImageData getCurrentArtworkData(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		final String data = settings.getString( context.getString( R.string.prefs_current_artwork), "" );
		try{
			DataService.ImageData result = (DataService.ImageData)ObjectSerializer.deserialize( data );
			if( result == null )
				throw new IOException( "Not a valid data" );
			return result;
		}catch ( Exception e ){
			return null;
		}
	}

	public static void setCurrentArtworkData(final Context context, DataService.ImageData value){
		if( value == null )
			throw new IllegalArgumentException( "value = null" );
		String data = "";
		try{
			data = ObjectSerializer.serialize( value );
		}catch ( Exception e ){
			// Do nothing
		}
		final SharedPreferences.Editor settings = PreferenceManager.getDefaultSharedPreferences( context ).edit();
		settings.putString( context.getString( R.string.prefs_current_artwork), data );
		settings.apply();
	}

	public static ArrayList<Item> getRecentUsers(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		final String data = settings.getString( context.getString( R.string.prefs_users_recently), "" );
		try{
			ArrayList<Item> result = (ArrayList<Item>)ObjectSerializer.deserialize( data );
			if( result == null )
				throw new IOException( "Not a valid data" );
			return result;
		}catch ( Exception e ){
			return new ArrayList<Item>();
		}
	}

	public static void setRecentCosplayers(final Context context, ArrayList<Item> value){
		if( value == null )
			throw new IllegalArgumentException( "value = null" );
		String data = "";
		try{
			data = ObjectSerializer.serialize( value );
		}catch ( Exception e ){
			// Do nothing
		}
		final SharedPreferences.Editor settings = PreferenceManager.getDefaultSharedPreferences( context ).edit();
		settings.putString( context.getString( R.string.prefs_users_recently), data );
		settings.apply();
	}

	public static void updateRecentUsers(final Context context, String entry, String value){
		ArrayList<Item> items = getRecentUsers(context);
		int index = -1;
		for( int ii = 0; ii < items.size(); ++ii ) {
			Item item = items.get( ii );
			if (item.entry.equalsIgnoreCase( entry ) && item.value.equalsIgnoreCase(value)){
				index = ii;
				break;
			}
		}
		if( index < 0 ){
			// Add
			items.add( 0, new Item( entry, value ) );
		}else{
			// Reorder
			items.add( 0, items.get( index ) );
			items.remove( index + 1 );
		}

		while( items.size() > 10 ){
			items.remove( 10 );
		}
		setRecentCosplayers(context, items);
	}

	public static Date getNextRefreshTime( Context context ){
		final String PREF_SCHEDULED_UPDATE_TIME_MILLIS = "scheduled_update_time_millis";
		final long scheduled_refresh = ArtSource.getSharedPreferences(context).getLong(
				PREF_SCHEDULED_UPDATE_TIME_MILLIS, System.currentTimeMillis() );
		return new Date( scheduled_refresh );
	}
}
