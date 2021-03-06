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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Date;


/**
 * Utils
 */
public class InernetUtils {

	public static boolean isNetworkAvailable(final Context context){
		if ( context != null ) {
			final ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo info = connManager.getActiveNetworkInfo();
			return info != null && info.isConnected();
		}
		return false;
	}

	public static boolean isWifiAvailable(final Context context){

		if ( context != null ) {
			final ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo[] connections = connManager.getAllNetworkInfo();
			for ( NetworkInfo netInfo : connections ) {
				if( netInfo != null ) {
					boolean connected = netInfo.isConnected();
					int connectType = netInfo.getType();

					if( connected )
						if( connectType == ConnectivityManager.TYPE_WIFI ||
							connectType == ConnectivityManager.TYPE_ETHERNET )
								return true;
				}
			}
		}
		return false;
	}

}
