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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.IOException;

/**
 *
 */
public class AmpleDownloadReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			DownloadManager dm = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
			long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			Cursor c = dm.query(
					new DownloadManager.Query()
							.setFilterById(id)
			);
			if (c.moveToFirst()) {
				int statusCol = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
				int statusCode = c.getInt(statusCol);
				switch ( statusCode )
				{
					case DownloadManager.STATUS_SUCCESSFUL:
						proccessDownloadSuccess(
								context,
								id,
								c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)),
								c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
						);
						break;
					case DownloadManager.STATUS_FAILED:
						proccessDownloadFailed(
								context,
								id,
								c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)),
								c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON))
						);
						break;
				}
			}
		}
	}

	private void proccessDownloadFailed( final Context context, final long id, final String description, final int errorCode ) {
		Notification notif = new Notification.Builder(context)
				.setContentTitle(context.getString(R.string.imgDownloadFailed))
				.setContentText(description)
				.setSubText(description)
				.setSmallIcon(android.R.drawable.stat_sys_download_done)
				.build();

		NotificationManager notifMgr = (NotificationManager)context.getSystemService( context.NOTIFICATION_SERVICE );
		try {
			notifMgr.notify(mNofityID, notif);
		}catch (Exception e){

		}
		mNofityID = (mNofityID + 1) % 5000;
	}

	private void proccessDownloadSuccess( final Context context, final long id, final String description, final String localUri ) {
		try {
			final Uri imageUri = Uri.parse( localUri );

			final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension( MimeTypeMap.getFileExtensionFromUrl( localUri ) );

			final Bitmap bitmap = MediaStore.Images.Media.getBitmap(
					context.getContentResolver(),
					imageUri
			);

			Intent openIntent = new Intent()
					.setAction(Intent.ACTION_VIEW)
					.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION )
					.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK )
					.setDataAndType(imageUri, mimeType);

			Intent shareIntent = new Intent()
					.setAction( Intent.ACTION_SEND)
					.putExtra( Intent.EXTRA_STREAM, imageUri)
					.setType(mimeType)
					.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION )
					.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );


			Notification notif = new Notification.Builder(context)
					.setContentTitle(context.getString(R.string.imgDownloaded))
					.setAutoCancel( false )
					.setContentText( description )
					.setSubText( description )
					.addAction(
							android.R.drawable.ic_menu_crop,
							context.getString(R.string.action_open),
							PendingIntent.getActivity( context, 0, openIntent, PendingIntent.FLAG_CANCEL_CURRENT  )
					)
					.addAction(
							android.R.drawable.ic_menu_share,
							context.getString(R.string.action_share),
							PendingIntent.getActivity( context, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT  ) )
					.setSmallIcon(android.R.drawable.stat_sys_download_done)
					.setStyle(new Notification.BigPictureStyle()
									.bigPicture(bitmap)
					)
					.build();

			final NotificationManager notifMgr = (NotificationManager)context.getSystemService( context.NOTIFICATION_SERVICE );

			try {
				notifMgr.notify(mNofityID, notif);
			}catch ( Exception e ){
				// TODO: Notification failed
			}
			mNofityID = (mNofityID + 1) % 5000;

		}catch ( IOException e ){

		}
	}

	static int mNofityID = 0;
}
