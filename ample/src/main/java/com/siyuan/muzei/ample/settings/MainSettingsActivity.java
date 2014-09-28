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

import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.siyuan.muzei.ample.R;

/**
 * Settings Activity
 */
public class MainSettingsActivity extends FragmentActivity {

	MainSettingsFragment mFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().add( android.R.id.content, mFragment = new MainSettingsFragment()).commit();

		setupActionBar();
	}

	void setupActionBar(){
		getActionBar().getCustomView().findViewById(R.id.action_done).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu( menu );
		getMenuInflater().inflate( R.menu.settings_options, menu );
		return true;
	}

	@Override
	protected void onStart(){
		super.onStart();
		mFragment.refresh();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.action_about:
				onAboutMenuItemPressed(item);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onAboutMenuItemPressed(final MenuItem item){
		startActivity(new Intent()
				.setClass(getApplicationContext(), AboutActivity.class)
		);
	}


}
