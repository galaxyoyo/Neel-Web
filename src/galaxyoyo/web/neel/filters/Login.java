package galaxyoyo.web.neel.filters;

import galaxyoyo.web.neel.User;
import galaxyoyo.web.neel.util.Authentications;
import galaxyoyo.web.neel.util.DSUtil;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mojang.authlib.exceptions.AuthenticationException;

public class Login implements Filter
{
	@Override
	public void destroy()
	{
	}
	
	@Override
	public void doFilter(ServletRequest _req, ServletResponse _resp, FilterChain chain) throws IOException, ServletException
	{
		if (!(_req instanceof HttpServletRequest) || !(_resp instanceof HttpServletResponse))
		{
			_resp.getWriter().write("Erreur : seul le HTTP est autoris\u00e9.");
			return;
		}
		
		HttpServletRequest req = (HttpServletRequest) _req;
		HttpServletResponse resp = (HttpServletResponse) _resp;
		
		Cookie clientToken = null, accessToken = null;
		for (Cookie c : req.getCookies())
		{
			if (c.getName().equalsIgnoreCase("clientToken"))
				clientToken = c;
			else if (c.getName().equalsIgnoreCase("accessToken"))
				accessToken = c;
			if (clientToken != null && accessToken != null)
				break;
		}
		
		if (clientToken == null || accessToken == null)
		{
			chain.doFilter(req, resp);
			return;
		}
		
		List<User> users = DSUtil.list(User.class);
		User user = null;
		for (User u : users)
		{
			if (u.getClientToken().equalsIgnoreCase(clientToken.getValue())
							&& u.getAuthenticationToken().equalsIgnoreCase(accessToken.getValue()))
			{
				user = u;
				break;
			}
		}
		
		if (req.getSession().getAttribute("user") != null && user == req.getSession().getAttribute("user"))
		{
			chain.doFilter(req, resp);
			return;
		}
		
		if (user != null)
		{
			try
			{
				Authentications.getAuth(user).logIn(user.isCracked());
				user.saveData();
				req.setAttribute("user", user);
			}
			catch (AuthenticationException e)
			{
				e.printStackTrace();
			}
		}
		
		chain.doFilter(req, resp);
	}
	
	@Override
	public void init(FilterConfig config) throws ServletException
	{
	}	
}
