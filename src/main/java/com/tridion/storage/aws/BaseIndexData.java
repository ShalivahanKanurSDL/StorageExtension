package com.tridion.storage.aws;

/**
 * BaseIndexData.
 *
 * POJO which holds all data necessary for an indexing action
 *
 */
public class BaseIndexData
{

    protected FactoryAction _action;
    protected String _uniqueIndexId;
    protected String _storageId;
    protected IndexType _itemType;
    protected String _publicationId;
    protected String _relativePath;
    protected String _IndexXMl;

    public BaseIndexData(FactoryAction action, IndexType itemType, String publicationId, String storageId,String indexXML)
    {
        this._action = action;
        this._itemType = itemType;
        this._publicationId = publicationId;
        this._storageId = storageId;
        this._IndexXMl=indexXML;
    }

    /**
     * Gets the storage id configured in cd_storage_conf_xml.
     *
     * @return the storage id
     */
    public String getStorageId()
    {
        return this._storageId;
    }

    /**
     * Sets the Indexing action.
     *
     * @param _action the new action
     */
    public void setAction(FactoryAction _action)
    {
        this._action = _action;
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    public FactoryAction getAction()
    {
        return this._action;
    }

    /**
     * Sets the unique index id.
     *
     * @param tcmUri the new unique index id
     */
    public void setUniqueIndexId(String tcmUri)
    {
        this._uniqueIndexId = tcmUri;
    }

    /**
     * Gets the unique index id.
     *
     * @return the unique index id
     */
    public String getUniqueIndexId()
    {
        return this._uniqueIndexId;
    }

    /**
     * Gets the index type.
     *
     * @return the index type
     */
    public IndexType getIndexType()
    {
        return _itemType;
    }

    /**
     * Sets the item type.
     *
     * @param itemType the new item type
     */
    public void setItemType(IndexType itemType)
    {
        this._itemType = itemType;
    }

    /**
     * Gets the publication item id.
     *
     * @return the publication item id
     */
    public String getPublicationItemId()
    {
        return this._publicationId;
    }


    /**
     * Sets the relativePath.
     *
     * @param relativePath the new relativePath
     */
    public void setRelativePath(String relativePath)
    {
        this._relativePath = relativePath;
    }

    /**
     * Gets the relativePath.
     *
     * @return the relativePath
     */
    public String getRelativePath()
    {
        return this._relativePath;
    }


    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
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
                this._storageId +
                "]";
    }
}

