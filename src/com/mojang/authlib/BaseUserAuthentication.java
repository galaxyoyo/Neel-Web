package com.mojang.authlib;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public abstract class BaseUserAuthentication implements UserAuthentication
{
	
	private static final Logger LOGGER = Logger.getLogger("Yggdrasil");
	protected static final String STORAGE_KEY_PROFILE_NAME = "displayName";
	protected static final String STORAGE_KEY_PROFILE_ID = "uuid";
	protected static final String STORAGE_KEY_PROFILE_PROPERTIES = "profileProperties";
	protected static final String STORAGE_KEY_USER_NAME = "username";
	protected static final String STORAGE_KEY_USER_ID = "userid";
	protected static final String STORAGE_KEY_USER_PROPERTIES = "userProperties";
	private final AuthenticationService authenticationService;
	private final PropertyMap userProperties = new PropertyMap();
	private String userid;
	private String username;
	private String password;
	private GameProfile selectedProfile;
	private UserType userType;
	
	protected BaseUserAuthentication(AuthenticationService authenticationService)
	{
		Validate.notNull(authenticationService);
		this.authenticationService = authenticationService;
	}
	
	public boolean canLogIn()
	{
		return !canPlayOnline() && StringUtils.isNotBlank(getUsername()) && StringUtils.isNotBlank(getPassword());
	}
	
	public void logOut()
	{
		password = null;
		userid = null;
		setSelectedProfile(null);
		getModifiableUserProperties().clear();
		setUserType(null);
	}
	
	public boolean isLoggedIn()
	{
		return getSelectedProfile() != null;
	}
	
	public void setUsername(String username)
	{
		if (isLoggedIn() && canPlayOnline())
		{
			throw new IllegalStateException("Cannot change username whilst logged in & online");
		}
		else
		{
			this.username = username;
			return;
		}
	}
	
	public void setPassword(String password)
	{
		if (isLoggedIn() && canPlayOnline() && StringUtils.isNotBlank(password))
		{
			throw new IllegalStateException("Cannot set password whilst logged in & online");
		}
		else
		{
			this.password = password;
			return;
		}
	}
	
	protected String getUsername()
	{
		return username;
	}
	
	protected String getPassword()
	{
		return password;
	}
	
	@SuppressWarnings("unchecked")
	public void loadFromStorage(Map<String, Object> credentials)
	{
		logOut();
		setUsername(String.valueOf(credentials.get("username")));
		if (credentials.containsKey("userid"))
			userid = String.valueOf(credentials.get("userid"));
		else
			userid = username;
		if (credentials.containsKey("userProperties"))
			try
			{
				List<Map<String, String>> list = (List<Map<String, String>>) credentials.get("userProperties");
				for (Iterator<Map<String, String>> i$ = list.iterator(); i$.hasNext();)
				{
					Map<String, String> propertyMap = i$.next();
					String name = propertyMap.get("name");
					String value = propertyMap.get("value");
					String signature = propertyMap.get("signature");
					if (signature == null)
						getModifiableUserProperties().put(name, new Property(name, value));
					else
						getModifiableUserProperties().put(name, new Property(name, value, signature));
				}
				
			}
			catch (Throwable t)
			{
				LOGGER.log(Level.WARNING, "Couldn't deserialize user properties", t);
			}
		if (credentials.containsKey("displayName") && credentials.containsKey("uuid"))
		{
			GameProfile profile = new GameProfile(UUIDTypeAdapter.fromString(String.valueOf(credentials.get("uuid"))), String.valueOf(credentials
							.get("displayName")));
			if (credentials.containsKey("profileProperties"))
				try
				{
					List<Map<String, String>> list = (List<Map<String, String>>) credentials.get("profileProperties");
					for (Iterator<Map<String, String>> i$ = list.iterator(); i$.hasNext();)
					{
						Map<String, String> propertyMap = i$.next();
						String name = propertyMap.get("name");
						String value = propertyMap.get("value");
						String signature = propertyMap.get("signature");
						if (signature == null)
							profile.getProperties().put(name, new Property(name, value));
						else
							profile.getProperties().put(name, new Property(name, value, signature));
					}
					
				}
				catch (Throwable t)
				{
					LOGGER.log(Level.WARNING, "Couldn't deserialize profile properties", t);
				}
			setSelectedProfile(profile);
		}
	}
	
	public Map<String, Object> saveForStorage()
	{
		Map<String, Object> result = new HashMap<String, Object>();
		if (getUsername() != null)
			result.put("username", getUsername());
		if (getUserID() != null)
			result.put("userid", getUserID());
		else if (getUsername() != null)
			result.put("username", getUsername());
		if (!getUserProperties().isEmpty())
		{
			List<Map<String, String>> properties = new ArrayList<Map<String, String>>();
			Map<String, String> property;
			for (Iterator<Property> i$ = getUserProperties().values().iterator(); i$.hasNext(); properties.add(property))
			{
				Property userProperty = i$.next();
				property = new HashMap<String, String>();
				property.put("name", userProperty.getName());
				property.put("value", userProperty.getValue());
				property.put("signature", userProperty.getSignature());
			}
			
			result.put("userProperties", properties);
		}
		GameProfile selectedProfile = getSelectedProfile();
		if (selectedProfile != null)
		{
			result.put("displayName", selectedProfile.getName());
			result.put("uuid", selectedProfile.getId());
			List<Map<String, String>> properties = new ArrayList<Map<String, String>>();
			Map<String, String> property;
			for (Iterator<Property> i$ = selectedProfile.getProperties().values().iterator(); i$.hasNext(); properties.add(property))
			{
				Property profileProperty = i$.next();
				property = new HashMap<String, String>();
				property.put("name", profileProperty.getName());
				property.put("value", profileProperty.getValue());
				property.put("signature", profileProperty.getSignature());
			}
			
			if (!properties.isEmpty())
				result.put("profileProperties", properties);
		}
		return result;
	}
	
	protected void setSelectedProfile(GameProfile selectedProfile)
	{
		this.selectedProfile = selectedProfile;
	}
	
	public GameProfile getSelectedProfile()
	{
		return selectedProfile;
	}
	
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append(getClass().getSimpleName());
		result.append("{");
		if (isLoggedIn())
		{
			result.append("Logged in as ");
			result.append(getUsername());
			if (getSelectedProfile() != null)
			{
				result.append(" / ");
				result.append(getSelectedProfile());
				result.append(" - ");
				if (canPlayOnline())
					result.append("Online");
				else
					result.append("Offline");
			}
		}
		else
		{
			result.append("Not logged in");
		}
		result.append("}");
		return result.toString();
	}
	
	public AuthenticationService getAuthenticationService()
	{
		return authenticationService;
	}
	
	public String getUserID()
	{
		return userid;
	}
	
	public PropertyMap getUserProperties()
	{
		if (isLoggedIn())
		{
			PropertyMap result = new PropertyMap();
			result.putAll(getModifiableUserProperties());
			return result;
		}
		else
		{
			return new PropertyMap();
		}
	}
	
	protected PropertyMap getModifiableUserProperties()
	{
		return userProperties;
	}
	
	public UserType getUserType()
	{
		if (isLoggedIn())
			return userType != null ? userType : UserType.LEGACY;
		else
			return null;
	}
	
	protected void setUserType(UserType userType)
	{
		this.userType = userType;
	}
	
	protected void setUserid(String userid)
	{
		this.userid = userid;
	}
	
}
