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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.siyuan.muzei.ample.R;

/**
 * About Activity
 */
public class AmpleAboutActivity extends FragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.about_activity );
		setupVersion();
		setupBody();
	}

	void setupVersion(){

		final PackageManager pm = getPackageManager();
		String versionName;
		try{
			PackageInfo info = pm.getPackageInfo( getPackageName(), 0 );
			versionName = info.versionName;
		}catch ( PackageManager.NameNotFoundException e ){
			versionName = "N/A";
		}

		TextView view = (TextView)findViewById( R.id.app_version );
		view.setText( Html.fromHtml(
						getString( R.string.about_version_template, versionName )) );
	}

	void setupBody(){
		TextView view = (TextView)findViewById( R.id.about_body );
		view.setText( Html.fromHtml( getString( R.string.about_body ) ) );
		view.setMovementMethod( new LinkMovementMethod() );
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
