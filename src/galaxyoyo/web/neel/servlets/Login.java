package galaxyoyo.web.neel.servlets;

import galaxyoyo.web.neel.User;
import galaxyoyo.web.neel.util.Authentications;
import galaxyoyo.web.neel.util.DSUtil;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mojang.authlib.exceptions.AuthenticationException;

@SuppressWarnings("serial")
public class Login extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		getServletContext().getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setContentType("text/html; charset=UTF-8");
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		boolean mojangAccount = false;
		
		try
		{
			new InternetAddress(email);
			mojangAccount = true;
		}
		catch (MessagingException ex)
		{
			mojangAccount = false;
		}
		
		User user = null;
		List<User> users = DSUtil.list(User.class);
		for (User u : users)
		{
			if (mojangAccount)
			{
				if (u.getEmail().equals(email))
				{
					user = u;
					break;
				}
			}
			else
			{
				if (u.getUsername().equals(email))
				{
					user = u;
					break;
				}
			}
		}
		
		if (user == null)
		{
			if (mojangAccount)
				resp.getWriter().write("E-mail inconnu");
			else
				resp.getWriter().write("Pseudo inconnu");
			return;
		}
		
		if (!user.getPassword().equals(password))
		{
			resp.getWriter().write("Mot de passe incorret");
			return;
		}
		
		try
		{
			Authentications.getAuth(user).logIn(user.isCracked());
		}
		catch (AuthenticationException e)
		{
			resp.getWriter().write(e.getMessage());
			resp.getWriter().write("<br />\r\nMot de passe chang\u00e9 ?");
			return;
		}
		
		user.refreshData();
		user.saveData();
		Cookie accessTokenCookie = new Cookie("accessToken", user.getAuthenticationToken());
		resp.addCookie(accessTokenCookie);
		Cookie clientTokenCookie = new Cookie("clientToken", user.getClientToken());
		resp.addCookie(clientTokenCookie);
		req.getSession().setAttribute("user", user);
		resp.getWriter().write("Connexion effectu\u00e9e !");
	}
}
