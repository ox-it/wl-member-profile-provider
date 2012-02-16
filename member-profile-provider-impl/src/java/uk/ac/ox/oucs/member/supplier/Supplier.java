package uk.ac.ox.oucs.member.supplier;

import uk.ac.ox.oucs.member.model.MemberProfile;

/**
 * @author Colin Hebert
 */
public interface Supplier
{
	MemberProfile supplyInformation(MemberProfile memberProfile);
}
