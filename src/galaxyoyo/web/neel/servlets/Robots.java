package galaxyoyo.web.neel.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Robots extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.getWriter().write("User-agent : *\r\n");
		resp.getWriter().write("Disallow : \r\n");
		resp.getWriter().write("\r\n");
		resp.getWriter().write("User-agent : Googlebot/2.1 (+http://www.google.com/bot.html)\r\n");
	}
}
