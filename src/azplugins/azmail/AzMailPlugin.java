/**
 * @author Matt Haynes <matt@matthaynes.net> 
 */
package azplugins.azmail;

import azplugins.azmail.Mailer;
import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.logging.*;
import org.gudy.azureus2.plugins.ui.config.*;
import org.gudy.azureus2.plugins.ui.model.*;
import org.gudy.azureus2.plugins.download.*;

/**
 * 
 */
public class AzMailPlugin implements Plugin, DownloadManagerListener {

	protected PluginInterface plugin_interface;
	protected Mailer mailer;
	protected LoggerChannel logger;
	
	/* (non-Javadoc)
	 * @see org.gudy.azureus2.plugins.Plugin#initialize(org.gudy.azureus2.plugins.PluginInterface)
	 */
	public void initialize(PluginInterface plugin_interface) throws PluginException {
		
		this.plugin_interface = plugin_interface;
						
		this.logger = plugin_interface.getLogger().getChannel("azmail");
		
		this.mailer = new Mailer(this.logger);
		
		this.plugin_interface.getDownloadManager().addListener(this);
		
		this.setupConfigurationPage();
		
	}
	
	/**
	 * Capture any new downloads
	 */
	public void downloadAdded(Download download) {
		download.addCompletionListener(this.mailer);
	}

	/**
	 * Capture any removed downloads
	 */
	public void downloadRemoved(Download download) {
		download.removeCompletionListener(this.mailer);
	}
	
	/**
	 * Set up the plugin's configuration page
	 */
	protected void setupConfigurationPage() {
		
		final BasicPluginConfigModel config_page = this.getConfigModel();
		final Mailer mailer = this.mailer;
		
		// Setup config params
		config_page.addLabelParameter2("azmail.config.title");
		final BooleanParameter enabled_param = config_page.addBooleanParameter2("enabled", "azmail.config.enabled", false);
		final BooleanParameter secure_param = config_page.addBooleanParameter2("smtp_secure", "azmail.config.smtp_secure", false);
		final StringParameter smtp_param = config_page.addStringParameter2("smtp_server", "azmail.config.smtp_server", "");
		final StringParameter username_param = config_page.addStringParameter2( "smtp_user", "azmail.config.smtp_user", "");
		//final StringParameter password_param = config_page.addStringParameter2("smtp_pass", "azmail.config.smtp_pass", "");
		final PasswordParameter password_param = config_page.addPasswordParameter2("smtp_pass", "azmail.config.smtp_pass", PasswordParameter.ET_PLAIN, null);
		final IntParameter port_param = config_page.addIntParameter2("port", "azmail.config.smtp_port", 25); 
		final StringParameter email_param = config_page.addStringParameter2("email", "azmail.config.email", "");
		
		// Grey them out if enabled==false
		enabled_param.addEnabledOnSelection(smtp_param);
		enabled_param.addEnabledOnSelection(username_param);
		enabled_param.addEnabledOnSelection(password_param);
		enabled_param.addEnabledOnSelection(port_param);
		enabled_param.addEnabledOnSelection(secure_param);
		
				
		// When they change update the mailer object
		ParameterListener param_listener = new ParameterListener() {
			public void parameterChanged(Parameter p) {
				mailer.enabled 		= enabled_param.getValue();
				mailer.smtp_server 	= smtp_param.getValue();
				mailer.smtp_user 	= username_param.getValue();
				mailer.smtp_pass	= password_param.getValue();
				mailer.smtp_secure	= secure_param.getValue();
				mailer.smtp_port 	= port_param.getValue();
				mailer.email 		= email_param.getValue();
			}
		};
		
		// call the listener so the mailer class is config'd
		param_listener.parameterChanged(enabled_param);
		
		// Add event listeners
		enabled_param.addListener(param_listener);
		smtp_param.addListener(param_listener);
		secure_param.addListener(param_listener);
		username_param.addListener(param_listener);
		password_param.addListener(param_listener);
		port_param.addListener(param_listener);
		
	}
	
	/**
	 * Get plugin config model
	 * @return BasicPluginConfigModel
	 */
	protected BasicPluginConfigModel getConfigModel() {
		return this.plugin_interface.getUIManager().createBasicPluginConfigModel("azmail.config.title");
	}
	
}
