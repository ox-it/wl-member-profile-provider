package uk.ac.ox.oucs.member.exception;

/**
 * @author Colin Hebert
 */
public class NotLoggedInException extends RuntimeException
{
	public NotLoggedInException()
	{
	}

	public NotLoggedInException(String message)
	{
		super(message);
	}
}
