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
	private ProfileLogic profileLogic;
	private Behaviour behaviour = Behaviour.ADD_UNEXISTING;

	public void setProfileLogic(ProfileLogic profileLogic)
	{
		this.profileLogic = profileLogic;
	}

	public void setBehaviour(Behaviour behaviour)
	{
		this.behaviour = behaviour;
	}

	public void supplyInformation(MemberProfile memberProfile)
	{
		//TODO : Use the actual profile API
		// Collection<Person> connections = profileService.getConnectionsForUser(memberProfile.getId());
		Collection<Person> connections = Collections.singleton(profileLogic.getPerson(memberProfile.getId()));
		for (Person connection : connections)
		{
			switch (behaviour)
			{
				case OVERRIDE:
					overrideExistingInformation(memberProfile, connection);
					break;
				case CONTACTENATE:
					concatenateExistingInformation(memberProfile, connection);
					break;
				case ADD_UNEXISTING:
				default:
					addUnexistingInformation(memberProfile, connection);
			}
		}
	}

	private void concatenateExistingInformation(MemberProfile memberProfile, Person person)
	{
		memberProfile.setEmailAddress(concatenateNullSafe(memberProfile.getEmailAddress(), person.getProfile().getEmail()));
		memberProfile.setPhotoUrl(concatenateNullSafe(memberProfile.getPhotoUrl(), person.getProfile().getImageUrl()));
		memberProfile.setPhoneNumber(concatenateNullSafe(memberProfile.getPhoneNumber(), person.getProfile().getWorkphone()));
		memberProfile.setDescription(concatenateNullSafe(memberProfile.getDescription(), person.getProfile().getPersonalSummary()));
	}

	private String concatenateNullSafe(String previousValue, String addedValue)
	{
		if (previousValue == null)
			return addedValue;
		if (addedValue == null)
			return previousValue;
		return previousValue + ", " + addedValue;
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

	public static enum Behaviour
	{
		OVERRIDE,
		ADD_UNEXISTING,
		CONTACTENATE
	}
}
