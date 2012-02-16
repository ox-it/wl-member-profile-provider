package uk.ac.ox.oucs.member.supplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import uk.ac.ox.oucs.member.model.MemberProfile;

/**
 * @author Colin Hebert
 */
public class KernelMemberProvider implements MemberProvider
{
	private SiteService siteService;


	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public Collection<MemberProfile> getMembers(String siteId)
	{
		Collection<Member> members = getSiteMembers(siteId);
		Collection<MemberProfile> profiles = new ArrayList<MemberProfile>(members.size());
		for (Member member : members)
		{
			profiles.add(getProfileFromMember(member));
		}

		return profiles;
	}

	public Collection<MemberProfile> getMembersWithRole(String siteId, String roleId)
	{
		return getMembers(siteId); //TODO, filter roles
	}

	private Collection<Member> getSiteMembers(String siteId)
	{
		Collection<Member> members;
		try
		{
			//TODO: Check access rights !
			members = siteService.getSite(siteId).getMembers();
		}
		catch (IdUnusedException e)
		{
			members = Collections.emptyList();
			e.printStackTrace(); //TODO throw a nice exception
		}

		return members;
	}

	private MemberProfile getProfileFromMember(Member member)
	{
		MemberProfile memberProfile = new MemberProfile();
		memberProfile.setId(member.getUserId());
		memberProfile.setRole(member.getRole().getId());

		return memberProfile;
	}
}
