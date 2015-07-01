package galaxyoyo.web.neel.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

public class MCServerUtil
{
	public static Query query() throws IOException
	{
		return query(getServerIP(), getQueryPort());
	}
	
	public static Query query(String address, int port) throws IOException
	{
		return Query.query(address, port);
	}
	
	public static String rcon(String msg) throws IOException
	{
		return rcon(getServerIP(), getQueryPort(), msg);
	}
	
	public static String rcon(String ip, int port, String msg) throws IOException
	{
		URL url = new URL("http://" + ip + ":" + port + "/rcon");
		HttpURLConnection co = (HttpURLConnection) url.openConnection();
		co.setRequestMethod("POST");
		co.setDoOutput(true);
		OutputStream os = co.getOutputStream();
		os.write(("password=neel-opopop&command=" + msg).getBytes(StandardCharsets.UTF_8));
		os.close();
		co.connect();
		String resp = IOUtils.toString(co.getInputStream(), StandardCharsets.UTF_8);
		return resp;
	}
	
	public static String getServerIP()
	{
		return "neelmc.omgcraft.fr";
	}
	
	public static int getServerPort()
	{
		return 12769;
	}
	
	public static int getQueryPort()
	{
		return 40281;
	}
	
	public static class Query
	{
		Query()
		{
		}

		public String hostname;
		public String gametype;
		public String game_id;
		public String version;
		public String plugins;
		public String map;
		public int numplayers;
		public int maxplayers;
		public int hostport;
		public String hostip;
		private final List<String> players = new ArrayList<String>();
		
		static Query query(String ip, int port) throws IOException
		{
			URL url = new URL("http://" + ip + ":" + port + "/query");
			String json = IOUtils.toString(url, "UTF-8");
			return new Gson().fromJson(json, Query.class);
		}
		
		public String getHostname()
		{
			return hostname;
		}
		
		public String getGametype()
		{
			return gametype;
		}
		
		public String getGameId()
		{
			return game_id;
		}
		
		public String getVersion()
		{
			return version;
		}
		
		public String getMotor()
		{
			return plugins.split(":")[0];
		}
		
		public List<String> getPlugins()
		{
			String all = plugins;
			all = all.substring(all.indexOf(':') + 2);
			return Arrays.asList(all.split("; "));
		}
		
		public String getMap()
		{
			return map;
		}
		
		public int getConnectedPlayersNumber()
		{
			return numplayers;
		}
		
		public int getMaxPlayers()
		{
			return maxplayers;
		}
		
		public int getHostPort()
		{
			return hostport;
		}
		
		public String getHostIP()
		{
			return hostip;
		}
		
		public List<String> getConnectedPlayers()
		{
			return new ArrayList<String>(players);
		}
		
		static String readString(byte[] array, AtomicInteger cursor)
		{
			final int pos = cursor.incrementAndGet();
			while (cursor.get() < array.length && array[cursor.get()] != 0)
			{
				cursor.incrementAndGet();
			}
			return new String(Arrays.copyOfRange(array, pos, cursor.get()));
		}
	}
}
