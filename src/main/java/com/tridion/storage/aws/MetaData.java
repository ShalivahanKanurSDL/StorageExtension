/**
 * Copyright 2011-2013 Radagio & SDL
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

package com.tridion.storage.aws;

/**
 * BaseIndexData.
 * 
 * POJO which holds all data necessary for an indexing action
 * 
 *
 */
public class MetaData
{

	protected int _recordCount;
	protected String _globaleCategory;
	protected String _locale;
		
	public MetaData(String globaleCategory,String locale,int recordCount)
	{
		this._globaleCategory = globaleCategory;
		this._locale=locale;
		this._recordCount=recordCount;
	}

	/**
	 * Gets the storage id configured in cd_storage_conf_xml.
	 * 
	 * @return the storage id
	 */
	public String getGlobaleCategory()
	{
		return this._globaleCategory;
	}

	/**
	 * Gets the publication item id.
	 * 
	 * @return the publication item id
	 */
	public String getLocaleId()
	{
		return this._locale;
	}
		
	/**
	 * Gets the publication item id.
	 * 
	 * @return the publication item id
	 */
	public int getRecordCount()
	{
		return this._recordCount;
	}
		
	
	/**
	 * Gets the storage id configured in cd_storage_conf_xml.
	 * 
	 * @return the storage id
	 */
	public void  setGlobaleCategory(String globalCategory)
	{
	 this._globaleCategory=globalCategory;
	}

	/**
	 * Gets the publication item id.
	 * 
	 * @return the publication item id
	 */
	public void  setLocaleId(String locale)
	{
		this._locale=locale;
	}
		
	/**
	 * Gets the publication item id.
	 * 
	 * @return the publication item id
	 */
	public void  setRecordCount(int count)
	{
		this._recordCount=count;
	}
		
}
