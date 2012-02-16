package uk.ac.ox.oucs.member.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import uk.ac.ox.oucs.member.model.MemberProfile;
import uk.ac.ox.oucs.member.supplier.Supplier;

/**
 * @author Colin Hebert
 */
public class MemberProfileProvider extends AbstractEntityProvider implements Resolvable, Outputable
{
	private SiteService siteService;
	private List<Supplier> suppliers;

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void setSuppliers(List<Supplier> suppliers)
	{
		this.suppliers = suppliers;
	}

	public String getEntityPrefix()
	{
		return "memberProfile";
	}

	public Object getEntity(EntityReference ref)
	{
		String siteId = ref.getId();
		Collection<MemberProfile> memberProfiles = getMembersProfiles(siteId);
		return memberProfiles;
	}

	private Collection<MemberProfile> getMembersProfiles(String siteId)
	{
		Collection<Member> members = getSiteMembers(siteId);
		Collection<MemberProfile> profiles = new ArrayList<MemberProfile>(members.size());
		for (Member member : members)
		{
			MemberProfile profile = getProfileFromMember(member);

			//Load info from other modules
			for (Supplier supplier : suppliers)
			{
				try
				{
					profile = supplier.supplyInformation(profile);
				}
				catch (Exception ignore)
				{
					//Nothing should interrupt this call
				}
			}

			profiles.add(profile);
		}

		return profiles;
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

	public String[] getHandledOutputFormats()
	{
		return new String[]{Formats.XML, Formats.JSON};
	}
}
