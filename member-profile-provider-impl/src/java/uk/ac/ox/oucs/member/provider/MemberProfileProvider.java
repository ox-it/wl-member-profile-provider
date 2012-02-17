package uk.ac.ox.oucs.member.provider;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import uk.ac.ox.oucs.member.model.MemberProfile;
import uk.ac.ox.oucs.member.supplier.MemberProvider;
import uk.ac.ox.oucs.member.supplier.Supplier;

/**
 * @author Colin Hebert
 */
public class MemberProfileProvider extends AbstractEntityProvider implements Resolvable, Outputable
{
	private MemberProvider memberProvider;
	private List<Supplier> suppliers;

	public void setMemberProvider(MemberProvider memberProvider)
	{
		this.memberProvider = memberProvider;
	}

	public void setSuppliers(List<Supplier> suppliers)
	{
		this.suppliers = suppliers;
	}

	public String getEntityPrefix()
	{
		return "memberProfile";
	}

	public Collection<MemberProfile> getEntity(EntityReference ref)
	{
		String siteId = ref.getId();
		return getMembersProfiles(siteId);
	}

	private Collection<MemberProfile> getMembersProfiles(String siteId)
	{
		Collection<MemberProfile> profiles = memberProvider.getMembers(siteId);
		for (MemberProfile profile : profiles)
		{
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

	public String[] getHandledOutputFormats()
	{
		return new String[]{Formats.XML, Formats.JSON};
	}
}
