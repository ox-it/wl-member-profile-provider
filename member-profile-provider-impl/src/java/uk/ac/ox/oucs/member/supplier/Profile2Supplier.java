package uk.ac.ox.oucs.member.supplier;

import java.util.Collection;

import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.service.ProfileService;
import uk.ac.ox.oucs.member.model.MemberProfile;

/**
 * @author Colin Hebert
 */
public class Profile2Supplier implements Supplier
{
	private ProfileService profileService;
	private boolean overrideInfo;

	public void setProfileService(ProfileService profileService)
	{
		this.profileService = profileService;
	}

	public void setOverrideInfo(boolean overrideInfo)
	{
		this.overrideInfo = overrideInfo;
	}

	public MemberProfile supplyInformation(MemberProfile memberProfile)
	{
		Collection<Person> connections = profileService.getConnectionsForUser(memberProfile.getId());
		for (Person connection : connections)
		{
			if (overrideInfo)
			{
				overrideExistingInformation(memberProfile, connection);
			}
			else
			{
				addUnexistingInformation(memberProfile, connection);
			}
		}
		return memberProfile;
	}

	private void addUnexistingInformation(MemberProfile memberProfile, Person person)
	{
		if (memberProfile.getEmailAddress() == null)
			memberProfile.setEmailAddress(person.getProfile().getEmail());
		if (memberProfile.getPhotoUrl() == null)
			memberProfile.setPhotoUrl(person.getProfile().getImageUrl());
		if (memberProfile.getPhoneNumber() == null)
			memberProfile.setPhoneNumber(person.getProfile().getWorkphone());
	}

	private void overrideExistingInformation(MemberProfile memberProfile, Person person)
	{
		if (person.getProfile().getEmail() != null)
			memberProfile.setEmailAddress(person.getProfile().getEmail());
		if (person.getProfile().getImageUrl() != null)
			memberProfile.setPhotoUrl(person.getProfile().getImageUrl());
		if (person.getProfile().getWorkphone() != null)
			memberProfile.setPhoneNumber(person.getProfile().getWorkphone());
	}
}
