package galaxyoyo.web.neel.filters;

import galaxyoyo.web.neel.util.MCServerUtil;
import galaxyoyo.web.neel.util.MCServerUtil.Query;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class QueryFilter implements Filter
{
	@Override
	public void destroy()
	{
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
	{
		req.setAttribute("server-ip", MCServerUtil.getServerIP());
		req.setAttribute("server-port", MCServerUtil.getServerPort());
		
		try
		{
			Query query = MCServerUtil.query();
			req.setAttribute("server-online", true);
			req.setAttribute("server-query", query);
		}
		catch (IOException ex)
		{
			req.setAttribute("server-online", false);
		}
		
		chain.doFilter(req, resp);
	}
	
	@Override
	public void init(FilterConfig config) throws ServletException
	{
	}
}
