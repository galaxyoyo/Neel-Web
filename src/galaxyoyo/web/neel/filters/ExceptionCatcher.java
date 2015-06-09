package galaxyoyo.web.neel.filters;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExceptionCatcher implements Filter
{
	@Override
	public void destroy()
	{
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
	{
		if (!(req instanceof HttpServletRequest) || !(resp instanceof HttpServletResponse))
		{
			resp.getWriter().write("Erreur : seul le HTTP est autoris\u00e9.");
			return;
		}
		
		try
		{
			chain.doFilter(req, resp);
		}
		catch (Throwable t)
		{
			HttpServletRequest hreq = (HttpServletRequest) req;
			HttpServletResponse hresp = (HttpServletResponse) resp;
			hresp.setStatus(500);
			String url = hreq.getProtocol().toLowerCase().substring(0, hreq.getProtocol().length() - 4) + "://" + hreq.getServerName();
			if (hreq.getServerPort() != 80)
				url += ":" + hreq.getServerPort();
			url += hreq.getServletPath();
			if (hreq.getPathInfo() != null)
				url += hreq.getPathInfo();
			String html = "<!DOCTYPE html>\n<html>\n  <head>\n  <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />\n";
			html += "  <title>Erreur 500 : " + t.getClass().getSimpleName() + "</title>\n  \n  <body>\n  <pre>\n";
			html += "Une erreur est survenue lors du chargement de la page '" + url + "'\n\n";
			html += "Voici le log complet :\n\n<hr />\n";
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			html += sw.toString();
			sw.close();
			html += "\n    </pre>\n  </body>\n</html>\n";
			hresp.getWriter().write(html);
		}
	}
	
	@Override
	public void init(FilterConfig config) throws ServletException
	{
	}
}
