package uk.ac.ox.oucs.member.supplier;

import java.util.Collection;

import uk.ac.ox.oucs.member.model.MemberProfile;

/**
 * @author Colin Hebert
 */
public interface MemberProvider
{
	Collection<MemberProfile> getMembers(String siteId);

	Collection<MemberProfile> getMembersWithRole(String siteId, String roleId);
}
