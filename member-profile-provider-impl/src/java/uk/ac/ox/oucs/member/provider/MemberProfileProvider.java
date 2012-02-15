package uk.ac.ox.oucs.member.provider;

import java.util.Collections;
import java.util.List;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import uk.ac.ox.oucs.member.model.MemberProfile;

/**
 * @author Colin Hebert
 */
public class MemberProfileProvider extends AbstractEntityProvider implements ActionsExecutable, Resolvable
{
	public String getEntityPrefix()
	{
		return "memberProfile";
	}

	public Object getEntity(EntityReference ref)
	{
		List<MemberProfile> memberProfiles = Collections.emptyList();
		String siteId = ref.getId();

		return memberProfiles;
	}

}
