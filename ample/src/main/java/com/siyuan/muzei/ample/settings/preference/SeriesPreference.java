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
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;

import android.widget.ExpandableListView;

import android.widget.SearchView;
import android.widget.TextView;

import com.siyuan.muzei.ample.R;
import com.siyuan.muzei.ample.data.Category;
import com.siyuan.muzei.ample.data.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static android.preference.PreferenceManager.*;

/**
 * Series
 */
public class SeriesPreference extends DialogPreference {

	public SeriesPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource( R.layout.series_dialog );

		for (int i = 0; i < attrs.getAttributeCount(); i++) {
			final String attr = attrs.getAttributeName(i);
			String val = attrs.getAttributeValue(i);

			if (attr.equalsIgnoreCase("entryKey")) {
				final int restId = context.getResources().getIdentifier(val, "string", context.getPackageName());
				if (restId != 0) val = context.getString(restId);
				mEntryKey = val;
			}
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getContext() );
		mSelectedEntry = prefs.getString( mEntryKey, getContext().getString( R.string.settings_filter_none_use ) );
		mSelectedValue = prefs.getString( getKey(), "" );

		setSummary( mSelectedEntry );
	}

	/**
	 * Get value
	 * @param value
	 * @return get current value
	 */
	public String getValue(String value) {
		return mSelectedValue;
	}

	/**
	 * Set value
	 * @param value
	 */
	public void setValue(String value) {
		mSelectedValue = value;
		mSelectedEntry = "";
		mSelectedItem = null;
		mSelectedView = null;

		ItemPosition position = findPosition( value );
		if( position.isValid() )
			mSelectedEntry = getItem( position ).entry;

		persistValue();

		setSummary( mSelectedEntry );

		if (getDialog() != null) {
			updateSelectedViewItem();
			if (isSearching())
				clearSearch();
		}
	}

	/**
	 * Set data
	 *
	 * @param data
	 */
	public void setData(ArrayList<Category> data) {
		mCatData = new SeriesAdapter( data );
	}

	AsyncTask<Void, Void, Void> mSearchHandler;

	void doSearch(final String query){
		if( mSearchHandler != null )
			mSearchHandler.cancel( true );
		mSearchHandler = new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				mResult = search(query);
				return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				mSearchHandler = null;
				Collections.sort( mResult );
				setSearchResult( mResult );
			}
			ArrayList<Item> mResult;
		};
		mSearchHandler.execute();
	}

	ArrayList<Item> search(final String query){
		Pattern pattern = Pattern.compile( query, Pattern.CASE_INSENSITIVE );
		ArrayList<Item> result = new ArrayList<Item>();
		for( int cc = 0; cc < mCatData.getGroupCount(); ++cc )
			for( int tt = 0; tt < mCatData.getChildrenCount( cc ); ++tt ){
				Item item = (Item)mCatData.getChild( cc, tt );
				if( pattern.matcher( item.entry ).find() )
					result.add( item );
			}
		return result;
	}

	void setSearchResult(ArrayList<Item> result){
		mSearchData = new ResultAdapter( result );
		mListView.setAdapter( mSearchData );
		mListView.expandGroup( 0 );
	}

	boolean isSearching(){
		return  mListView.getExpandableListAdapter() == mCatData;
	}

	void clearSearch(){
		if( !isSearching() ) {
			if( mSearchView.getQuery().length() > 0 )
				mSearchView.setQuery( "", false );
			setDefaultItems( mListView );
			{
				ItemPosition position = findPosition(mSelectedItem);
				if (position.isValid() )
					mListView.setSelectedChild(position.groupPosition, position.childPosition, true);
			}
		}
	}

	void setDefaultItems(ExpandableListView list){
		list.setAdapter(mCatData);
		for( int cc = 0; cc < mCatData.getGroupCount(); ++cc )
			if( mCatData.getCategory( cc ).name.length() <= 0 )
				list.expandGroup( cc );
	}


	ItemPosition findPosition(String value){
		for( int cc = 0; cc < mCatData.getGroupCount(); ++cc ) {
			for (int ii = 0; ii < mCatData.getChildrenCount( cc ); ++ii){
				Item item = (Item)mCatData.getChild( cc, ii );
				if( item.value.equals( value ) ){
					return new ItemPosition( cc, ii );
				}
			}
		}
		return new ItemPosition( -1, -1 );
	}

	ItemPosition findPosition(Item value){
		if( mSelectedItem != null )
			for( int cc = 0; cc < mCatData.getGroupCount(); ++cc ) {
				for (int ii = 0; ii < mCatData.getChildrenCount( cc ); ++ii){
					Item item = (Item)mCatData.getChild( cc, ii );
					if( item == value ){
						return new ItemPosition( cc, ii );
					}
				}
			}
		return new ItemPosition( -1, -1 );
	}

	Item getItem( ItemPosition position ){
		if( position.isValid() )
			return (Item)mCatData.getChild( position.groupPosition, position.childPosition );
		return null;
	}

	void updateSelectedViewItem(){
		if( mListView == null )
			return;

		ItemPosition position = findPosition( mSelectedValue );

		if( position.isValid() ){
			mListView.setSelectedChild( position.groupPosition, position.childPosition, true );
		}else{
			mListView.setSelectedChild( 0, 0, true );
		}
		View oldView = mSelectedView;
		mSelectedItem = getItem( position );
		mSelectedView = null;

		setSelectedViewBackground( oldView, false );
	}

	class ItemPosition{
		public ItemPosition( int groupPosition, int childPosition ) {
			this.groupPosition = groupPosition;
			this.childPosition = childPosition;
		}
		public boolean isValid(){
			return groupPosition >= 0 && childPosition >= 0;
		}
		public int groupPosition;
		public int childPosition;
	}

	@Override
	protected void onPrepareDialogBuilder(android.app.AlertDialog.Builder builder){

		super.onPrepareDialogBuilder( builder );

		LayoutInflater inflater = (LayoutInflater)builder.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );

		View titleView = inflater.inflate( R.layout.series_dialog_title , null );
		TextView textView = (TextView)titleView.findViewById( R.id.series_title_tv );
		textView.setText( getTitle() );

		SearchView searchView = mSearchView = (SearchView)titleView.findViewById( R.id.series_title_search );
		searchView.setActivated( true );
		searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				if( s.length() == 0 )
					clearSearch();
				else
					doSearch(s);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				if( s.length() == 0 )
					clearSearch();
				else
					doSearch(s);
				return true;
			}
		});

		builder.setCustomTitle( titleView );
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		final ExpandableListView list = (ExpandableListView) view.findViewById(R.id.series_list);
		setDefaultItems(list);
		list.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView listView, View view, int groupPosition, int childPosition, long id) {

				view.setSelected( true );
				int index = listView.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
				listView.setItemChecked(index, true);

				View oldSelectedView = mSelectedView;
				Item item = (Item) listView.getExpandableListAdapter().getChild(groupPosition, childPosition);
				mSelectedItem = item;
				mSelectedView = view;

				setSelectedViewBackground( oldSelectedView, false );
				setSelectedViewBackground( mSelectedView, true );

				return true;
			}
		});
		list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				int groupCount = list.getExpandableListAdapter().getGroupCount();
				for (int gg = 1; gg < groupCount; ++gg)
					if (gg != groupPosition)
						list.collapseGroup(gg);
			}
		});
		mListView = list;
		updateSelectedViewItem();
	}

	void setSelectedViewBackground( View view, boolean selected ){
		if( view != null )
			if( selected ){
				view.setBackgroundColor( getContext().getResources().getColor( android.R.color.holo_blue_dark ) );
			}else{
				view.setBackgroundColor( getContext().getResources().getColor( android.R.color.transparent ) );
			}
	}

	@Override
	protected void showDialog(android.os.Bundle state){
		super.showDialog( state );

		getDialog().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED |
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
		);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		mListView = null;
		if (positiveResult && mSelectedItem != null) {
			mSelectedValue = mSelectedItem.value;
			mSelectedEntry = mSelectedItem.entry;

			persistValue();

			setSummary( mSelectedEntry ) ;
		}
	}

	private void persistValue(){
		String value = mSelectedValue;
		String entry = mSelectedEntry;

		if (persistString( value )) {
			SharedPreferences.Editor prefEditor = getDefaultSharedPreferences(getContext()).edit();
			prefEditor.putString(mEntryKey, entry );
			prefEditor.apply();
		}
	}

	class SeriesAdapter extends BaseExpandableListAdapter {

		public SeriesAdapter( ArrayList< Category > data ){
			super();
			mData = data;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return getCategory( groupPosition ).items.get( childPosition );
		}

		@Override
		public long getChildId( int groupPosition, int childPosition ) {
			return groupPosition << 20 + childPosition;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return getCategory( groupPosition ).items.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return getCategory(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return mData.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public boolean areAllItemsEnabled(){
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
								 View convertView, ViewGroup parent) {
			Category cat = (Category)getGroup( groupPosition );
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE );
			if( cat.name.length() <= 0 ){
				convertView = inflater.inflate( R.layout.series_empty_row, null);
			}else {
				convertView = inflater.inflate( R.layout.series_group, null);
				TextView text = (TextView)convertView.findViewById( R.id.series_group_tv );
				text.setText( cat.name );
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, final int childPosition,
								 boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE );
				convertView = inflater.inflate(R.layout.series_child, null);
			}
			Item item = (Item)getChild( groupPosition, childPosition );

			TextView text = (TextView)convertView.findViewById( R.id.series_child_tv );
			text.setText( item.entry );

			setSelectedViewBackground( convertView, item == mSelectedItem );
			if( item == mSelectedItem )
				mSelectedView = convertView;

			return convertView;
		}

		Category getCategory( int groupPosition ){
			if( groupPosition >= 0 && groupPosition < mData.size() )
				return mData.get( groupPosition );
			return null;
		}

		ArrayList< Category > mData;
	}

	class ResultAdapter extends BaseExpandableListAdapter {

		public ResultAdapter( ArrayList< Item > data ){
			super();
			mData = data;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mData.get( childPosition );
		}

		@Override
		public long getChildId( int groupPosition, int childPosition ) {
			return groupPosition << 20 + childPosition;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mData.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public int getGroupCount() {
			return 1;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public boolean areAllItemsEnabled(){
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
								 View convertView, ViewGroup parent) {
			if( convertView == null ) {
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE );
				convertView = inflater.inflate(R.layout.series_empty_row, null);
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, final int childPosition,
								 boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE );
				convertView = inflater.inflate(R.layout.series_child, null);
			}
			Item item = (Item)getChild( groupPosition, childPosition );

			TextView text = (TextView)convertView.findViewById( R.id.series_child_tv );
			text.setText( item.entry );

			setSelectedViewBackground( convertView, item == mSelectedItem );
			if( item == mSelectedItem )
				mSelectedView = convertView;

			return convertView;
		}

		List< Item > mData;
	}

	String mEntryKey;
	String mSelectedEntry;
	String mSelectedValue;
	Item mSelectedItem;
	View mSelectedView;

	ExpandableListView mListView;
	SearchView mSearchView;
	SeriesAdapter mCatData = null;
	ResultAdapter mSearchData = null;
}
