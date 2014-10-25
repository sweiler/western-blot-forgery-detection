package forgery.web;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18N {

	private ResourceBundle bundle;
	private Locale currentLocale;
	private String currentScope;

	public Map<String, String> supportedLanguages = new HashMap<String, String>();

	/**
	 * Initialize with default scope.
	 * 
	 * @param locale
	 *            The language, which should be used.
	 */
	public I18N(Locale locale) {
		this(null, locale);
	}

	/**
	 * Initializes the class with the {@code scope} and the language indicated
	 * by {@code locale}. On initialization a properties file with the name
	 * "forgery_[scope]_[language].properties" is loaded and used for
	 * internationalization.
	 * 
	 * @param scope
	 *            The scope of this instance. If {@code null}, the default scope
	 *            is used.
	 * @param locale
	 *            The locale to use on loading the {@link ResourceBundle}.
	 */
	public I18N(String scope, Locale locale) {
		currentLocale = locale;
		setScope(scope);
		ResourceBundle options = ResourceBundle.getBundle(
				"localization/forgery_languages", currentLocale);
		Enumeration<String> keys = options.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			supportedLanguages.put(key, options.getString(key));
		}
	}

	/**
	 * Change the scope of this instance. The corresponding
	 * {@link ResourceBundle} will be loaded.
	 * 
	 * @param scope
	 *            The scope to which this instance will change.
	 */
	public void setScope(String scope) {
		if (scope == null) {
			bundle = ResourceBundle.getBundle("localization/forgery_default",
					currentLocale);
			currentScope = "default";
		} else {
			bundle = ResourceBundle.getBundle("localization/forgery_" + scope,
					currentLocale);
			currentScope = scope;
		}

	}

	/**
	 * @return The currently selected scope or {@code null} if the default scope
	 *         is active.
	 */
	public String getScope() {
		return currentScope;
	}

	/**
	 * @return The currently used locale.
	 */
	public Locale getLocale() {
		return currentLocale;
	}

	/**
	 * Get the corresponding translation for the string referenced by
	 * {@code identifier}. If {@code args} is not empty, the translated String
	 * is formatted using the {@code args}.
	 * 
	 * @param identifier
	 *            The identifier of the translatable string.
	 * @param args
	 *            Optional arguments to format the translated string with.
	 * @return The (optionally) formatted translated string corresponding to the
	 *         {@code identifier}.
	 */
	public String __(String identifier, Object... args) {
		try {
			if (args != null && args.length > 0) {
				MessageFormat formatter = new MessageFormat(
						bundle.getString(identifier), currentLocale);
	
				return formatter.format(args);
			}
			return bundle.getString(identifier);
		} catch(MissingResourceException e) {
			return identifier.toUpperCase();
		}
	}

}
