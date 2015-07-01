package com.mojang.authlib.yggdrasil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.request.JoinMinecraftServerRequest;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.authlib.yggdrasil.response.Response;
import com.mojang.util.UUIDTypeAdapter;

public class YggdrasilMinecraftSessionService extends HttpMinecraftSessionService
{

	private static final String WHITELISTED_DOMAINS[] = {".minecraft.net", ".mojang.com", ".appspot.com"};
	private static final Logger LOGGER = Logger.getLogger("Yggdrasil");
	private static final String BASE_URL = "https://sessionserver.mojang.com/session/minecraft/";
	private static final URL JOIN_URL = HttpAuthenticationService.constantURL(BASE_URL + "join");
	private static final URL CHECK_URL = HttpAuthenticationService.constantURL(BASE_URL + "hasJoined");
	private final PublicKey publicKey;
	private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
	private final LoadingCache<GameProfile, GameProfile> insecureProfiles;

	protected YggdrasilMinecraftSessionService(YggdrasilAuthenticationService authenticationService)
	{
		super (authenticationService);
		insecureProfiles = CacheBuilder.newBuilder().expireAfterWrite(6L, TimeUnit.HOURS).build(new CacheLoader<GameProfile, GameProfile>()
				{
					public GameProfile load(GameProfile key) throws Exception
					{
						return fillGameProfile(key, false);
					}
				});
		try
		{
			X509EncodedKeySpec spec = new X509EncodedKeySpec(
					IOUtils.toByteArray(YggdrasilMinecraftSessionService.class
									.getResourceAsStream("/yggdrasil_session_pubkey.der")));
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			publicKey = keyFactory.generatePublic(spec);
		}
		catch (Exception e)
		{
			throw new Error("Missing/invalid yggdrasil public key!");
		}
	}

	public void joinServer(GameProfile profile, String authenticationToken, String serverId) throws AuthenticationException
	{
		JoinMinecraftServerRequest request = new JoinMinecraftServerRequest();
		request.accessToken = authenticationToken;
		request.selectedProfile = profile.getId();
		request.serverId = serverId;
		try
		{
			getAuthenticationService().makeRequest(JOIN_URL, request, Response.class);
		}
		catch (AuthenticationException ex)
		{
			LOGGER.severe("Non connect\u00e9 \u00e0 minecraft.net, possibilit\u00e9 que cela ne fonctionne pas");
		}
	}

	public GameProfile hasJoinedServer(GameProfile user, String serverId) throws AuthenticationUnavailableException
    {
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("username", user.getName());
        arguments.put("serverId", serverId);
        URL url = HttpAuthenticationService.concatenateURL(CHECK_URL, HttpAuthenticationService.buildQuery(arguments));
        GameProfile result;
        HasJoinedMinecraftServerResponse response;
		try
		{
			response = (HasJoinedMinecraftServerResponse)getAuthenticationService().makeRequest(url, null, HasJoinedMinecraftServerResponse.class);
		}
		catch (AuthenticationException e)
		{
			throw new AuthenticationUnavailableException(e);
		}
        if(response == null || response.getId() == null)
            return null;
        result = new GameProfile(response.getId(), user.getName());
        if(response.getProperties() != null)
            result.getProperties().putAll(response.getProperties());
        return result;
    }

	public Map<Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure)
	{
		Property textureProperty = (Property) Iterables.getFirst(profile.getProperties().get("textures"), null);
		if (textureProperty == null)
		{
			HashMap<Type, MinecraftProfileTexture> hashmap = new HashMap<MinecraftProfileTexture.Type, MinecraftProfileTexture>();
            try
            {
				URL url = new URL(String.format("http://galaxy-yoyo.appspot.com/minecraft/skins/%s/%s.png", "1.8.7", profile.getName()));
				HttpURLConnection co = (HttpURLConnection) url.openConnection();
				co.setRequestMethod("HEAD");
				co.connect();
				if (co.getResponseCode() == HttpURLConnection.HTTP_OK)
					hashmap.put(Type.SKIN, new MinecraftProfileTexture(url.toString(), new HashMap<String, String>()));
				co.disconnect();
				
				url = new URL(String.format("http://galaxy-yoyo.appspot.com/minecraft/capes/%s.png", profile.getName()));
				co = (HttpURLConnection) url.openConnection();
				co.setRequestMethod("HEAD");
				co.connect();
				if (co.getResponseCode() == HttpURLConnection.HTTP_OK)
					hashmap.put(Type.CAPE, new MinecraftProfileTexture(url.toString(), new HashMap<String, String>()));
				co.disconnect();
            }
            catch (IOException e)
            {
				e.printStackTrace();
			}
            
            return hashmap;
		}
		if (requireSecure)
		{
			if (!textureProperty.hasSignature())
			{
				LOGGER.severe("Signature is missing from textures payload");
				throw new InsecureTextureException("Signature is missing from textures payload");
			}
			if (!textureProperty.isSignatureValid(publicKey))
			{
				LOGGER.severe("Textures payload has been tampered with (signature invalid)");
				throw new InsecureTextureException(
						"Textures payload has been tampered with (signature invalid)");
			}
		}
		MinecraftTexturesPayload result;
		try {
			String json = new String(Base64.decodeBase64(textureProperty.getValue()), Charsets.UTF_8);
			result = (MinecraftTexturesPayload) gson.fromJson(json, MinecraftTexturesPayload.class);
		}
		catch (JsonParseException e)
		{
			LOGGER.log(Level.SEVERE, "Could not decode textures payload", e);
			return new HashMap<Type, MinecraftProfileTexture>();
		}
		if (result.getTextures() == null)
			return new HashMap<Type, MinecraftProfileTexture>();
		for (Entry<Type, MinecraftProfileTexture> entry : result.getTextures().entrySet())
		{
			if (!isWhitelistedDomain(entry.getValue().getUrl()))
			{
				LOGGER.severe("Textures payload has been tampered with (non-whitelisted domain)");
				return new HashMap<Type, MinecraftProfileTexture>();
			}
		}

		return result.getTextures();
	}

	public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure)
	{
		if (profile.getId() == null)
			return profile;
		if (!requireSecure)
			return (GameProfile) insecureProfiles.getUnchecked(profile);
		else
			return fillGameProfile(profile, true);
	}

	@SuppressWarnings("null")
	protected GameProfile fillGameProfile(GameProfile profile, boolean requireSecure)
    {
        MinecraftProfilePropertiesResponse response;
        URL url = HttpAuthenticationService.constantURL((new StringBuilder()).append("https://sessionserver.mojang.com/session/minecraft/profile/").append(UUIDTypeAdapter.fromUUID(profile.getId())).toString());
        url = HttpAuthenticationService.concatenateURL(url, (new StringBuilder()).append("unsigned=").append(!requireSecure).toString());
        try
        {
			response = (MinecraftProfilePropertiesResponse)getAuthenticationService().makeRequest(url, null, MinecraftProfilePropertiesResponse.class);
		}
        catch (AuthenticationException e)
        {
			e.printStackTrace();
			LOGGER.warning((new StringBuilder()).append("Couldn't fetch profile properties for ").append(profile).append(" as the profile does not exist").toString());
			return null;
		}
        if(response != null)
            return profile;
        GameProfile result = new GameProfile(response.getId(), response.getName());
        result.getProperties().putAll(response.getProperties());
        profile.getProperties().putAll(response.getProperties());
        LOGGER.warning((new StringBuilder()).append("Successfully fetched profile properties for ").append(profile).toString());
        return result;
    }

	public YggdrasilAuthenticationService getAuthenticationService()
	{
		return (YggdrasilAuthenticationService) super.getAuthenticationService();
	}

	private static boolean isWhitelistedDomain(String url)
	{
		URI uri = null;
		try
		{
			uri = new URI(url);
		} catch (URISyntaxException e)
		{
			throw new IllegalArgumentException("Invalid URL '" + url + "'");
		}
		String domain = uri.getHost();
		for (int i = 0; i < WHITELISTED_DOMAINS.length; i++)
			if (domain.endsWith(WHITELISTED_DOMAINS[i]))
				return true;

		return false;
	}
}
