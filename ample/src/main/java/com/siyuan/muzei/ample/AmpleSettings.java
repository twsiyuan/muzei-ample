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
import android.text.format.DateUtils;

/**
 * AmpleSettings, wrapper for preferences
 */
public class AmpleSettings {

	public static boolean getWifiOnly( final Context context ){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		final boolean defaultValue = Boolean.parseBoolean( context.getString( R.string.prefs_wifionly_default ) );
		return settings.getBoolean( context.getString( R.string.prefs_wifionly ), defaultValue );
	}

	public static long getRefreshInterval(final Context context){
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		final String stringVal = settings.getString(
				context.getString( R.string.prefs_refresh_interval ),
				context.getString( R.string.prefs_refresh_interval_default )
				);

		try {
			return Long.parseLong( stringVal );
		}catch ( NumberFormatException e ){
			return -1;
		}
	}

	public static int getTopPages( final Context context ){
		return 8;
	}

}
