package galaxyoyo.web.neel.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MCServerUtil
{
	public static Query query(String address, int port) throws IOException
	{
		return Query.query(address, port);
	}
	
	public static class Query
	{
		Query()
		{
		}
		
		private final Map<String, String> values = new HashMap<String, String>();
		private final List<String> players = new ArrayList<String>();
		
		static Query query(String ip, int port) throws IOException
		{
			Query query = new Query();
			InetSocketAddress address = new InetSocketAddress(ip, port);
			final DatagramSocket socket = new DatagramSocket();
			
			try
			{
				final byte[] receivedData = new byte[10240];
				socket.setSoTimeout(2000);
				sendPacket(socket, address, 0xFE, 0xFD, 0x09, 0x01, 0x01, 0x01, 0x01);
				final int challengeInteger;
				receivePacket(socket, receivedData);
				byte b = -1;
				int i = 0;
				byte[] buffer = new byte[11];
				for (int count = 5; (b = receivedData[count++]) != 0;)
					buffer[++i] = b;
				challengeInteger = Integer.parseInt(new String(buffer).trim());
				sendPacket(socket, address, 0xFE, 0xFD, 0x00, 0x01, 0x01, 0x01,
								0x01, challengeInteger >> 24, challengeInteger >> 16,
								challengeInteger >> 8, challengeInteger, 0x00, 0x00, 0x00,
								0x00);
				final int length = receivePacket(socket, receivedData).getLength();
				final AtomicInteger cursor = new AtomicInteger(5);
				while (cursor.get() < length)
				{
					String key = readString(receivedData, cursor);
					if (key.isEmpty())
						break;
					String value = readString(receivedData, cursor);
					query.values.put(key, value);
				}
				readString(receivedData, cursor);
				while (cursor.get() < length)
				{
					final String player = readString(receivedData, cursor);
					if (!player.isEmpty())
						query.players.add(player);
				}
			}
			finally
			{
				socket.close();
			}
			
			return query;
		}
		
		public String getHostname()
		{
			return values.get("hostname");
		}
		
		public String getGametype()
		{
			return values.get("gametype");
		}
		
		public String getGameId()
		{
			return values.get("game_id");
		}
		
		public String getVersion()
		{
			return values.get("version");
		}
		
		public String getMotor()
		{
			return values.get("plugins").split(":")[0];
		}
		
		public List<String> getPlugins()
		{
			String all = values.get("plugins");
			all = all.substring(all.indexOf(':') + 2);
			return Arrays.asList(all.split("; "));
		}
		
		public String getMap()
		{
			return values.get("map");
		}
		
		public int getConnectedPlayersNumber()
		{
			return Integer.parseInt(values.get("numplayers"));
		}
		
		public int getMaxPlayers()
		{
			return Integer.parseInt(values.get("maxplayers"));
		}
		
		public int getHostPort()
		{
			return Integer.parseInt(values.get("hostport"));
		}
		
		public String getHostIP()
		{
			return values.get("hostip");
		}
		
		public List<String> getConnectedPlayers()
		{
			return new ArrayList<String>(players);
		}
		
		static void sendPacket(DatagramSocket socket, InetSocketAddress targetAddress, byte... data) throws IOException
		{
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, targetAddress.getAddress(), targetAddress.getPort());
			socket.send(sendPacket);
		}
		
		static void sendPacket(DatagramSocket socket, InetSocketAddress targetAddress, int... data) throws IOException
		{
			final byte[] d = new byte[data.length];
			int i = 0;
			for(int j : data)
				d[i++] = (byte)(j & 0xFF);
			sendPacket(socket, targetAddress, d);
		}
		
		static DatagramPacket receivePacket(DatagramSocket socket, byte[] buffer) throws IOException
		{
			final DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			socket.receive(dp);
			return dp;
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
