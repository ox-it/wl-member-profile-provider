package uk.ac.ox.oucs.member.supplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import uk.ac.ox.oucs.member.model.MemberProfile;

/**
 * @author Colin Hebert
 */
public class RosterMemberProvider implements MemberProvider
{
	private AuthzGroupService authzGroupService;
	private PrivacyManager privacyManager;
	private SecurityService securityService;
	private SiteService siteService;
	private UserDirectoryService userDirectoryService;

	//TODO: Replace with an actual service once roster is fixed? See SAK-21818
	private RosterService rosterService = new RosterService();

	public void setAuthzGroupService(AuthzGroupService authzGroupService)
	{
		this.authzGroupService = authzGroupService;
	}

	public void setPrivacyManager(PrivacyManager privacyManager)
	{
		this.privacyManager = privacyManager;
	}

	public void setSecurityService(SecurityService securityService)
	{
		this.securityService = securityService;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	public void setRosterService(RosterService rosterService)
	{
		this.rosterService = rosterService;
	}

	public Collection<MemberProfile> getMembers(String siteId)
	{
		Collection<Participant> participants = getSiteParticipants(siteId);
		Collection<MemberProfile> profiles = new ArrayList<MemberProfile>(participants.size());
		for (Participant participant : participants)
		{
			profiles.add(getProfileFromParticipant(participant));
		}

		return profiles;

	}

	public Collection<MemberProfile> getMembersWithRole(String siteId, String roleId)
	{
		return getMembers(siteId); //TODO, filter roles?
	}

	private Collection<Participant> getSiteParticipants(String siteId)
	{
		List<Participant> participants;
		User currentUser = userDirectoryService.getCurrentUser();

		//Check permission to list users
		if (!securityService.unlock(currentUser, SiteService.SECURE_VIEW_ROSTER, siteId))
			participants = rosterService.getSiteRoster(siteId);
		else
			participants = Collections.emptyList();
		return participants;
	}

	private MemberProfile getProfileFromParticipant(Participant participant)
	{
		MemberProfile memberProfile = new MemberProfile();
		memberProfile.setId(participant.getUser().getId());
		memberProfile.setRole(participant.getRoleTitle());

		return memberProfile;
	}

//------------------------------
//COPY OF ROSTER IMPLEMENTATION (a dragon sleeps here)
//------------------------------

	private class RosterService
	{
		public List<Participant> getSiteRoster(String siteId)
		{
			List<Participant> participants;
			User currentUser = userDirectoryService.getCurrentUser();
			boolean viewAllInSite = userHasSitePermission(currentUser,
					RosterFunctions.ROSTER_FUNCTION_VIEWALL, siteId);

			Map<Group, Set<String>> groupMembers = getGroupMembers(siteId);

			// Users with "viewall" see everybody
			if (viewAllInSite)
			{
				participants = getParticipantsInSite(siteId);
			}
			else
			{
				participants = getParticipantsInGroups(currentUser, groupMembers, siteId);
			}

			filterHiddenUsers(currentUser, participants, groupMembers, siteId);
			return participants;
		}

		/**
		 * Gets a Map of the groups in this site (key) to the user IDs for the members in the group (value)
		 *
		 * @return
		 */
		private Map<Group, Set<String>> getGroupMembers(String siteId)
		{
			Map<Group, Set<String>> groupMembers = new HashMap<Group, Set<String>>();
			Site site;
			try
			{
				site = siteService.getSite(siteId);
			}
			catch (IdUnusedException ide)
			{
				return groupMembers;
			}
			Collection<Group> groups = site.getGroups();
			for (Group group : groups)
			{
				Set<String> userIds = new HashSet<String>();
				Set<Member> members = group.getMembers();
				for (Member member : members)
				{
					userIds.add(member.getUserId());
				}
				groupMembers.put(group, userIds);
			}
			return groupMembers;
		}

		private void filterHiddenUsers(User currentUser, List<Participant> participants, Map<Group, Set<String>> groupMembers, String siteId)
		{
			// If the user has view hidden in the site, don't filter anyone out
			if (userHasSitePermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN, siteId))
			{
				return;
			}

			// Keep track of the users for which the current user has the group-scoped view hidden permission
			Set<String> visibleMembersForCurrentUser = new HashSet<String>();
			for (Map.Entry<Group, Set<String>> e : groupMembers.entrySet())
			{
				if (userHasGroupPermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN, e.getKey().getReference()))
				{
					visibleMembersForCurrentUser.addAll(e.getValue());
				}
			}

			// Iterate through the participants, removing the hidden ones that are not in visibleMembersForCurrentUser
			Set<String> userIds = new HashSet<String>();
			for (Participant participant : participants)
			{
				userIds.add(participant.getUser().getId());
			}

			Set<String> hiddenUsers = privacyManager.findHidden("/site/" + siteId, userIds);

			for (Iterator<Participant> iter = participants.iterator(); iter.hasNext(); )
			{
				Participant participant = iter.next();
				String userId = participant.getUser().getId();
				if (hiddenUsers.contains(userId) && !visibleMembersForCurrentUser.contains(userId))
				{
					iter.remove();
				}
			}
		}

		private List<Participant> getParticipantsInSite(String siteId)
		{
			Map<String, UserRole> userMap = getUserRoleMap(getSiteReference(siteId));
			return buildParticipantList(userMap, userMap.keySet());
		}

		private List<Participant> getParticipantsInGroups(User currentUser, Map<Group, Set<String>> groupMembers, String siteId)
		{
			boolean userHasSiteViewAll = userHasSitePermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWALL, siteId);
			Set<String> viewableUsers = new HashSet<String>();

			for (Map.Entry<Group, Set<String>> e : groupMembers.entrySet())
			{
				if (userHasGroupPermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWALL, e.getKey().getReference())
						|| userHasSiteViewAll)
				{
					viewableUsers.addAll(e.getValue());
				}
			}

			// Build the list of participants

			// Use the site reference because we need to display the site role, not the group role
			Map<String, UserRole> userMap = getUserRoleMap(getSiteReference(siteId));
			return buildParticipantList(userMap, viewableUsers);
		}

		private List<Participant> buildParticipantList(Map<String, UserRole> userMap, Collection<String> profilesMap)
		{
			List<Participant> participants = new ArrayList<Participant>();

			for (String userId : profilesMap)
			{
				UserRole userRole = userMap.get(userId);

				// Profiles may exist for users that have been removed.  If there's a profile
				// for a missing user, skip the profile.  See SAK-10936
				if (userRole == null || userRole.user == null)
				{
					continue;
				}

				participants.add(new ParticipantImpl(userRole.user, userRole.role));
			}
			return participants;
		}


		class UserRole
		{
			User user;
			String role;

			UserRole(User user, String role)
			{
				this.user = user;
				this.role = role;
			}
		}

		private Map<String, UserRole> getUserRoleMap(String authzRef)
		{
			Map<String, UserRole> userMap = new HashMap<String, UserRole>();
			Set<String> userIds = new HashSet<String>();
			Set<Member> members;

			// Get the member set
			try
			{
				members = authzGroupService.getAuthzGroup(authzRef).getMembers();
			}
			catch (GroupNotDefinedException e)
			{
				return userMap;
			}

			// Build a map of userId to role
			Map<String, String> roleMap = new HashMap<String, String>();
			for (Member member : members)
			{
				if (member.isActive())
				{
					// SAK-17286 Only list users that are 'active' not 'inactive'
					userIds.add(member.getUserId());
					roleMap.put(member.getUserId(), member.getRole().getId());
				}
			}

			// Get the user objects
			List<User> users = userDirectoryService.getUsers(userIds);
			for (User user : users)
			{
				String role = roleMap.get(user.getId());
				userMap.put(user.getId(), new UserRole(user, role));
			}
			return userMap;
		}

		private boolean userHasSitePermission(User user, String permissionName, String siteId)
		{
			if (user == null || permissionName == null)
				return false;

			String siteReference = getSiteReference(siteId);
			return securityService.unlock(user, permissionName, siteReference);
		}

		private boolean userHasGroupPermission(User user, String permissionName, String groupReference)
		{
			if (user == null || permissionName == null || groupReference == null)
				return false;
			return authzGroupService.isAllowed(user.getId(), permissionName, groupReference);
		}

		private String getSiteReference(String siteId)
		{
			return siteService.siteReference(siteId);
		}

		private class ParticipantImpl implements Participant
		{
			protected User user;
			protected String roleTitle;
			protected String groupsString;

			/**
			 * Constructs a ParticipantImpl.
			 *
			 * @param user
			 * @param roleTitle
			 */
			public ParticipantImpl(User user, String roleTitle)
			{
				this.user = user;
				this.roleTitle = roleTitle;
			}

			public Profile getProfile()
			{
				return null;
			}


			public String getRoleTitle()
			{
				return roleTitle;
			}

			public void setRoleTitle(String roleTitle)
			{
				this.roleTitle = roleTitle;
			}

			public User getUser()
			{
				return user;
			}

			public void setUser(User user)
			{
				this.user = user;
			}

			public boolean isProfilePhotoPublic()
			{
				return false;
			}

			public boolean isOfficialPhotoPreferred()
			{
				return false;
			}

			public boolean isOfficialPhotoPublicAndPreferred()
			{
				return false;
			}

			public String getGroupsString()
			{
				return null;
			}
		}
	}
}
