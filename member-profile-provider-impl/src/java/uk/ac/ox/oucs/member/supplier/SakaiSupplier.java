package uk.ac.ox.oucs.member.supplier;

import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import uk.ac.ox.oucs.member.model.MemberProfile;

/**
 * @author Colin Hebert
 */
public class SakaiSupplier implements Supplier
{
	private UserDirectoryService userDirectoryService;

	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	public MemberProfile supplyInformation(MemberProfile memberProfile)
	{
		try
		{
			User user = userDirectoryService.getUser(memberProfile.getId());
			memberProfile.setFirstname(user.getFirstName());
			memberProfile.setLastname(user.getLastName());
			memberProfile.setEmailAddress(user.getEmail());
		}
		catch (UserNotDefinedException e)
		{
			e.printStackTrace(); //TODO: Ignore this exception ?
		}
		return memberProfile;
	}
}
