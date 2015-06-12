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
		resp.setContentType("text/plain");
		Query query = MCServerUtil.query("212.129.49.101", 40241);
		resp.getWriter().write("IP : " + query.getHostIP() + ":" + query.getHostPort() + "\n");
		resp.getWriter().write("Message du jour : " + query.getHostname() + "\n");
		resp.getWriter().write("Jeu : " + query.getGameId() + "\n");
		resp.getWriter().write("Type de jeu : " + query.getGametype() + "\n");
		resp.getWriter().write("Version du jeu : " + query.getVersion() + "\n");
		resp.getWriter().write("Serveur : " + query.getMotor() + "\n");
		resp.getWriter().write("Plugins : " + query.getPlugins() + "\n");
		resp.getWriter().write("Dossier de la map : " + query.getMap() + "\n");
		resp.getWriter().write(query.getConnectedPlayersNumber() + " / " + query.getMaxPlayers()
						+ " joueur" + (query.getConnectedPlayersNumber() > 1 ? "s" : "") + " connect\u00e9"
						+ (query.getConnectedPlayersNumber() > 1 ? "s" : "")
						+ (query.getConnectedPlayersNumber() > 0 ? " :" : "") + "\n");
		for (String player : query.getConnectedPlayers())
		{
			resp.getWriter().write("\t- " + player + "\n");
		}
	}
}
