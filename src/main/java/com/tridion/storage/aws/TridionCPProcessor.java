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
import com.tridion.storage.ComponentPresentation;
import com.tridion.storage.services.LocalThreadTransaction;
import org.slf4j.Logger;

/**
 * TridionCPProcessor.
 *
 * @author Shalivahan Kanur
 * * @version 1.0
 *
 */

public class TridionCPProcessor
{
	public static void registerItemAddition(ComponentPresentationIndexData data, Logger log)
	{
//		String indexId = "cp:" + cp.getPublicationId() + "-" + cp.getComponentId();
//		String fileSize = Integer.toString(cp.toString().length());
//		//String path = originalRelativePath;
//		String fileExtension = Utils.GetBinaryFileExtension(cp.toString());
//		ComponentPresentationIndexData data = new ComponentPresentationIndexData(FactoryAction.PERSIST, IndexType.PAGE, Integer.toString(cp.getPublicationId()), storageId,"");
//
//		data.setContent(cp);
//		data.setUniqueIndexId(indexId);
//		data.setFileName(Utils.GetBinaryFileName(cp.getContent().toString()) + "." + Utils.GetBinaryFileExtension(cp.getContent().toString()));
//		//data.setRelativePath(newRelativePath);
//		data.setFileSize(fileSize);
//		data.setFileType(fileExtension);

		AWSS3CPIndexProcessor.registerAction(LocalThreadTransaction.getTransactionId(), data, log);
	}

	public static void registerItemRemoval(String indexId, IndexType type, Logger log, String publicationId, String storageId,String removalXML)
	{
		BaseIndexData removalData = new BaseIndexData(FactoryAction.REMOVE, type, publicationId, storageId,removalXML);
		removalData.setUniqueIndexId(indexId);
		//removalData.setRelativePath(relativePath);
		AWSS3CPIndexProcessor.registerAction(LocalThreadTransaction.getTransactionId(), removalData, log);
	}
}
