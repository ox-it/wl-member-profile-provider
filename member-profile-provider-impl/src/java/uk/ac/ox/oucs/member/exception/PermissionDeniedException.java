package uk.ac.ox.oucs.member.exception;

/**
 * @author Colin Hebert
 */
public class PermissionDeniedException extends RuntimeException
{
	public PermissionDeniedException()
	{
	}

	public PermissionDeniedException(String message)
	{
		super(message);
	}
}
