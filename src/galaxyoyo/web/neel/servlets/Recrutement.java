package galaxyoyo.web.neel.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Recrutement extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		getServletContext().getRequestDispatcher("/WEB-INF/jsp/recrutement.jsp").forward(req, resp);
	}
}
