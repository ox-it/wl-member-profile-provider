package uk.ac.ox.oucs.member.exception;

/**
 * @author Colin Hebert
 */
public class InexistingSiteException extends RuntimeException
{
	public InexistingSiteException()
	{
	}

	public InexistingSiteException(String message)
	{
		super(message);
	}
}
