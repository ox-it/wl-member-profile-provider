package uk.ac.ox.oucs.member.supplier;

import java.util.Collection;
import java.util.Collections;

import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.Person;
import uk.ac.ox.oucs.member.model.MemberProfile;

/**
 * @author Colin Hebert
 */
public class Profile2Supplier implements Supplier
{
	private boolean overrideInfo;
	private ProfileLogic profileLogic;

	public void setProfileLogic(ProfileLogic profileLogic)
	{
		this.profileLogic = profileLogic;
	}

	public void setOverrideInfo(boolean overrideInfo)
	{
		this.overrideInfo = overrideInfo;
	}

	public void supplyInformation(MemberProfile memberProfile)
	{
		//TODO : Use the actual profile API
		// Collection<Person> connections = profileService.getConnectionsForUser(memberProfile.getId());
		Collection<Person> connections = Collections.singleton(profileLogic.getPerson(memberProfile.getId()));
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
	}

	private void addUnexistingInformation(MemberProfile memberProfile, Person person)
	{
		if (memberProfile.getEmailAddress() == null)
			memberProfile.setEmailAddress(person.getProfile().getEmail());
		if (memberProfile.getPhotoUrl() == null)
			memberProfile.setPhotoUrl(person.getProfile().getImageUrl());
		if (memberProfile.getPhoneNumber() == null)
			memberProfile.setPhoneNumber(person.getProfile().getWorkphone());
		if (memberProfile.getDescription() == null)
			memberProfile.setDescription(person.getProfile().getPersonalSummary());
	}

	private void overrideExistingInformation(MemberProfile memberProfile, Person person)
	{
		if (person.getProfile().getEmail() != null)
			memberProfile.setEmailAddress(person.getProfile().getEmail());
		if (person.getProfile().getImageUrl() != null)
			memberProfile.setPhotoUrl(person.getProfile().getImageUrl());
		if (person.getProfile().getWorkphone() != null)
			memberProfile.setPhoneNumber(person.getProfile().getWorkphone());
		if (person.getProfile().getPersonalSummary() != null)
			memberProfile.setDescription(person.getProfile().getPersonalSummary());
	}
}
