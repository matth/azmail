package azplugins.azmail;

import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadCompletionListener;
import org.gudy.azureus2.plugins.logging.*;
import javax.mail.*;
import com.sun.mail.util.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.security.Security;

public class Mailer implements DownloadCompletionListener {

	public int smtp_port;
	public Boolean enabled, smtp_secure;
	public String smtp_server, smtp_user, email;
	public byte[] smtp_pass;
	public LoggerChannel logger;
	
	public Mailer(LoggerChannel logger) {
		this.logger = logger;
	}
	
	public void onCompletion(Download download) {
		if (this.enabled) {
			this.sendMail(download.getName());
		}
	}
	
	public void sendMail(String filename) {
		
		Properties props = new Properties();
		
        props.put("mail.transport.protocol", (this.smtp_secure) ? "smtps" : "smtp");
        props.put("mail.host", this.smtp_server);
               
        try {
        	
        	if (this.smtp_secure) {
        		
        		String cacerts = System.getProperty("java.home") +
				 System.getProperty("file.separator") +
				 "lib" +
				 System.getProperty("file.separator") +
				 "security" +
				 System.getProperty("file.separator") +
				 "cacerts";

        		System.setProperty("javax.net.ssl.trustStore", cacerts);        		
        		
        		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        		        		
        		props.put("mail.smtps.auth", "true");
        		props.put("mail.smtp.ssl.enable", "true");
        		props.put("mail.smtps.quitwait", "false");
        		props.put("mail.smtps.port", this.smtp_port);
        		props.put("mail.smtp.socketFactory.port", this.smtp_port);
        		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        		props.put("mail.smtp.socketFactory.fallback", "false");

        		
        	} else {
        		props.put("mail.smtp.auth", "true");
        		props.put("mail.smtp.port", this.smtp_port);
        	}
            		
			Session mailSession = Session.getDefaultInstance(props);
			
			mailSession.setDebug(true);
			
			Transport transport = mailSession.getTransport();
			
			MimeMessage message = new MimeMessage(mailSession);
			
			message.setSubject("Azureus Mail Plugin: " + filename + " has been downloaded");
			message.setContent(filename + " has been downloaded", "text/plain");
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(this.email));
			transport.connect(this.smtp_server, this.smtp_port, this.smtp_user, new String(this.smtp_pass));
	
			transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
			transport.close();
			
		} catch (Exception e) {
			 this.logger.logAlertRepeatable("Azureus Mail Plugin: Error whilst trying to send email", e);
		}
		
	}

}
