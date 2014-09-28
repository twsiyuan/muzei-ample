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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.siyuan.muzei.ample.utils.InernetUtils;
import com.siyuan.muzei.ample.utils.PreferenceUtils;
import com.siyuan.muzei.ample.R;
import com.siyuan.muzei.ample.data.Category;
import com.siyuan.muzei.ample.data.DataService;
import com.siyuan.muzei.ample.data.Item;
import com.siyuan.muzei.ample.data.ItemList;
import com.siyuan.muzei.ample.settings.preference.SeriesPreference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.siyuan.muzei.ample.data.DataService.FilterData;


public class FilterSettingsFragment extends PreferenceFragment{

	static final String TAG = "AmpleFiltersFragment";

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		new InitAsyncTask( this ).execute(true, false);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView( inflater, container, savedInstanceState );
		ListView listView = mMainView = (ListView) view.findViewById(android.R.id.list);
		if( listView != null ) {
			listView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
					ListView listView = (ListView) adapterView;
					if( listView != null ){
						Object obj = listView.getAdapter().getItem(position);

						if( clearPreference( obj ) )
							return true;
					}
					return false;
				}
			});
		}
		return view;
	}

	private boolean clearPreference( Object obj ){
		if (obj != null ) {
			if( obj instanceof  ListPreference ) {
				((ListPreference) obj).setValue("");
				return true;
			}else if( obj instanceof SeriesPreference){
				((SeriesPreference)obj).setValue("");
				return true;
			}else if( obj instanceof EditTextPreference){
				((EditTextPreference)obj).setText("");
				return true;
			}
		}
		return false;
	}

	public void clearPreferences(){
		ListAdapter listAdapter = mMainView.getAdapter();
		for( int ii = 0; ii < listAdapter.getCount(); ++ii ){
			Object obj = listAdapter.getItem( ii );
			clearPreference( obj );
		}
	}

	public void refreshFilters(){
		new InitAsyncTask( this ).execute( false, true );
	}

	private class InitAsyncTask extends AsyncTask<Boolean, Void, Void>{

		public InitAsyncTask( FilterSettingsFragment fragment ){
			super();
			mFragment = fragment;
		}

		@Override
		protected Void doInBackground(Boolean... args) {
			final boolean initPreferences = mInitPreferences = args[ 0 ];
			final boolean forceReload = args[ 1 ];

			final Context context = getActivity();
			if( !InernetUtils.isNetworkAvailable(context) ){
				showToast( mFragment.getString( R.string.noInternet ), Toast.LENGTH_SHORT );
			}else{
				if( !FilterSettingsFragment.mFiltersLoaded || forceReload ) {
					setupProgress();
					try {
						FilterData filterData = DataService.getFilterData( context );
						setupNonUseValues(filterData);
						mFilterData = filterData;
						FilterSettingsFragment.mFiltersLoaded = true;
					} catch (IOException e) {
						showToast(mFragment.getString(R.string.unknown_exception_try_again), Toast.LENGTH_SHORT);
					}finally {

					}
				}
			}
			if( mFilterData == null ){
				mFilterData = createDefaultFilterData();
			}

			return null;
		}

		private FilterData createDefaultFilterData(){
			FilterData result = new FilterData();
			result.Colors = new ItemList();
			result.Locales = new ItemList();
			result.Series = new ArrayList<Category>();
			result.Users = new ItemList();
			setupNonUseValues(result);
			return result;
		}

		private void setupNonUseValues(FilterData filterData){
			// Colors & Locales
			filterData.Colors.add( 0, createNonUseItem() );
			filterData.Locales.add( 0, createNonUseItem() );
			filterData.Users.add( 0, createNonUseItem() );

			// Series category
			Category cat = new Category( "" );
			cat.items.add( createNonUseItem() );
			filterData.Series.add( 0, cat );
		}

		private void setupPreferences( Boolean addPreferences, FilterData filterData ){
			if( addPreferences )
				mFragment.addPreferencesFromResource( R.xml.filters );
			setupColorPreference( filterData.Colors );
			setupLocalePreference( filterData.Locales );
			setupSeriesPreference( filterData.Series );
			setupUserPreference(filterData.Users);
		}

		private ListPreference getColorPreference(){
			return (ListPreference) findPreference(getString(R.string.prefs_filter_color));
		}

		private ListPreference getLocalePreference(){
			return (ListPreference) findPreference(getString(R.string.prefs_filter_locale));
		}

		private ListPreference getUserPreference(){
			return (ListPreference) findPreference(getString(R.string.prefs_filter_user));
		}

		private SeriesPreference getSeriesPreference(){
			return (SeriesPreference)findPreference( getString(R.string.prefs_filter_series ));
		}

		private Item createNonUseItem(){
			return new Item( getString( R.string.settings_filter_none_use ) );
		}

		private void setupColorPreference( ItemList values ) {
			setupListPreference(
					getColorPreference(),
					values,
					PreferenceUtils.getColorFilterEntry(getActivity()), PreferenceUtils.getColorFilter(getActivity())
			);
		}

		private void setupUserPreference(ItemList values){
			// Discard values, copy from preferences
			values = new ItemList( PreferenceUtils.getRecentUsers(getActivity()) );
			values.add( 0, createNonUseItem() );

			setupListPreference(
					getUserPreference(),
					values,
					PreferenceUtils.getUserFilterEntry(getActivity()), PreferenceUtils.getUserFilter(getActivity())
			);
		}

		private void setupLocalePreference( ItemList values ){
			setupListPreference(
					getLocalePreference(),
					values,
					PreferenceUtils.getLocaleFilterEntry(getActivity()), PreferenceUtils.getLocalFilter(getActivity())
			);
		}

		private void setupSeriesPreference( ArrayList<Category> values ){
			{
				String defaultEntry = PreferenceUtils.getSeriesFilterEntry( getActivity() );
				String defaultValue = PreferenceUtils.getSeriesFilter( getActivity() );

				// Default value check
				boolean contains = false;
				for( Category cat : values ){
					for( Item item : cat.items ){
						if( item.value.equals( defaultValue ) ){
							contains = true;
							break;
						}
					}
					if( contains )
						break;
				}
				if( !contains ){
					ArrayList temp = new ArrayList( values );
					Category cat = new Category( "" );
					cat.items.add( new Item( defaultEntry, defaultValue ) );
					temp.add( 1, cat );
					values = temp;
				}
			}
			getSeriesPreference().setData( values );
		}

		private void setupListPreference(ListPreference preference, ItemList list, String defaultEntry, String defaultValue){

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
			setupPreferences( mInitPreferences, mFilterData );
		}

		@Override
		protected void onCancelled(Void result){
			destroyProgress();
			setupPreferences( mInitPreferences, mFilterData );
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

		boolean mInitPreferences;
		ProgressDialog mProgressDialog;
		FilterSettingsFragment mFragment;

	}

	ListView mMainView;
	static boolean mFiltersLoaded = false;
	static FilterData mFilterData = null;

}
