package com.tridion.storage.aws;

import com.tridion.storage.BinaryContent;

/**
 * BinaryIndexData.
 */
public class BinaryIndexData extends BaseIndexData
{
    private String _fileName;
    private String _fileSize;
    private String _fileType;
    private BinaryContent _content;
    private String _url;

    public BinaryIndexData(FactoryAction action, IndexType itemType, String publicationId, String storageId, String removalXML)
    {
        super(action, itemType, publicationId, storageId,"");
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
    public BinaryContent getContent()
    {
        return _content;
    }

    /**
     * Sets the content.
     *
     * @param _content the new content
     */
    public void setContent(BinaryContent _content)
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
     * @see com.tridion.storage.extensions.search.BaseIndexData#toString()
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