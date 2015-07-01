package galaxyoyo.web.neel.servlets;

import galaxyoyo.web.neel.User;
import galaxyoyo.web.neel.util.Authentications;
import galaxyoyo.web.neel.util.MCServerUtil;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.util.UUIDTypeAdapter;

@SuppressWarnings("serial")
public class Register extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		getServletContext().getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		String confirmPassword = req.getParameter("confirm_password");
		boolean mojangAccount = req.getParameter("mojangAccount") != null;
		String username = req.getParameter("username");
		
		if ((email == null || password == null || confirmPassword == null)
						|| (mojangAccount && username != null))
		{
			resp.sendRedirect(req.getServletPath());
			resp.getWriter().write("Something is null, that's an error");
			return;
		}
		
		if (!password.equals(confirmPassword))
		{
			resp.getWriter().write("Mot de passe diff\u00e9rents");
			return;
		}
		
		try
		{
			new InternetAddress(email);
		}
		catch (MessagingException ex)
		{
			resp.getWriter().write("Adresse e-mail incorrecte");
			return;
		}
		
		User user;
		if (mojangAccount)
		{
			if (Authentications.logIn(email, password))
			{
				user = new User();
				user.setEmail(email);
				user.setPassword(password);
				try
				{
					Authentications.getAuth(user).logIn(false);
				}
				catch (AuthenticationException ex)
				{
					// Will be very strange ...
					throw new ServletException(ex);
				}
			}
			else
			{
				resp.getWriter().write("Combinaison e-mail / mot de passe invalide");
				return;
			}
		}
		else
		{
			String checkSHAResp = MCServerUtil.rcon("checkSHAPassword " + username + " " + DigestUtils.sha1Hex(password));
			if (checkSHAResp.contains("doesn't exist"))
				resp.getWriter().write("Ce compte n'existe pas !");
			else if (checkSHAResp.contains("Mojang"))
				resp.getWriter().write("Ce compte existe, mais utilise Mojang ! Merci d'utiliser votre compte Mojang");
			else if (checkSHAResp.contains("Invalid"))
				resp.getWriter().write("Mot de passe invalide");
			
			if (checkSHAResp.contains("OK") || checkSHAResp.contains("help"))
			{
				user = new User();
				user.setEmail(email);
				user.setUsername(username);
				user.setPassword(password);
				try
				{
					Authentications.getAuth(user).logIn(true);
				}
				catch (AuthenticationException ex)
				{
					// Will be very strange ...
					throw new ServletException(ex);
				}
			}
			else
			{
				resp.getWriter().write(checkSHAResp);
				return;
			}
		}
		
		Random random = new Random();
		String a = Long.toHexString(random.nextLong());
		while (a.length() < 16)
			a = Integer.toHexString(random.nextInt(16)) + a;
		String b = Long.toHexString(random.nextLong());
		while (b.length() < 16)
			b = Integer.toHexString(random.nextInt(16)) + b;
		String token = a + b;
		user.verifiyEmail(false);
		user.setEmailToken(token);
		user.saveData();
		try
		{
			MimeMessage mail = new MimeMessage(Session.getDefaultInstance(new Properties()));
			mail.setFrom(new InternetAddress("inscription@neel-mc.appspotmail.com", "Inscription - Neel"));
			mail.setRecipient(RecipientType.TO, new InternetAddress(email, user.getUsername()));
			mail.setSubject("Validation d'inscription sur le site de Neel", "UTF-8");
			mail.setSentDate(new Date());
			String text = "<div style=\"text-align: justify;\"><p>Bonjour,<br /><br />Vous avez r\u00e9cemment fait";
			text += " une demande pour vous inscrire sur le site et nous vous en remercions. Bon je ne vais pas vous";
			text += " r\u00e9p\u00e9ter tout le blabla que j'ai pu vous faire pour le recrutement, mais vous ";
			text += "<strong>devez</strong> valider votre compte en cliquant sur le lien suivant :</p>";
			text += "<p><a href=\"https://neel-mc.appspot.com/validate-account?uid=" + UUIDTypeAdapter.fromUUID(user.getUUID());
			text += "&token=" + token + "\">https://neel-mc.appspot.com/validate-account</a></p>";
			text += "<p>Nous esp\u00e9rons vous voir bientôt sur <em>Neel</em> !</p>";
			text += "<hr /><p><strong style=\"font-weight: normal; text-decoration: italic;\">Ce message est un ";
			text += "message automatique. Merci de ne pas y r\u00e9pondre. Envoy\u00e9 depuis Java ";
			text += System.getProperty("java.version") + " sur " + System.getProperty("os.name") + " ";
			text += System.getProperty("os.version") + ", fi\u00e8rement propuls\u00e9 par galaxyoyo";
			text += "</strong><br /></p></div>";
			mail.setText(text, "UTF-8", "html");
			Transport.send(mail);
		}
		catch (MessagingException ex)
		{
			throw new ServletException(ex);
		}
		
		resp.getWriter().write("Inscription effectu\u00e9e ! Vous avez dû reçevoir un mail confirmant votre demande, il contient un lien confirmant votre inscription.");
	}
}
