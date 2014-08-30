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

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.MuzeiArtSource;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSourceEx;
import com.google.android.apps.muzei.api.UserCommand;
import com.siyuan.muzei.ample.settings.AmpleSettingsActivity;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;


public class AmpleArtSource extends RemoteMuzeiArtSourceEx {

	public static SharedPreferences getSharedPreferences(Context context) {
		return MuzeiArtSource.getSharedPreferences( context, SOURCE_NAME );
	}

	private static final String SOURCE_NAME = "AmpleArtSource";
	private static final String TAG = "Ample";
	private static final AmpleService mService = new AmpleService();

	public AmpleArtSource() {
		super(SOURCE_NAME);
	}


	@Override
	public void onCreate() {
		super.onCreate();
		setDescription(getString(R.string.source_description));
		manageUserCommands();
	}

	private void showToast( final String text, final int duration ){
		new Handler( Looper.getMainLooper() ).post(
			new Runnable() {
				@Override
				public void run() {
					Toast.makeText( getApplicationContext(), text, duration ).show();
				}
			}
		);
	}

	@Override
	protected void onUpdateWithoutNetwork( int reason ){
		if( reason == UPDATE_REASON_USER_NEXT )
			showToast( getString(R.string.noInternet), Toast.LENGTH_SHORT );
	}

	@Override
	protected void onUpdateWithoutWifi( int reason ){
		if( reason == UPDATE_REASON_USER_NEXT )
			showToast( getString(R.string.noInternet), Toast.LENGTH_SHORT );
		reschdule();
	}

	@Override
	protected void onTryUpdate(int reason) throws RetryException {

		// Get next
		AmpleService.ImageData nextImage;
		try {

			nextImage = getNextImage( getNextThumbnail(
					AmpleSettings.getSourceFrom( this ),
					AmpleSettings.getFilterArgs( this )) );

		}catch ( IOException e ){
			throw new RetryException();
		}catch ( NoImageException e ){
			Log.w(TAG, "No images returned.");
			if( reason == UPDATE_REASON_USER_NEXT )
				showToast( getString( R.string.noImages ) ,Toast.LENGTH_SHORT );
			reschdule();
			return;
		}

		// Build
		publishArtwork(new Artwork.Builder()
				.title( nextImage.userName )
				.byline( nextImage.getDescription() )
				.imageUri( Uri.parse( AmpleService.BASE_URI + nextImage.imageUri ) )
				.token( nextImage.code )
				.viewIntent(
						new Intent(
							Intent.ACTION_VIEW,
							Uri.parse( AmpleService.BASE_URI + nextImage.pageUri )
						)
				)
				.build()
		);
		reschdule();
	}

	private AmpleService.Thumbnail getNextThumbnail( final int source, final String args ) throws IOException, NoImageException{

		final Random random = new Random();
		final int countPerPage = source == AmpleService.THUMBNAIL_SOURCE_NEWEST? AmpleService.THUMBNAIL_NEWEST_LIST_COUNT : AmpleService.THUMBNAIL_CONTENT_LIST_COUNT;
		int maxPageCount = (int)Math.ceil( AmpleSettings.getTopCosplays(this) / countPerPage);
		List<AmpleService.Thumbnail> thumbnails;

		// Get thumbnails from random page
		while( true ) {
			final int currentPage = random.nextInt( maxPageCount + 1 );
			thumbnails = mService.getThumbnails( currentPage, source, args );  // index start with 0

			if (thumbnails.size() <= 0) {
				maxPageCount = currentPage - 1;
				if (currentPage <= 0)
					throw new NoImageException();
			}else {
				break;
			}
		}

		// Get random result from thumbnails
		AmpleService.Thumbnail result;
		final String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : null;

		while (true) {
			final int nextIndex = random.nextInt( thumbnails.size() );
			result = thumbnails.get( nextIndex );
			final String nextToken = result.code;
			if (!nextToken.equals( currentToken ) || thumbnails.size() == 1 ) {
				break;
			}
		}

		return result;
	}

	private AmpleService.ImageData getNextImage( AmpleService.Thumbnail thumb ) throws IOException{
		return mService.getImageData( thumb );
	}

	private static final int COMMAND_ID_SHARE = MAX_CUSTOM_COMMAND_ID;
	private static final int COMMAND_ID_DOWNLOAD = MAX_CUSTOM_COMMAND_ID - 1;
	private static final int COMMAND_ID_SETTINGS = MAX_CUSTOM_COMMAND_ID - 2;


	private void manageUserCommands( ){
		List<UserCommand> commands = new ArrayList<UserCommand>();
		commands.add(new UserCommand(BUILTIN_COMMAND_ID_NEXT_ARTWORK, ""));
		commands.add(new UserCommand(COMMAND_ID_SHARE, getString(R.string.action_share)));
		commands.add(new UserCommand(COMMAND_ID_DOWNLOAD, getString(R.string.action_download)));
		commands.add(new UserCommand(COMMAND_ID_SETTINGS, getString(R.string.action_settings)));
		setUserCommands(commands);
	}

	@Override
	protected boolean isUpdateWifiOnly(){
		return AmpleSettings.getWifiOnly( this );
	}

	@Override
	protected void onCustomCommand(int id){
		super.onCustomCommand( id );

		switch ( id ){
			case COMMAND_ID_SHARE:
			case COMMAND_ID_DOWNLOAD:
				{
					Artwork currentArtwork = getCurrentArtwork();
					final String characterDescription = currentArtwork.getByline().trim();
					final String userName = currentArtwork.getTitle().trim();
					final String link = currentArtwork.getViewIntent().getDataString();
					final Uri imageUri = currentArtwork.getImageUri();

					if( id == COMMAND_ID_SHARE )
						makeShare( characterDescription, userName, link );
					else if( id == COMMAND_ID_DOWNLOAD )
						makeDownload( characterDescription, userName, imageUri );
				}
				break;
			case COMMAND_ID_SETTINGS:

				Intent intent = new Intent(this.getApplication(), AmpleSettingsActivity.class);
				intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION );
				startActivity( intent );
				break;
		}
	}

	private void makeDownload( final String characterDescription, final String userName, final Uri imageUri ){

		String fileName = imageUri.getPath();
		fileName = fileName.substring( fileName.lastIndexOf( "/" )+1 );

		DownloadManager.Request request = new DownloadManager.Request( imageUri )
			.setDestinationInExternalPublicDir( Environment.DIRECTORY_DOWNLOADS + "/Ample/", fileName)
			.setTitle( getString( R.string.imgDownloading ) )
			.setDescription( userName )
			.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE )
			.setAllowedOverRoaming( true )
			.setAllowedNetworkTypes( DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI );

		request.allowScanningByMediaScanner();		// add to media for SHARE permission

		try {

			final DownloadManager dm = (DownloadManager)getSystemService( DOWNLOAD_SERVICE );

			dm.enqueue( request );
		}catch (Exception e){
			Log.e( TAG, e.getMessage() );
		}


	}

	private void makeShare( final String characterDescription, final String userName, final String link ){
		Intent intent = new Intent()
			.setAction( Intent.ACTION_SEND )
			.setType("text/plain")
			.putExtra(Intent.EXTRA_TEXT,
					String.format(
							getString(R.string.shareTextContent),
							characterDescription,
							userName,
							link
					)
			)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		//intent = Intent.createChooser( intent, getString( R.string.shareChooserTitle ) );
		startActivity(intent);
	}


	private void reschdule(){
		final long interval = AmpleSettings.getRefreshInterval(this);

		if( interval > 0 )
			scheduleUpdate(System.currentTimeMillis() + interval );
	}

	public static class NoImageException extends Exception {
		public NoImageException() {
		}

		public NoImageException(Throwable cause) {
			super(cause);
		}
	}
}

