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
import org.slf4j.Logger;

import com.tridion.storage.services.LocalThreadTransaction;

/**
 * TridionPageProcessor.
 *
 * @author Shalivahan Kanur
 * * @version 1.0
 *
 */

public class TridionPageProcessor
{
	public static void registerItemAddition(CharacterData page, String originalRelativePath, String newRelativePath, String storageId, Logger log)
	{
		String indexId = "page:" + page.getPublicationId() + "-" + page.getId();
		String fileSize = Integer.toString(page.toString().length());
		String path = originalRelativePath;
		String fileExtension = Utils.GetBinaryFileExtension(newRelativePath);
		CharacterIndexData data = new CharacterIndexData(FactoryAction.PERSIST, IndexType.PAGE, Integer.toString(page.getPublicationId()), storageId,"");

		data.setContent(page);
		data.setUniqueIndexId(indexId);
		data.setFileName(Utils.GetBinaryFileName(path) + "." + Utils.GetBinaryFileExtension(path));
		data.setRelativePath(newRelativePath);
		data.setFileSize(fileSize);
		data.setFileType(fileExtension);

		AWSS3PageIndexProcessor.registerAction(LocalThreadTransaction.getTransactionId(), data, log);
	}

	public static void registerItemRemoval(String indexId, IndexType type, Logger log, String publicationId, String relativePath, String storageId,String removalXML)
	{
		BaseIndexData removalData = new BaseIndexData(FactoryAction.REMOVE, type, publicationId, storageId,removalXML);
		removalData.setUniqueIndexId(indexId);
		removalData.setRelativePath(relativePath);
        AWSS3PageIndexProcessor.registerAction(LocalThreadTransaction.getTransactionId(), removalData, log);
	}
}
