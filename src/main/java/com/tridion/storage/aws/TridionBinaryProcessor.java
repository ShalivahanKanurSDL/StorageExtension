package com.tridion.storage.aws;

import com.tridion.storage.BinaryContent;
import com.tridion.storage.services.LocalThreadTransaction;
import org.slf4j.Logger;

/**
 * TridionBinaryProcessor.
 *
 * @author Shalivahan Kanur
 * * @version 1.0
 *
 */

public class TridionBinaryProcessor
{

    public static void registerItemRemoval(String indexId, IndexType type, Logger log, String publicationId, String relativePath, String storageId, String removalXML)
    {
        BaseIndexData removalData = new BaseIndexData(FactoryAction.REMOVE, type, publicationId, storageId,removalXML);
        removalData.setUniqueIndexId(indexId);
        removalData.setRelativePath(relativePath);
        AWSS3BinaryIndexProcessor.registerAction(LocalThreadTransaction.getTransactionId(), removalData);
    }

    /**
     * Register addition of a Binary item.
     *
     * @param binaryContent
     * @param originalRelativePath
     * @param newRelativePath
     * @param storageId
     */
    public static void registerAddition(BinaryContent binaryContent, String originalRelativePath, String newRelativePath, String storageId)
    {
        String indexId = "binary:" + Integer.toString(binaryContent.getPublicationId()) + "-" + Integer.toString(binaryContent.getBinaryId());
        String fileSize = Integer.toString(binaryContent.getObjectSize());
        String path = originalRelativePath;
        String fileExtension = Utils.GetBinaryFileExtension(newRelativePath);
        BinaryIndexData data = new BinaryIndexData(FactoryAction.PERSIST, IndexType.BINARY, Integer.toString(binaryContent.getPublicationId()), storageId,"");

        data.setContent(binaryContent);
        data.setUniqueIndexId(indexId);
        data.setFileName(Utils.GetBinaryFileName(path) + "." + Utils.GetBinaryFileExtension(path));
        data.setRelativePath(newRelativePath);
        data.setFileSize(fileSize);
        data.setFileType(fileExtension);
        AWSS3BinaryIndexProcessor.registerAction(LocalThreadTransaction.getTransactionId(), data);
    }


}
