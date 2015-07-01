package com.mojang.authlib.yggdrasil;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.HttpUserAuthentication;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.yggdrasil.request.AuthenticationRequest;
import com.mojang.authlib.yggdrasil.request.RefreshRequest;
import com.mojang.authlib.yggdrasil.request.ValidateRequest;
import com.mojang.authlib.yggdrasil.response.AuthenticationResponse;
import com.mojang.authlib.yggdrasil.response.RefreshResponse;
import com.mojang.authlib.yggdrasil.response.Response;
import com.mojang.authlib.yggdrasil.response.User;

public class YggdrasilUserAuthentication extends HttpUserAuthentication
{
	
	private static final Logger LOGGER = Logger.getLogger("Yggdrasil");
	private static final String BASE_URL = "https://authserver.mojang.com/";
	private static final URL ROUTE_AUTHENTICATE = HttpAuthenticationService.constantURL(BASE_URL + "authenticate");
	private static final URL ROUTE_REFRESH = HttpAuthenticationService.constantURL(BASE_URL + "refresh");
	private static final URL ROUTE_VALIDATE = HttpAuthenticationService.constantURL(BASE_URL + "validate");
//	private static final URL ROUTE_INVALIDATE = HttpAuthenticationService.constantURL(BASE_URL + "invalidate");
//	private static final URL ROUTE_SIGNOUT = HttpAuthenticationService.constantURL(BASE_URL + "signout");
//	private static final String STORAGE_KEY_ACCESS_TOKEN = "accessToken";
	private final Agent agent;
	private GameProfile profiles[];
	private String accessToken;
	private boolean isOnline;
	
	public YggdrasilUserAuthentication(YggdrasilAuthenticationService authenticationService, Agent agent)
	{
		super(authenticationService);
		this.agent = agent;
	}
	
	public boolean canLogIn()
	{
		return !canPlayOnline() && StringUtils.isNotBlank(getUsername())
						&& (StringUtils.isNotBlank(getPassword()) || StringUtils.isNotBlank(getAuthenticatedToken()));
	}
	
	public void logIn() throws AuthenticationException
	{
		if (StringUtils.isBlank(getUsername()))
			throw new InvalidCredentialsException("Invalid username");
		if (StringUtils.isNotBlank(getAuthenticatedToken()))
			logInWithToken();
		else if (StringUtils.isNotBlank(getPassword()))
			logInWithPassword();
		else
			throw new InvalidCredentialsException("Invalid password");
	}
	
	protected void logInWithPassword() throws AuthenticationException
	{
		if (StringUtils.isBlank(getUsername()))
			throw new InvalidCredentialsException("Invalid username");
		if (StringUtils.isBlank(getPassword()))
			throw new InvalidCredentialsException("Invalid password");
		LOGGER.info("Logging in with username & password");
		AuthenticationRequest request = new AuthenticationRequest(this, getUsername(), getPassword());
		AuthenticationResponse response = (AuthenticationResponse) getAuthenticationService().makeRequest(ROUTE_AUTHENTICATE, request,
						AuthenticationResponse.class);
		if (!response.getClientToken().equals(getAuthenticationService().getClientToken()))
			throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
		if (response.getSelectedProfile() != null)
			setUserType(response.getSelectedProfile().isLegacy() ? UserType.LEGACY : UserType.MOJANG);
		else if (ArrayUtils.isNotEmpty(response.getAvailableProfiles()))
			setUserType(response.getAvailableProfiles()[0].isLegacy() ? UserType.LEGACY : UserType.MOJANG);
		User user = response.getUser();
		if (user != null && user.getId() != null)
			setUserid(user.getId());
		else
			setUserid(getUsername());
		isOnline = true;
		accessToken = response.getAccessToken();
		profiles = response.getAvailableProfiles();
		setSelectedProfile(response.getSelectedProfile());
		getModifiableUserProperties().clear();
		updateUserProperties(user);
	}
	
	protected void updateUserProperties(User user)
	{
		if (user == null)
			return;
		if (user.getProperties() != null)
			getModifiableUserProperties().putAll(user.getProperties());
	}
	
	protected void logInWithToken() throws AuthenticationException
	{
		if (StringUtils.isBlank(getUserID()))
			if (StringUtils.isBlank(getUsername()))
				setUserid(getUsername());
			else
				throw new InvalidCredentialsException("Invalid uuid & username");
		if (StringUtils.isBlank(getAuthenticatedToken()))
			throw new InvalidCredentialsException("Invalid access token");
		LOGGER.info("Logging in with access token");
		if (checkTokenValidity())
		{
			LOGGER.warning("Skipping refresh call as we're safely logged in.");
			isOnline = true;
			return;
		}
		RefreshRequest request = new RefreshRequest(this);
		RefreshResponse response = (RefreshResponse) getAuthenticationService().makeRequest(ROUTE_REFRESH, request,
						RefreshResponse.class);
		if (!response.getClientToken().equals(getAuthenticationService().getClientToken()))
			throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
		if (response.getSelectedProfile() != null)
			setUserType(response.getSelectedProfile().isLegacy() ? UserType.LEGACY : UserType.MOJANG);
		else if (ArrayUtils.isNotEmpty(response.getAvailableProfiles()))
			setUserType(response.getAvailableProfiles()[0].isLegacy() ? UserType.LEGACY : UserType.MOJANG);
		if (response.getUser() != null && response.getUser().getId() != null)
			setUserid(response.getUser().getId());
		else
			setUserid(getUsername());
		isOnline = true;
		accessToken = response.getAccessToken();
		profiles = response.getAvailableProfiles();
		setSelectedProfile(response.getSelectedProfile());
		getModifiableUserProperties().clear();
		updateUserProperties(response.getUser());
	}
	
	protected boolean checkTokenValidity() throws AuthenticationException
    {
		try
		{
	        ValidateRequest request = new ValidateRequest(this);
	        getAuthenticationService().makeRequest(ROUTE_VALIDATE, request, Response.class);
	        return true;
		}
		catch (AuthenticationException ex)
		{
			return false;
		}
    }
	
	public void logOut()
	{
		super.logOut();
		accessToken = null;
		profiles = null;
		isOnline = false;
	}
	
	public GameProfile[] getAvailableProfiles()
	{
		return profiles;
	}
	
	public boolean isLoggedIn()
	{
		return StringUtils.isNotBlank(accessToken);
	}
	
	public boolean canPlayOnline()
	{
		return isLoggedIn() && getSelectedProfile() != null && isOnline;
	}
	
	public void selectGameProfile(GameProfile profile) throws AuthenticationException
	{
		if (!isLoggedIn())
			throw new AuthenticationException("Cannot change game profile whilst not logged in");
		if (getSelectedProfile() != null)
			throw new AuthenticationException("Cannot change game profile. You must log out and back in.");
		if (profile == null || !ArrayUtils.contains(profiles, profile))
			throw new IllegalArgumentException((new StringBuilder()).append("Invalid profile '").append(profile).append("'").toString());
		RefreshRequest request = new RefreshRequest(this, profile);
		RefreshResponse response = (RefreshResponse) getAuthenticationService().makeRequest(ROUTE_REFRESH, request,
						RefreshResponse.class);
		if (!response.getClientToken().equals(getAuthenticationService().getClientToken()))
		{
			throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
		}
		else
		{
			isOnline = true;
			accessToken = response.getAccessToken();
			setSelectedProfile(response.getSelectedProfile());
			return;
		}
	}
	
	public void loadFromStorage(Map<String, Object> credentials)
	{
		super.loadFromStorage(credentials);
		accessToken = String.valueOf(credentials.get("accessToken"));
	}
	
	public Map<String, Object> saveForStorage()
	{
		Map<String, Object> result = super.saveForStorage();
		if (StringUtils.isNotBlank(getAuthenticatedToken()))
			result.put("accessToken", getAuthenticatedToken());
		return result;
	}
	
	/**
	 * @deprecated Method getSessionToken is deprecated
	 */
	
	public String getSessionToken()
	{
		if (isLoggedIn() && getSelectedProfile() != null && canPlayOnline())
			return String.format("token:%s:%s", new Object[] {getAuthenticatedToken(), getSelectedProfile().getId()});
		else
			return null;
	}
	
	public String getAuthenticatedToken()
	{
		return accessToken;
	}
	
	public Agent getAgent()
	{
		return agent;
	}
	
	public String toString()
	{
		return (new StringBuilder()).append("YggdrasilAuthenticationService{agent=").append(agent).append(", profiles=").append(Arrays.toString(profiles))
						.append(", selectedProfile=").append(getSelectedProfile()).append(", username='").append(getUsername()).append('\'')
						.append(", isLoggedIn=").append(isLoggedIn()).append(", userType=").append(getUserType()).append(", canPlayOnline=")
						.append(canPlayOnline()).append(", accessToken='").append(accessToken).append('\'').append(", clientToken='")
						.append(getAuthenticationService().getClientToken()).append('\'').append('}').toString();
	}
	
	public YggdrasilAuthenticationService getAuthenticationService()
	{
		return (YggdrasilAuthenticationService) super.getAuthenticationService();
	}
	
}
