package uk.ac.ox.oucs.member.provider;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.List;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import uk.ac.ox.oucs.member.exception.InexistingSiteException;
import uk.ac.ox.oucs.member.exception.NotLoggedInException;
import uk.ac.ox.oucs.member.exception.PermissionDeniedException;
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
		return "site-members";
	}

	public Collection<MemberProfile> getEntity(EntityReference ref)
	{
		String siteId = ref.getId();
		return getMembersProfiles(siteId);
	}

	private Collection<MemberProfile> getMembersProfiles(String siteId)
	{
		Collection<MemberProfile> profiles;
		try
		{
			profiles = memberProvider.getMembers(siteId);
		}
		catch (InexistingSiteException e)
		{
			throw new EntityException("Site not found", siteId, HttpURLConnection.HTTP_NOT_FOUND);
		}
		catch (PermissionDeniedException e)
		{
			throw new EntityException("Unauthorised access", siteId, HttpURLConnection.HTTP_UNAUTHORIZED);
		}
		catch (NotLoggedInException e)
		{
			throw new EntityException("User not logged-in", siteId, HttpURLConnection.HTTP_FORBIDDEN);
		}

		for (MemberProfile profile : profiles)
		{
			//Load info from other modules
			for (Supplier supplier : suppliers)
			{
				try
				{
					supplier.supplyInformation(profile);
				}
				catch (Exception ignore)
				{
					//Nothing should interrupt this call
				}
			}
		}

		return profiles;
	}

	public String[] getHandledOutputFormats()
	{
		return new String[]{Formats.XML, Formats.JSON};
	}
}
