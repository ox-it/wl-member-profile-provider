package uk.ac.ox.oucs.member.supplier;

import java.util.Collection;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import uk.ac.ox.oucs.member.exception.InexistingSiteException;
import uk.ac.ox.oucs.member.exception.NotLoggedInException;
import uk.ac.ox.oucs.member.exception.PermissionDeniedException;
import uk.ac.ox.oucs.member.model.MemberProfile;

/**
 * @author Colin Hebert
 */
public abstract class AbstractMemberProvider implements MemberProvider
{
	private SecurityService securityService;
	private UserDirectoryService userDirectoryService;
	private SiteService siteService;

	public SecurityService getSecurityService()
	{
		return securityService;
	}

	public void setSecurityService(SecurityService securityService)
	{
		this.securityService = securityService;
	}

	public SiteService getSiteService()
	{
		return siteService;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public UserDirectoryService getUserDirectoryService()
	{
		return userDirectoryService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	public Collection<MemberProfile> getMembers(String siteId)
	{
		checkPermissions(siteId);
		return getMembersSecure(siteId);
	}

	public Collection<MemberProfile> getMembersWithRole(String siteId, String roleId)
	{
		checkPermissions(siteId);
		return getMembersWithRoleSecure(siteId, roleId);
	}

	private void checkPermissions(String siteId) throws NotLoggedInException, PermissionDeniedException
	{
		User currentUser = userDirectoryService.getCurrentUser();
		if (currentUser.equals(userDirectoryService.getAnonymousUser()))
			throw new NotLoggedInException();

		if (!siteService.siteExists(siteId))
			throw new InexistingSiteException();

		//Check permission to list users
		if (!securityService.unlock(currentUser, SiteService.SECURE_VIEW_ROSTER, siteId))
			throw new PermissionDeniedException(currentUser.getDisplayName() + " can't access site " + siteId);
	}

	protected abstract Collection<MemberProfile> getMembersSecure(String siteId);

	protected abstract Collection<MemberProfile> getMembersWithRoleSecure(String siteId, String roleId);
}
