package com.mojang.authlib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

public abstract class HttpAuthenticationService extends BaseAuthenticationService
{
	
	private static final Logger LOGGER = Logger.getLogger("Yggdrasil");
	private final Proxy proxy;
	
	protected HttpAuthenticationService(Proxy proxy)
	{
		Validate.notNull(proxy);
		this.proxy = proxy;
	}
	
	public Proxy getProxy()
	{
		return proxy;
	}
	
	protected HttpURLConnection createUrlConnection(URL url) throws IOException
	{
		Validate.notNull(url);
		LOGGER.warning((new StringBuilder()).append("Opening connection to ").append(url).toString());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
		connection.setConnectTimeout(15000);
		connection.setReadTimeout(15000);
		connection.setUseCaches(false);
		return connection;
	}
	
	public String performPostRequest(URL url, String post, String contentType) throws IOException
	{
		Validate.notNull(url);
		Validate.notNull(post);
		Validate.notNull(contentType);
		HttpURLConnection connection = createUrlConnection(url);
		byte[] postAsBytes = post.getBytes(Charsets.UTF_8);
		connection.setRequestProperty("Content-Type", (new StringBuilder()).append(contentType).append("; charset=utf-8").toString());
		connection.setRequestProperty("Content-Length", (new StringBuilder()).append("").append(postAsBytes.length).toString());
		connection.setDoOutput(true);
		LOGGER.warning((new StringBuilder()).append("Writing POST data to ").append(url).append(": ").append(post).toString());
		OutputStream outputStream = connection.getOutputStream();
		try
		{
			IOUtils.write(postAsBytes, outputStream);
			IOUtils.closeQuietly(outputStream);
		}
		catch (Exception exception)
		{
			IOUtils.closeQuietly(outputStream);
			throw exception;
		}
		LOGGER.warning((new StringBuilder()).append("Reading data from ").append(url).toString());
		InputStream inputStream = connection.getInputStream();
		try
		{
			String result = IOUtils.toString(inputStream, Charsets.UTF_8);
			LOGGER.warning((new StringBuilder()).append("Successful read, server response was ").append(connection.getResponseCode()).toString());
			LOGGER.warning((new StringBuilder()).append("Response: ").append(result).toString());
			IOUtils.closeQuietly(inputStream);
			return result;
		}
		catch (IOException e)
		{
			IOUtils.closeQuietly(inputStream);
		}
		inputStream = connection.getErrorStream();
		if (inputStream == null)
			return null;
		try
		{
			LOGGER.warning((new StringBuilder()).append("Reading error page from ").append(url).toString());
			String result = IOUtils.toString(inputStream, Charsets.UTF_8);
			LOGGER.warning((new StringBuilder()).append("Successful read, server response was ").append(connection.getResponseCode()).toString());
			LOGGER.warning((new StringBuilder()).append("Response: ").append(result).toString());
			IOUtils.closeQuietly(inputStream);
			return result;
		}
		catch (IOException e)
		{
			LOGGER.log(Level.WARNING, "Request failed", e);
			throw e;
		}
		catch (Exception exception1)
		{
			IOUtils.closeQuietly(inputStream);
			throw exception1;
		}
	}
	
	public String performGetRequest(URL url) throws IOException
	{
		Validate.notNull(url);
		HttpURLConnection connection = createUrlConnection(url);
		LOGGER.warning((new StringBuilder()).append("Reading data from ").append(url).toString());
		InputStream inputStream = connection.getInputStream();
		try
		{
			String result = IOUtils.toString(inputStream, Charsets.UTF_8);
			LOGGER.warning((new StringBuilder()).append("Successful read, server response was ").append(connection.getResponseCode()).toString());
			LOGGER.warning((new StringBuilder()).append("Response: ").append(result).toString());
			IOUtils.closeQuietly(inputStream);
			return result;
		}
		catch (IOException e)
		{
			IOUtils.closeQuietly(inputStream);
			inputStream = connection.getErrorStream();
			if (inputStream == null)
				return null;
			LOGGER.warning((new StringBuilder()).append("Reading error page from ").append(url).toString());
			try
			{
				String result = IOUtils.toString(inputStream, Charsets.UTF_8);
				LOGGER.warning((new StringBuilder()).append("Successful read, server response was ").append(connection.getResponseCode()).toString());
				LOGGER.warning((new StringBuilder()).append("Response: ").append(result).toString());
				IOUtils.closeQuietly(inputStream);
				return result;
			}
			catch (IOException e1)
			{
				LOGGER.log(Level.WARNING, "Request failed", e);
				throw e1;
			}
			catch (Exception e2)
			{
				IOUtils.closeQuietly(inputStream);
				throw e2;
			}
		}
	}
	
	public static URL constantURL(String url)
	{
		try
		{
			return new URL(url);
		}
		catch (MalformedURLException ex)
		{
			throw new Error((new StringBuilder()).append("Couldn't create constant for ").append(url).toString(), ex);
		}
	}
	
	public static String buildQuery(Map<String, String> query)
	{
		if (query == null)
			return "";
		StringBuilder builder = new StringBuilder();
		Iterator<Entry<String, String>> i$ = query.entrySet().iterator();
		do
		{
			if (!i$.hasNext())
				break;
			Entry<String, String> entry = i$.next();
			if (builder.length() > 0)
				builder.append('&');
			try
			{
				builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			}
			catch (UnsupportedEncodingException e)
			{
				LOGGER.log(Level.SEVERE, "Unexpected exception building query", e);
			}
			if (entry.getValue() != null)
			{
				builder.append('=');
				try
				{
					builder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
				}
				catch (UnsupportedEncodingException e)
				{
					LOGGER.log(Level.SEVERE, "Unexpected exception building query", e);
				}
			}
		}
		while (true);
		return builder.toString();
	}
	
	public static URL concatenateURL(URL url, String query)
	{
		try
		{
			if (url.getQuery() != null && url.getQuery().length() > 0)
				return new URL(url.getProtocol(), url.getHost(), url.getPort(), (new StringBuilder()).append(url.getFile()).append("&").append(query)
								.toString());
			return new URL(url.getProtocol(), url.getHost(), url.getPort(), (new StringBuilder()).append(url.getFile()).append("?").append(query).toString());
		}
		catch (MalformedURLException ex)
		{
			throw new IllegalArgumentException("Could not concatenate given URL with GET arguments!", ex);
		}
	}
}
