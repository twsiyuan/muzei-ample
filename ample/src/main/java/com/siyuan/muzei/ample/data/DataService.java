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

package com.siyuan.muzei.ample.data;

import android.content.Context;
import android.util.Log;

import com.siyuan.muzei.ample.BuildConfig;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for loading images
 */
public class DataService {

	public static final int THUMBNAIL_SOURCE_CONTENT = 0;
	public static final int THUMBNAIL_SOURCE_NEWEST = 1;

	public static final int THUMBNAIL_CONTENT_LIST_COUNT = 20;	// Approximate value
	public static final int THUMBNAIL_NEWEST_LIST_COUNT = 40;

	public static final String FILTER_ARG_NAME_KEYWORD = "t";
	public static final String FILTER_ARG_NAME_LOCALE = "c";
	public static final String FILTER_ARG_NAME_COLOR = "i";
	public static final String FILTER_ARG_NAME_SERIES = "s";
	public static final String FILTER_ARG_NAME_USER = "u";

	public static final String BASE_URI = "http://ample-cosplay.com";

	private static final String TAG = "AmpleService";

	private static final String PAGE_URI = "/detail/?code=";
	private static final String THUMBNAIL_URI = "/file/pic_top_thum/";
	private static final String IMAGE_URI = "/file/pic/";
	private static final String FILTER_URI = "/";

	private static final String THUMBNAIL_CONTENT_LIST_URI = "/js/getPhotoMore.php?p=";
	private static final String THUMBNAIL_NEWEST_LIST_URI = "/js/getThumMore.php?p=";

	private static final Pattern PAGECODE_PATTERN = Pattern.compile( "code\\s?\\=\\s?\\\"?([^\\\"\\&]+)" );
	private static final Pattern FILTER_ARG_PATTERN = Pattern.compile( "\\?(\\s*(\\w+)\\s*=[^&#]+)" );
	private static final List<Thumbnail> EMPTY_LIST = new ArrayList<Thumbnail>( 0 );

	public static class Thumbnail implements Serializable{
		public Thumbnail( final String code ) {
			this.code = code;
		}
		public String code;
	}

	public static class ImageData implements Serializable {
		public String code;
		public String imageUri;
		public String characterName;
		public String characterColor;
		public String characterColorArg;
		public String characterSeries;
		public String characterSeriesArg;
		public String pageUri;
		public String locale;
		public String localeArg;
		public String userSex;
		public String userName;
		public String userNameArgs;

		public String getDescription(){
			String result = "";
			if( this.characterName.length() > 0 )
				result += ( (result.length() > 0 )? "\n" : "" ) + this.characterName;
			if( this.characterSeries.length() > 0 && !this.characterSeries.equals( this.characterName ) )
				result += ( (result.length() > 0 )? "\n" : "" ) + this.characterSeries;
			if( this.locale.length() > 0 )
				result += ( (result.length() > 0 )? "\n" : "" ) + this.locale;
			return result;
		}
	}

	public static class FilterData implements Serializable{
		public ItemList Colors;
		public ItemList Locales;
		public ItemList Users;
		public ArrayList<Category> Series;
	}

	public static List<Thumbnail> getThumbnails( ) throws IOException {
		return getThumbnails( 1, THUMBNAIL_SOURCE_CONTENT );
	}

	public static List<Thumbnail> getThumbnails( final int page ) throws IOException {
		return getThumbnails( page, THUMBNAIL_SOURCE_NEWEST );
	}

	public static List< Thumbnail > getThumbnails( final int page, final int source ) throws IOException {
		return getThumbnails( page, source, "" );
	}

	public static List< Thumbnail > getThumbnails( final int page, final int source, final String args ) throws IOException {
		List<Thumbnail> result = EMPTY_LIST;
		final long timeStamp = System.currentTimeMillis();
		final String url = getThumbnailsListURL( page + 1, source ) + "&" + args;
		final String html = loadHTML( url ).trim();
		final long timeStamp2 = System.currentTimeMillis();

		if( html.length() > 0 ) {

			result = new ArrayList<Thumbnail>();

			// Parse for all thumbnail code
			final Matcher pageCodeMatcher = PAGECODE_PATTERN.matcher(html);
			while (pageCodeMatcher.find()) {
				result.add( new Thumbnail(
						pageCodeMatcher.group(1)
				));
			}

		}

		if( BuildConfig.DEBUG ){
			Log.d( TAG, String.format("loaded thumbs %d ms, parsed %d ms", timeStamp2 - timeStamp, System.currentTimeMillis() - timeStamp2 ) );
		}

		return result;
	}

	public static ImageData getImageData( final Thumbnail thumbnail ) throws IOException{
		return getImageData( thumbnail.code );
	}

	public static ImageData getImageData( String code ) throws IOException{
		//code = "GSNKBRQ83PP9E8A70F69SDTXN3PFHF"; // for screenshot
		long timeStamp = System.currentTimeMillis();

		String pageUri = PAGE_URI + code;
		String finalUrl = BASE_URI + pageUri;

		String characterSeries = "";
		String characterSeriesArg = "";
		String userName = "";
		String userNameArg = "";

		String imageUri;
		String characterName = "";
		String characterColor = "";
		String characterColorArg = "";

		String locale = "";
		String localeArg = "";
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
			Elements elesA = doc.select("ul[class=PhotoData d_top clearfix heightLineParent]");
			Elements elesB = doc.select("ul[class=PhotoData d_bottom clearfix]");
			Elements[] elementses = new Elements[]{ elesA, elesB };

			userSex = getElementText(elesA, "li:eq(1) span", userSex);
			characterName = getElementText(elesB, "li:eq(0) span", characterName);


			for( Elements eles : elementses ) {

				Elements items = eles.select("li");
				for (Element item : items) {
					String href = item.select("a").attr("href");
					String entry = item.select("a").text().trim();
					Matcher hrefMatcher = FILTER_ARG_PATTERN.matcher(href);

					if (entry.length() > 0 && hrefMatcher.find()) {
						String argName = hrefMatcher.group(2);
						String value = hrefMatcher.group(1);
						if (argName.equals(FILTER_ARG_NAME_SERIES)) {
							characterSeries = entry;
							characterSeriesArg = value;
						} else if (argName.equals(FILTER_ARG_NAME_COLOR)) {
							characterColor = entry;
							characterColorArg = value;
						} else if (argName.equals(FILTER_ARG_NAME_USER)) {
							userName = entry;
							userNameArg = value;
						} else if (argName.equals(FILTER_ARG_NAME_LOCALE)) {
							locale = entry;
							localeArg = value;
						}
					}
				}
			}
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
		result.userNameArgs = userNameArg;

		result.locale = locale;
		result.localeArg = localeArg;

		result.characterColor = characterColor;
		result.characterColorArg = characterColorArg;
		result.characterName = characterName;
		result.characterSeries = characterSeries;
		result.characterSeriesArg = characterSeriesArg;

		if( BuildConfig.DEBUG ){
			Log.d( TAG, String.format("loaded imageData %d ms, parsed %d ms", timeStamp2 - timeStamp , System.currentTimeMillis()- timeStamp2 ) );
		}

		return result;
	}

	public static FilterData getFilterData( Context context ) throws IOException{
		FilterData result = new FilterData();

		// Load html
		final String html = loadHTML( BASE_URI + FILTER_URI );

		// Parse
		Document doc = Jsoup.parse(html);
		Elements colorElements = doc.select( "ul#slideColor" );
		Elements localeElements = doc.select( "ul#slideCountry" );
		Elements seriesElements = doc.select( "ul#slideSeries" );

		result.Colors = parseColors( colorElements.select("li"));
		result.Locales = parseLocales( localeElements.select("li"));
		result.Series = parseSeries( seriesElements.select("li"));

		// TODO: get users from internet
		result.Users = new ItemList();

		return result;
	}

	private static ItemList parseColors(Elements elements){
		return parseList( elements, FILTER_ARG_NAME_COLOR );
	}

	private static ItemList parseLocales(Elements elements){
		return parseList( elements, FILTER_ARG_NAME_LOCALE );
	}

	private static ArrayList< Category > parseSeries( Elements elements ) {
		ArrayList<Category> result = new ArrayList<Category>();

		Pattern catPattern = Pattern.compile( "([^()\\s]+)\\s*\\((\\d+)\\)" );
		Pattern namePattern = Pattern.compile("([^\\(]+)\\((\\d+)\\)(\\s*(.+))?");

		String catName = "";
		for(Element element : elements ){
			String className = element.className();
			if( className.contains("catname") ){
				catName = element.text();
				Matcher catMatcher = catPattern.matcher( catName );
				if( catMatcher.find() ){
					catName = catMatcher.group( 1 );
				}
			}else if( className.contains( "tags" ) ){
				Elements catItems = element.select( "ul > li > a" );
				Category cat = new Category( catName );
				for( Element catItem : catItems ){
					String href = catItem.attr( "href" );
					Matcher hrefMatcher = FILTER_ARG_PATTERN.matcher( href );
					if( hrefMatcher.find() ){
						if( hrefMatcher.group( 2 ).equals(FILTER_ARG_NAME_SERIES) ) {
							String entry = catItem.text();
							String value = hrefMatcher.group(1);

							if (!cat.items.containsValue(value)) {
								Matcher entryMatcher = namePattern.matcher(entry);
								if (entryMatcher.find()) {
									String jpName = entryMatcher.group(1);
									String enName = entryMatcher.group(4);
									entry = jpName;
									if (enName != null && enName.length() > 0)
										entry = enName + "\r\n" + entry;
								}
								cat.items.add(new Item(entry, value));
							}
						}
					}
				}
				Collections.sort(cat.items);
				result.add( cat );
			}
		}
		return result;
	}

	private static ItemList parseList( Elements elements, String argNameCheck ) {
		return parseList(elements, argNameCheck, true);
	}

	private static ItemList parseList( Elements elements, String argNameCheck, boolean sort ){

		final ItemList result = new ItemList();

		for (Element element : elements){
			String value = "";
			String link = element.select( "a" ).attr( "href" );
			Matcher linkArgs = FILTER_ARG_PATTERN.matcher( link );
			if( linkArgs.find() )
				if( linkArgs.group(2).equals( argNameCheck ) )
					value = linkArgs.group( 1 );
			result.add(new Item(element.text(), value));
		}

		if( sort )
			Collections.sort( result );

		return result;
	}

	private static String getThumbnailsListURL( int page, int source ){
		return BASE_URI +
				(( source == THUMBNAIL_SOURCE_NEWEST )?
						THUMBNAIL_NEWEST_LIST_URI :
						THUMBNAIL_CONTENT_LIST_URI)
				+ page;
	}

	private static String getElementText(Elements elements, String query, String defaultValue){
		Elements results = elements.select( query );
		if( results.isEmpty() ){
			return defaultValue;
		}else{
			return results.text();
		}
	}

	private static String loadHTML( final String uri ) throws IOException {
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
