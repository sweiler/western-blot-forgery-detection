package forgery.web;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 * Servlet Filter implementation class LocalizationFilter
 */
@WebFilter(filterName = "localizationFilter", dispatcherTypes = { DispatcherType.REQUEST }, urlPatterns = { "/*" })
public class LocalizationFilter implements Filter {
	
	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		Locale requestedLocale;
		I18N test = new I18N(new Locale("en"));
		
		if(request.getParameter("L") != null && test.supportedLanguages.containsKey(request.getParameter("L"))) {
			
			requestedLocale = new Locale(request.getParameter("L"));
			((HttpServletRequest) request).getSession().setAttribute("locale", requestedLocale);
		} else if(((HttpServletRequest) request).getSession().getAttribute("locale") != null) {
			requestedLocale = (Locale) ((HttpServletRequest) request).getSession().getAttribute("locale");
		} else {
			requestedLocale = request.getLocale();
		}
		request.setAttribute("locale", requestedLocale.getLanguage());
		I18N tr = new I18N(requestedLocale);
		request.setAttribute("tr", tr);
		
		chain.doFilter(request, response);
	}

	public void init(FilterConfig fConfig) throws ServletException {

	}

}
