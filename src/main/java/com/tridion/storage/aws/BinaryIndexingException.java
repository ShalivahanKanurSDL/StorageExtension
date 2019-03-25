package com.tridion.storage.aws;

/**
 * BinaryIndexingException.
 *
 */
public class BinaryIndexingException extends Exception
{

    private static final long serialVersionUID = 7380395957713446857L;

    public BinaryIndexingException()
    {

    }

    public BinaryIndexingException(String message)
    {
        super(message);
    }

    public BinaryIndexingException(Throwable cause)
    {
        super(cause);
    }

    public BinaryIndexingException(String message, Throwable cause)
    {
        super(message, cause);
    }

}

