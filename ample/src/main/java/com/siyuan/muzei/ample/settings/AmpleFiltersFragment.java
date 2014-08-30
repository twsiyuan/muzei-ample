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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.siyuan.muzei.ample.AmpleService;
import com.siyuan.muzei.ample.AmpleSettings;
import com.siyuan.muzei.ample.BuildConfig;
import com.siyuan.muzei.ample.R;
import com.siyuan.muzei.ample.Utils;
import com.siyuan.muzei.ample.settings.filter.*;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AmpleFiltersFragment extends PreferenceFragment{

	static final String TAG = "AmpleFiltersFragment";

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		new InitAsyncTask( this ).execute( true, false );
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView( inflater, container, savedInstanceState );
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		if( listView != null ) {
			listView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
					ListView listView = (ListView) adapterView;
					if( listView != null ){
						Object obj = listView.getAdapter().getItem(position);

						if (obj != null ) {
							if( obj instanceof  ListPreference ) {
								((ListPreference) obj).setValue("");
								return true;
							}else if( obj instanceof SeriesPreference ){
								((SeriesPreference)obj).setValue("");
								return true;
							}
						}
					}
					return false;
				}
			});
		}
		return view;
	}

	public void refreshFilters(){
		new InitAsyncTask( this ).execute( false, true );
	}

	private class InitAsyncTask extends AsyncTask<Boolean, Void, Void>{

		static final String FILTER_DATA_URL = "http://ample-cosplay.com/";
		final Pattern URL_ARGS_PATTERN = Pattern.compile( "\\?([^&\"]+)" );

		public InitAsyncTask( AmpleFiltersFragment fragment ){
			super();
			mFragment = fragment;
		}

		@Override
		protected Void doInBackground(Boolean... args) {
			final boolean initPreferences = args[ 0 ];
			final boolean forceReload = args[ 1 ];

			final Context context = getActivity();
			if( !Utils.isNetworkAvailable( context ) ){
				showToast( mFragment.getString( R.string.noInternet ), Toast.LENGTH_SHORT );
			}else{
				if( !AmpleFiltersFragment.mFiltersLoaded || forceReload ) {
					setupProgress();
					try {
						final long timeStamp = System.currentTimeMillis();
						final String html = loadHTML();
						final long timeStamp2 = System.currentTimeMillis();

						parseHTML(html);
						AmpleFiltersFragment.mFiltersLoaded = true;

						if (BuildConfig.DEBUG) {
							Log.d(TAG, String.format("loaded settings %d ms, parsed %d ms", timeStamp2 - timeStamp, System.currentTimeMillis() - timeStamp2));
						}
					} catch (IOException e) {
						showToast(mFragment.getString(R.string.unknown_exception_try_again), Toast.LENGTH_SHORT);
					}finally {

					}
				}
			}

			setupPreferences( initPreferences );
			return null;
		}

		private String loadHTML() throws IOException{
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder()
					.url(FILTER_DATA_URL)
					.build();

			Response response = client.newCall(request).execute();
			return response.body().string();
		}

		private void parseHTML( final String html ){
			Document doc = Jsoup.parse(html);
			Elements color = doc.select( "ul#slideColor" );
			Elements locale = doc.select( "ul#slideCountry" );
			Elements series = doc.select( "ul#slideSeries" );

			parseColor( color.select( "li" ) );
			parseLocale( locale.select( "li" ) );
			parseSeries( series.select( "li" ) );
		}

		private void parseColor( Elements items ){
			mColors.clear();

			parseList( mColors, items );
			addNonUseItem( mColors );
		}

		private void parseLocale( Elements items ){
			mLocales.clear();

			parseList( mLocales, items );
			addNonUseItem( mLocales );
		}

		private void parseSeries( Elements items ) {
			mSeries.clear();

			Pattern catPattern = Pattern.compile( "([^()\\s]+)\\s*\\((\\d+)\\)" );
			Pattern hrefPattern = Pattern.compile( "\\?([^&#]+)" );
			Pattern namePattern = Pattern.compile("([^\\(]+)\\((\\d+)\\)(\\s*(.+))?");

			String catName = "";
			for(Element item : items ){
				String className = item.className();
				if( className.contains("catname") ){
					catName = item.text();
					Matcher catMatcher = catPattern.matcher( catName );
					if( catMatcher.find() ){
						catName = catMatcher.group( 1 );
					}
				}else if( className.contains( "tags" ) ){
					Elements catItems = item.select( "ul > li > a" );
					Category cat = new Category( catName );
					for( Element catItem : catItems ){
						String href = catItem.attr( "href" );
						Matcher hrefMatcher = hrefPattern.matcher( href );

						if( hrefMatcher.find() ){
							String entry = catItem.text();
							String value = hrefMatcher.group( 1 );

							if( !cat.items.containsValue( value ) ) {
								Matcher entryMatcher = namePattern.matcher(entry);
								if (entryMatcher.find()) {
									String jpName = entryMatcher.group(1);
									String enName = entryMatcher.group(4);
									entry = jpName;
									if( enName != null && enName.length() > 0 )
										entry = enName + "\r\n" + entry;
								}
								cat.items.add(new Item(entry, value));
							}
						}
					}
					Collections.sort( cat.items );
					mSeries.add( cat );
				}
			}
		}

		private void parseList( ItemList list, Elements items ){

			if( items == null )
				return;

			for (Element item : items){
				String value = "";
				String link = item.select( "a" ).attr( "href" );
				Matcher linkArgs = URL_ARGS_PATTERN.matcher( link );
				if( linkArgs.find() )
					value = linkArgs.group( 1 );
				list.add( new Item( item.text(), value ) );
			}

			Collections.sort(list);
		}

		private void addNonUseItem( ItemList list ){
			list.add( 0, new Item( getString(R.string.settings_filter_none_use) ) );
		}

		private void setupPreferences( Boolean addPreferences ){
			if( addPreferences )
				mFragment.addPreferencesFromResource( R.xml.filters );
			setupColorPreference();
			setupLocalePreference();
			setupSeriesPreference();
			//setupClearPreference(); // no use
		}

		private ListPreference getColorPreference(){
			return (ListPreference) findPreference(getString(R.string.prefs_filter_color));
		}

		private ListPreference getLocalePreference(){
			return (ListPreference) findPreference(getString(R.string.prefs_filter_locale));
		}

		private SeriesPreference getSeriesPreference(){
			return (SeriesPreference)findPreference( getString(R.string.prefs_filter_series ));
		}

		private void setupColorPreference(){
			setupListPreference(
					getColorPreference(),
					mColors,
					AmpleSettings.getColorFilterEntry(getActivity()), AmpleSettings.getColorFilter(getActivity()),
					getString(R.string.prefs_filter_color_entry));
	}

		private void setupLocalePreference(  ){
			setupListPreference(
					getLocalePreference(),
					mLocales,
					AmpleSettings.getLocaleFilterEntry(getActivity()), AmpleSettings.getLocalFilter(getActivity()),
					getString(R.string.prefs_filter_locale_entry ));
		}

		private void setupSeriesPreference(){
			getSeriesPreference().setData(mSeries);
		}

		private void setupClearPreference(){
			final Preference preference = findPreference( getString(R.string.prefs_filter_clear ));
			preference.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					getColorPreference().setValue( "" );
					getLocalePreference().setValue( "" );
					getSeriesPreference().setValue( "" );
					return true;
				}
			});
		}



		private void setupListPreference(ListPreference preference, ItemList list, String defaultEntry, String defaultValue, String entryPreferenceName){

			String[] entries = list.getEntries();
			String[] values = list.getValues();

			if( !list.containsValue(defaultValue) ){
				String[] tempEntries = Arrays.copyOf( entries, entries.length + 1 );
				String[] tempValues = Arrays.copyOf( values, values.length + 1 );

				tempEntries[ tempEntries.length - 1 ] = defaultEntry;
				tempValues[ tempValues.length - 1 ] = defaultValue;

				entries = tempEntries;
				values = tempValues;
			}

			preference.setEntries( entries );
			preference.setEntryValues( values );
			preference.setValue( defaultValue );
		}


		private void showToast( final String text, final int duration ){
			new Handler( Looper.getMainLooper() ).post(
					new Runnable() {
						@Override
						public void run() {
							Toast.makeText( getActivity(), text, duration ).show();
						}
					}
			);
		}

		@Override
		protected void onPostExecute(Void result){
			destroyProgress();
		}

		@Override
		protected void onCancelled(Void result){
			destroyProgress();
		}

		void setupProgress(){
			new Handler( Looper.getMainLooper() ).post(
					new Runnable() {
						@Override
						public void run() {
						mProgressDialog = ProgressDialog.show(
								getActivity(),
								null,
								mFragment.getString( R.string.loading_from_internet )
						);
						}
					}
			);
		}

		void destroyProgress(){
			if( mProgressDialog != null && mProgressDialog.isShowing() )
				mProgressDialog.dismiss();
		}

		ProgressDialog mProgressDialog;
		AmpleFiltersFragment mFragment;

	}

	static ItemList mColors = new ItemList();
	static ItemList mLocales = new ItemList();
	static ArrayList<Category> mSeries = new ArrayList<Category>();

	static boolean mFiltersLoaded = false;

}
