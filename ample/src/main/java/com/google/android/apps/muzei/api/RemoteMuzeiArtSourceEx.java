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

package com.google.android.apps.muzei.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.text.format.DateUtils;
import android.util.Log;

public abstract class RemoteMuzeiArtSourceEx extends RemoteMuzeiArtSource {

	private static final String TAG = "MuzeiArtSourceEx";

	private static final long FETCH_WAKELOCK_TIMEOUT_MILLIS = 30 * DateUtils.SECOND_IN_MILLIS;
	private static final long INITIAL_RETRY_DELAY_MILLIS = 10 * DateUtils.SECOND_IN_MILLIS;
	private static final long NOWIFI_RETRY_DELAY_MILLIS = 30 * DateUtils.MINUTE_IN_MILLIS;

	private static final String PREF_RETRY_ATTEMPT = "retry_attempt";

	private String mName;

	/**
	 * Remember to call this constructor from an empty constructor!
	 */
	public RemoteMuzeiArtSourceEx(String name) {
		super(name);
		mName = name;
	}

	/**
	 * Subclasses of {@link RemoteMuzeiArtSource} should implement {@link #onTryUpdate(int)}
	 * instead of this method.
	 * (make a extend.. :()
	 */
	@Override
	protected void onUpdate(int reason) {
		PowerManager pwm = (PowerManager) getSystemService(POWER_SERVICE);
		PowerManager.WakeLock lock = pwm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, mName);
		lock.acquire( FETCH_WAKELOCK_TIMEOUT_MILLIS );

		SharedPreferences sp = getSharedPreferences();

		try {
			// check network status
			if( !checkNetworkAvailable( reason ) )
				return;

			// In anticipation of update success, reset update attempt
			// Any alarms will be cleared before onUpdate is called
			sp.edit().remove(PREF_RETRY_ATTEMPT).apply();
			setWantsNetworkAvailable(false);

			// Attempt an update
			onTryUpdate(reason);

		} catch (RetryException e) {
			Log.w(TAG, "Error fetching, scheduling retry, id=" + mName);

			// Schedule retry with exponential backoff, starting with INITIAL_RETRY... seconds later
			int retryAttempt = sp.getInt(PREF_RETRY_ATTEMPT, 0);
			scheduleUpdate(
					System.currentTimeMillis() + (INITIAL_RETRY_DELAY_MILLIS << retryAttempt));
			sp.edit().putInt(PREF_RETRY_ATTEMPT, retryAttempt + 1).apply();
			setWantsNetworkAvailable(true);

		} finally {
			if (lock.isHeld()) {
				lock.release();
			}
		}
	}

	private boolean checkNetworkAvailable( int reason ) throws RetryException{
		final ConnectivityManager connMgr = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE );
		{
			NetworkInfo ni = connMgr.getActiveNetworkInfo();
			if (ni == null || !ni.isConnected()) {
				Log.d(TAG, "No network connection; not attempting to fetch update, id=" + mName);
				onUpdateWithoutNetwork(reason);
				throw new RetryException();
			}
		}
		if( isUpdateWifiOnly() )
		{
			boolean wifiAvailable = false;
			NetworkInfo[] connections = connMgr.getAllNetworkInfo();
			for ( NetworkInfo ni : connections ) {
				if( ni != null ) {
					final int connectType = ni.getType();
					if (ni.isConnected())
						if (connectType == ConnectivityManager.TYPE_WIFI ||
								connectType == ConnectivityManager.TYPE_ETHERNET) {
							wifiAvailable = true;
							break;
						}
				}
			}
			if( !wifiAvailable ){
				Log.d(TAG, "No network connection; not attempting to fetch update, id=" + mName);
				onUpdateWithoutWifi(reason);
				return false;
			}
		}

		return true;
	}
	/**
	 * Update without network
	 * @param reason
	 */
	protected void onUpdateWithoutNetwork(int reason){

	}

	/**
	 * Update without wifi only {@link #isUpdateWifiOnly} is true
	 * Note: Must do {@link #scheduleUpdate} for update schedule.
	 * @param reason
	 */
	protected void onUpdateWithoutWifi(int reason){
		scheduleUpdate( System.currentTimeMillis() + NOWIFI_RETRY_DELAY_MILLIS );
	}


	/**
	 * Update source only on wifi connection
	 * @return true if only update source via wifi connection
	 */
	protected boolean isUpdateWifiOnly(){
		return false;
	}
}
