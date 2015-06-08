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
			String html = "<!DOCTYPE html>\n<html>\n  <head>\n  <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />\n";
			html += "  <title>Erreur 500 : " + t.getClass().getSimpleName() + "</title>\n  \n  <body>\n  <pre>\n\t";
			html += "\tUne erreur est survenue lors du chargement de la page '" + hreq.getServletPath() + "'<br /><br />\n";
			html += "\tVoici le log complet :<br /><hr /><br />\n\n";
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			html += sw.toString().replaceAll("\n", "<br />\n");
			sw.close();
			html += "\n    </pre>\n  </body>\n</html>\n";
			resp.getWriter().write(html);
		}
	}
	
	@Override
	public void init(FilterConfig config) throws ServletException
	{
	}
}
