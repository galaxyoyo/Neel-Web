package galaxyoyo.web.neel;

import galaxyoyo.web.neel.util.Authentications;
import galaxyoyo.web.neel.util.DSUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import com.google.appengine.api.datastore.Blob;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.util.UUIDTypeAdapter;

@Entity
public class User
{
	@Id
	private String id;
	private String username;
	private String email;
	private Blob password;
	private String authenticationToken;
	private Map<String, Object> savableAuthProperties = new HashMap<String, Object>();
	private boolean cracked;
	
	private boolean emailVerified = false;
	private String emailToken;
	
	private Grade grade = Grade.HABITANT;
	private int coins = 0;
	
	public UUID getUUID()
	{
		return UUIDTypeAdapter.fromString(id);
	}
	
	public void setUUID(UUID uuid)
	{
		String id = UUIDTypeAdapter.fromUUID(uuid);
		if (this.id != null && !this.id.isEmpty())
			DSUtil.delete(User.class, this.id);
		this.id = id;
		DSUtil.save(this);
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getPassword()
	{
		return new String(password.getBytes(), StandardCharsets.UTF_8);
	}
	
	public void setPassword(String password)
	{
		this.password = new Blob(password.getBytes(StandardCharsets.UTF_8));
	}
	
	public String getAuthenticationToken()
	{
		return authenticationToken;
	}
	
	public void setAuthenticationToken(String accessToken)
	{
		this.authenticationToken = accessToken;
	}
	
	public String getClientToken()
	{
		return UUIDTypeAdapter.fromUUID(UUID.nameUUIDFromBytes(("Client token of " + username).getBytes(StandardCharsets.UTF_8)));
	}
	
	public void saveData()
	{
		savableAuthProperties.clear();
		savableAuthProperties.putAll(Authentications.getAuth(this).getSavableProperties());
		DSUtil.save(this);
	}
	
	public void refreshData()
	{
		Authentications.getAuth(this).refreshProperties(savableAuthProperties);
	}
	
	public boolean isCracked()
	{
		return cracked;
	}
	
	public void setCracked(boolean crack)
	{
		this.cracked = crack;
	}
	
	public boolean isEmailVerified()
	{
		return emailVerified;
	}
	
	public void verifiyEmail(boolean verify)
	{
		this.emailVerified = verify;
	}
	
	public String getEmailToken()
	{
		return emailToken;
	}
	
	public void setEmailToken(String token)
	{
		this.emailToken = token;
	}
	
	public int getCoins()
	{
		return coins;
	}
	
	public void addCoins(int amount)
	{
		coins += amount;
	}
	
	public void setCoins(int amount)
	{
		coins = amount;
	}
	
	public void pay(int amount)
	{
		coins -= amount;
	}
	
	public Grade getGrade()
	{
		return grade;
	}
	
	public void upgrade(Grade grade)
	{
		this.grade = grade;
	}
	
	public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures()
	{
		return Authentications.getAuth(this).getTextures();
	}
	
	@Ignore
	private byte[] skin;
	public byte[] getSkin()
	{
		if (skin != null)
			return skin;
		MinecraftProfileTexture texture = getTextures().get(MinecraftProfileTexture.Type.SKIN);
		if (texture == null)
			return new byte[0];
		try
		{
			skin = IOUtils.toByteArray(new URL(texture.getUrl()));
		}
		catch (IOException ex)
		{
			skin = new byte[0];
		}
		return skin;
	}
	
	@Ignore
	private byte[] cape;
	public byte[] getCape()
	{
		if (cape != null)
			return cape;
		MinecraftProfileTexture texture = getTextures().get(MinecraftProfileTexture.Type.CAPE);
		if (texture == null || texture.getUrl() == null)
			return new byte[0];
		try
		{
			cape = IOUtils.toByteArray(new URL(texture.getUrl()));
		}
		catch (IOException ex)
		{
			cape = new byte[0];
		}
		return cape;
	}
	
	public static enum Grade
	{
		HABITANT("Habitant"), NOBLE("Noble"), DIGNE("Digne"), ROI("Roi"), LEGENDAIRE("L\u00e9gendaire"),
		DIEU("Dieu"), CHUCKNORRIS("Chuck Norris"), MODO("Mod\u00e9rateur"), ADMIN("Admin");
		
		private final String display;
		
		Grade(String disp)
		{
			display = disp;
		}
		
		public String getId()
		{
			return name().toLowerCase();
		}
		
		public String getDisplay()
		{
			return display;
		}
		
		public static Grade getById(String id)
		{
			for (Grade grade : values())
			{
				if (grade.getId().equalsIgnoreCase(id))
					return grade;
			}
			return null;
		}
		
		public static Grade getByDisplay(String display)
		{
			for (Grade grade : values())
			{
				if (grade.getDisplay().equalsIgnoreCase(display))
					return grade;
			}
			return null;
		}
		
		public static Grade get(String name)
		{
			Grade grade;
			if ((grade = getById(name)) != null)
				return grade;
			else if ((grade = getByDisplay(name)) != null)
				return grade;
			return null;
		}
	}
	
	public static enum Job
	{
		CHOMEUR(""), WOODCUTTER("Bûcheron"), MINER("Mineur"), HUNTER("Chasseur"), BLACKSMITH("Forgeron"),
		CARPENTER("Menuisier"), STONECUTTER("Tailleur de pierre"), MASON("Maçon"), FARMER("Agriculteur / Éleveur");
		
		private final String display;
		
		Job(String disp)
		{
			display = disp;
		}
		
		public String getId()
		{
			return name().toLowerCase();
		}
		
		public String getDisplay()
		{
			return display;
		}
		
		public static Job getById(String id)
		{
			for (Job grade : values())
			{
				if (grade.getId().equalsIgnoreCase(id))
					return grade;
			}
			return null;
		}
		
		public static Job getByDisplay(String display)
		{
			for (Job grade : values())
			{
				if (grade.getDisplay().equalsIgnoreCase(display))
					return grade;
			}
			return null;
		}
		
		public static Job get(String name)
		{
			Job grade;
			if ((grade = getById(name)) != null)
				return grade;
			else if ((grade = getByDisplay(name)) != null)
				return grade;
			return null;
		}
	}
}
