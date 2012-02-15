package uk.ac.ox.oucs.member.provider;

import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

/**
 * @author Colin Hebert
 */
public class MemberProfileProvider extends AbstractEntityProvider implements ActionsExecutable
{
	public String getEntityPrefix()
	{
		return "memberProfile";
	}
}
