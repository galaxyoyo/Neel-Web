package galaxyoyo.web.neel.util;

import galaxyoyo.web.neel.User;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.Agent;
import com.mojang.authlib.BaseUserAuthentication;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.authlib.yggdrasil.request.AuthenticationRequest;
import com.mojang.authlib.yggdrasil.request.ValidateRequest;
import com.mojang.authlib.yggdrasil.response.AuthenticationResponse;
import com.mojang.authlib.yggdrasil.response.Response;
import com.mojang.util.UUIDTypeAdapter;

public class Authentications
{
	private static final Map<String, Authentications> auths = Maps.newHashMap();
	
	private User user;
	private final YggdrasilAuthenticationService service;
	private final YggdrasilUserAuthentication auth;
	private YggdrasilMinecraftSessionService session;
	
	public Authentications(User user)
	{
		this.user = user;
		this.service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, this.user.getClientToken());
		this.auth = new YggdrasilUserAuthentication(service, Agent.MINECRAFT);
	}
	
	public void logIn(boolean cracked) throws AuthenticationException
	{
		if (auth.canPlayOnline())
			return;
		
		try
		{
			auth.setUsername(user.getEmail());
			auth.setPassword(user.getPassword());
			auth.logIn();
			user.setAuthenticationToken(auth.getAuthenticatedToken());
			user.setUsername(auth.getSelectedProfile().getName());
			user.setCracked(false);
			user.setUUID(auth.getSelectedProfile().getId());
			session = (YggdrasilMinecraftSessionService) service.createMinecraftSessionService();
		}
		catch (AuthenticationException ex)
		{
			if (!cracked)
				throw ex;
			GameProfile gp = new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + user.getUsername()).getBytes()), user.getUsername());
			method(BaseUserAuthentication.class, auth, "setSelectedProfile", new Class<?>[] {GameProfile.class}, gp);
			setField(auth.getClass(), auth, "accessToken", MoreObjects.firstNonNull(auth.getAuthenticatedToken(), UUID.randomUUID()));
			setField(auth.getClass(), auth, "isOnline", true);
			user.setCracked(true);
		}
	}
	
	public static boolean logIn(String email, String password)
	{
		YggdrasilAuthenticationService serv = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUIDTypeAdapter.fromUUID(UUID.nameUUIDFromBytes(("TEMP CLIENT TOKEN").getBytes(StandardCharsets.UTF_8))));
		YggdrasilUserAuthentication auth = new YggdrasilUserAuthentication(serv, Agent.MINECRAFT);
		AuthenticationRequest req = new AuthenticationRequest(auth, email, password);
		try
		{
			AuthenticationResponse resp = method(serv.getClass(), serv, "makeRequest", new Class<?>[] {URL.class, Object.class, Class.class}, new URL(" https://authserver.mojang.com/authenticate"), req, AuthenticationResponse.class);
			ValidateRequest vreq = new ValidateRequest(auth);
			setField(vreq.getClass(), vreq, "accessToken", resp.getAccessToken());
			method(serv.getClass(), serv, "makeRequest", new Class<?> [] {URL.class, Object.class, Class.class}, new URL(" https://authserver.mojang.com/invalidate"), vreq, Response.class);
			return true;
		}
		catch (Exception ex)
		{
			if (!(ex instanceof AuthenticationException))
				ex.printStackTrace();
			return false;
		}
	}
	
	public boolean isLogged()
	{
		auth.getAuthenticatedToken();
		return auth.isLoggedIn();
	}
	
	public Map<String, Object> getSavableProperties()
	{
		return auth.saveForStorage();
	}
	
	public void refreshProperties(Map<String, Object> props)
	{
		auth.loadFromStorage(props);
	}
	
	public static Authentications getAuth(User user)
	{
		if (!auths.containsKey(user.getClientToken()))
		{
			Authentications auth = new Authentications(user);
			auths.put(user.getClientToken(), auth);
			user.refreshData();
		}
		return auths.get(user.getClientToken());
	}
	
	public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures()
	{
		return session.getTextures(auth.getSelectedProfile(), false);
	}
	
	protected static <T> T method(Class<?> clazz, Object o, String name, Class<?>[] params, Object ... args)
	{
		try
		{
			Method method = clazz.getDeclaredMethod(name, params);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			T ret = (T) method.invoke(o, args);
			method.setAccessible(false);
			return ret;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	protected static void setField(Class<?> clazz, Object o, String name, Object replace)
	{
		try
		{
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			field.set(o, replace);
			field.setAccessible(false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected static <T> T field(Class<?> clazz, Object o, String name)
	{
		try
		{
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			T ret = (T) field.get(o);
			field.setAccessible(false);
			return ret;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
