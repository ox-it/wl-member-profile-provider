package uk.ac.ox.oucs.member.supplier;

import java.util.ArrayList;
import java.util.Collection;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.exception.IdUnusedException;
import uk.ac.ox.oucs.member.model.MemberProfile;

/**
 * @author Colin Hebert
 */
public class KernelMemberProvider extends AbstractMemberProvider
{
	public Collection<MemberProfile> getMembersSecure(String siteId)
	{
		Collection<Member> members = getSiteMembers(siteId);
		Collection<MemberProfile> profiles = new ArrayList<MemberProfile>(members.size());
		for (Member member : members)
		{
			profiles.add(getProfileFromMember(member));
		}

		return profiles;
	}

	public Collection<MemberProfile> getMembersWithRoleSecure(String siteId, String roleId)
	{
		return getMembers(siteId); //TODO, filter roles
	}

	private Collection<Member> getSiteMembers(String siteId)
	{
		try
		{
			return getSiteService().getSite(siteId).getMembers();
		}
		catch (IdUnusedException ignore)
		{
			//Can't happen as it's checked in AbstractMemberProvider
			throw new RuntimeException(ignore);
		}
	}

	private MemberProfile getProfileFromMember(Member member)
	{
		MemberProfile memberProfile = new MemberProfile();
		memberProfile.setId(member.getUserId());
		memberProfile.setRole(member.getRole().getId());

		return memberProfile;
	}
}
