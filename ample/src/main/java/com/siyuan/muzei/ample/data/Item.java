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

import java.io.Serializable;

/**
 * Item
 */
public class Item implements Comparable< Item >, Serializable {
	public Item( String entry ){
		this.entry =  entry;
		this.value = "";
	}
	public Item( String entry, String value ){
		this.entry =  entry;
		this.value = value;
	}
	public String entry;
	public String value;

	public int compareTo(Item t){
		return this.entry.compareTo( t.entry );
	}
}