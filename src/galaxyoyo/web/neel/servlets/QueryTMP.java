package galaxyoyo.web.neel.servlets;

import galaxyoyo.web.neel.util.MCServerUtil;
import galaxyoyo.web.neel.util.MCServerUtil.Query;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class QueryTMP extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
	{
		resp.setContentType("text/html; charset=UTF-8");
		Query query = MCServerUtil.query();
		resp.getWriter().write("<!doctype html>\r\n<head>\r\n<meta http-equiv=\"content-type\" "
						+ "content=\"text/html; charset=UTF-8\" />\r\n<title>Query Serveur</title>\r\n"
						+ "</head>\r\n<body>\r\n");
		resp.getWriter().write("IP : " + query.getHostIP() + ":" + query.getHostPort() + "<br />\r\n");
		resp.getWriter().write("Message du jour : " + query.getHostname() + "<br />\r\n");
		resp.getWriter().write("Jeu : " + query.getGameId() + "<br />\r\n");
		resp.getWriter().write("Type de jeu : " + query.getGametype() + "<br />\r\n");
		resp.getWriter().write("Version du jeu : " + query.getVersion() + "<br />\r\n");
		resp.getWriter().write("Serveur : " + query.getMotor() + "<br />\r\n");
		resp.getWriter().write("Plugins : " + query.getPlugins() + "<br />\r\n");
		resp.getWriter().write("Dossier de la map : " + query.getMap() + "<br />\r\n");
		resp.getWriter().write(query.getConnectedPlayersNumber() + " / " + query.getMaxPlayers()
						+ " joueur" + (query.getConnectedPlayersNumber() > 1 ? "s" : "") + " connect\u00e9"
						+ (query.getConnectedPlayersNumber() > 1 ? "s" : "")
						+ (query.getConnectedPlayersNumber() > 0 ? " :" : "") + "<br />\r\n");
		for (String player : query.getConnectedPlayers())
		{
			resp.getWriter().write(" &nbsp; &nbsp; &nbsp; &nbsp;- " + player + "<br />\r\n");
		}
		resp.getWriter().write("</body>\r\n</html>");
	}
}
