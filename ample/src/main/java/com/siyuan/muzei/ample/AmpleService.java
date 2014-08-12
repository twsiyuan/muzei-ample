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

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for loading images
 */
public class AmpleService {

	private static final String TAG = "AmpleService";

	public class Thumbnail{
		public Thumbnail( final String code ) {
			this.code = code;
		}
		public String code;
	}

	public class ImageData	{
		public String code;
		public String imageUri;
		public String characterName;
		public String characterColor;
		public String characterSeries;
		public String pageUri;
		public String location;
		public String userSex;
		public String userName;

		public String getDescription(){
			String result = "";
			if( this.characterName.length() > 0 )
				result += ( (result.length() > 0 )? "\n" : "" ) + this.characterName;
			if( this.characterSeries.length() > 0 && !this.characterSeries.equals( this.characterName ) )
				result += ( (result.length() > 0 )? "\n" : "" ) + this.characterSeries;
			if( this.location.length() > 0 )
				result += ( (result.length() > 0 )? "\n" : "" ) + this.location;
			return result;
		}
	}

	public static final String BASE_URI = "http://ample-cosplay.com";

	private static final String PAGE_URI = "/detail/?code=";
	private static final String THUMBNAIL_URI = "/file/pic_top_thum/";
	private static final String IMAGE_URI = "/file/pic/";

	private static final String THUMBNAIL_LIST_URI = "/js/getPhotoMore.php?p=";

	private final Pattern pageCodePattern = Pattern.compile("alt\\=\\\"([^\\\"]+)\\\"");
	private final Pattern imageCodePattern = Pattern.compile("\\/([^.\\/]+)(.\\w+$)");

	public List<Thumbnail> getThumbnails( ) throws IOException {
		return getThumbnails( 1 );
	}

	public List< Thumbnail > getThumbnails( int page ) throws IOException {

		long timeStamp = System.currentTimeMillis();

		List<Thumbnail> result = new ArrayList<Thumbnail>();
		String html = loadHTML(BASE_URI + THUMBNAIL_LIST_URI + page);

		long timeStamp2 = System.currentTimeMillis();

		if( html.trim().length() > 0 ) {

			// Parse for all thumbnail code
			Matcher pageCodeMatcher = pageCodePattern.matcher(html);
			while (pageCodeMatcher.find()) {
				String code = pageCodeMatcher.group(1);

				result.add(new Thumbnail(code));
			}
		}

		if( BuildConfig.DEBUG ){
			Log.d( TAG, String.format("loaded thumbs %d ms, parsed %d ms", timeStamp2 - timeStamp, System.currentTimeMillis() - timeStamp2 ) );
		}

		return result;
	}

	public ImageData getImageData( final Thumbnail thumbnail ) throws IOException{
		return getImageData( thumbnail.code );
	}

	public ImageData getImageData( final String code ) throws IOException{

		long timeStamp = System.currentTimeMillis();

		String pageUri = PAGE_URI + code;
		String finalUrl = BASE_URI + pageUri;

		String characterSeries = "";
		String userName = "";
		String imageUri;
		String characterName = "";
		String characterColor = "";

		String location = "";
		String userSex = "";

		String html = loadHTML(finalUrl);
		{
			// for fast parsing
			final int idx = html.indexOf("<!-- | MainContent START | -->");
			if( idx >= 0 )
				html = html.substring( idx );
		}
		long timeStamp2 = System.currentTimeMillis();
		Document doc = Jsoup.parse(html);

		{
			String[] temp = doc.title().split("-");
			if( temp.length == 3 ){
				userName = temp[1].trim();
				characterSeries = temp[2].trim();
			}
		}
		{
			Elements eles = doc.select("ul[class=PhotoData d_top clearfix heightLineParent]");

			userName = getElementText(eles, "li:eq(0) span", userName);
			userSex = getElementText(eles, "li:eq(1) span", userSex);
			location = getElementText(eles, "li:eq(2) a", location);
		}
		{
			Elements eles = doc.select("ul[class=PhotoData d_bottom clearfix]");

			characterName = getElementText(eles, "li:eq(0) span", characterName);
			characterSeries = getElementText(eles, "li:eq(1) a", characterSeries);
			characterColor = getElementText(eles, "li:eq(2) a", characterColor);
		}
		{
			Elements eles = doc.select("div[class=pict]");

			imageUri = eles.select("img").attr("src");
			if( imageUri.startsWith("../") ){
				imageUri = imageUri.substring(2);
			}
		}

		ImageData result = new ImageData();
		result.code = code;
		result.pageUri = pageUri;
		result.imageUri = imageUri;

		result.userName = userName;
		result.userSex = userSex;

		result.location = location;

		result.characterColor = characterColor;
		result.characterName = characterName;
		result.characterSeries = characterSeries;

		if( BuildConfig.DEBUG ){
			Log.d( TAG, String.format("loaded imageData %d ms, parsed %d ms", timeStamp2 - timeStamp , System.currentTimeMillis()- timeStamp2 ) );
		}

		return result;
	}

	private String getElementText(Elements elements, String query, String defaultValue){
		Elements results = elements.select( query );
		if( results.isEmpty() ){
			return defaultValue;
		}else{
			return results.text();
		}
	}

	private final String loadHTML( final String uri ) throws IOException {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url( uri )
				.build();

		Response response = client.newCall(request).execute();
		return response.body().string();

		//HttpClient client = new DefaultHttpClient();
		//HttpGet get = new HttpGet( uri );
		//HttpResponse response = client.execute( get );
		//HttpEntity entity = response.getEntity();
		//return entity == null? "" : EntityUtils.toString( entity, "utf-8" );
	}
}
