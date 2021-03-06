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

import java.util.ArrayList;
import java.util.Collection;

/**
 * ItemList
 */
public  class ItemList extends ArrayList< Item > {

	public ItemList(  ){
		super( );
	}

	public ItemList( Collection< Item > collection ){
		super( collection );
	}

	public String[] getValues(){
		final String[] result = new String[ this.size() ];
		for( int ii = 0; ii < this.size(); ++ii ){
			result[ ii ] = this.get( ii ).value;
		}
		return result;
	}

	public String[] getEntries(){
		final String[] result = new String[ this.size() ];
		for( int ii = 0; ii < this.size(); ++ii ){
			result[ ii ] = this.get( ii ).entry;
		}
		return result;
	}

	public boolean containsValue( String value ){
		for( Item item : this ){
			if( item.value.equals( value ) )
				return true;
		}
		return false;
	}
}