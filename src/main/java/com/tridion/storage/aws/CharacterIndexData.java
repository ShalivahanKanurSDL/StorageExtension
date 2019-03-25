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

import com.tridion.data.CharacterData;
import com.tridion.storage.PageContent;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CharacterIndexData.--> Which is used to get the Pages from JPAPageDAO
 * 
 * POJO which holds all data necessary for an indexing action
 * 
 */
public class CharacterIndexData extends BaseIndexData
{
	private String _fileName;
	private String _fileSize;
	private String _fileType;
	private CharacterData _content;
	private String _url;

	private ConcurrentHashMap<String, ArrayList<Object>> _indexFields = new ConcurrentHashMap<String, ArrayList<Object>>();

	public CharacterIndexData(FactoryAction action, IndexType itemType, String publicationId, String storageId,String searchDataXml)
	{
		super(action, itemType, publicationId, storageId,searchDataXml);
	}

//	public void addIndexField(String name, Object value)
//	{
//		ArrayList<Object> field = this._indexFields.get(name);
//		if (field == null)
//		{
//			field = new ArrayList<Object>();
//		}
//		field.add(value);
//		this._indexFields.put(name, field);
//	}
//
//	public ConcurrentHashMap<String, ArrayList<Object>> getIndexFields()
//	{
//		return this._indexFields;
//	}
//
//	public ArrayList<Object> getIndexField(String name)
//	{
//		return this._indexFields.get(name);
//	}
//
//	public Object getIndexField(String name, int index)
//	{
//		ArrayList<Object> list = this._indexFields.get(name);
//		if (index < list.size() && index > 0)
//		{
//			return list.get(index);
//		}
//		return null;
//	}

	public int getFieldSize()
	{
		return this._indexFields.size();
	}
	/**
	 * Gets the file size.
	 *
	 * @return the file size
	 */
	public String getFileSize()
	{
		return _fileSize;
	}

	/**
	 * Sets the file size.
	 *
	 * @param _fileSize the new file size
	 */
	public void setFileSize(String _fileSize)
	{
		this._fileSize = _fileSize;
	}

	/**
	 * Gets the file type.
	 *
	 * @return the file type
	 */
	public String getFileType()
	{
		return _fileType;
	}

	/**
	 * Sets the file type.
	 *
	 * @param _fileType the new file type
	 */
	public void setFileType(String _fileType)
	{
		this._fileType = _fileType;
	}

	/**
	 * Gets the content.
	 *
	 * @return the content
	 */
	public CharacterData getContent()
	{
		return _content;
	}

	/**
	 * Sets the content.
	 *
	 * @param _content the new content
	 */
	public void setContent(CharacterData _content)
	{
		this._content = _content;
	}

	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName()
	{
		return _fileName;
	}

	/**
	 * Sets the file name.
	 *
	 * @param _fileName the new file name
	 */
	public void setFileName(String _fileName)
	{
		this._fileName = _fileName;
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String get_url()
	{
		return _url;
	}

	/**
	 * Sets the url.
	 *
	 * @param _url the new file type
	 */
	public void set_url(String _url)
	{
		this._url = _url;
	}

	/*
	 * (non-Javadoc)
	 * @see com.tridion.storage.extensions.xml.BaseIndexData#toString()
	 */
	@Override
	public String toString()
	{
		return "[" +
				this._action +
				"," +
				this._uniqueIndexId +
				"," +
				this._itemType +
				"," +
				this._fileName +
				"," +
				this._fileSize +
				"," +
				this._url +
				"]";
	}
}
