package com.als.mail.example;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMailConfig {

	public static final String CONFIG_MAIL_FOLDER = "configMail\\";
	public static final String CONFIG_AUTH_FOLDER = "configAuthentication\\";

	private InternetAddress[] to;
	private String message;
	private String subject;
	private String pathToConfigMailProperties;
	private String pathToConfigAuthenticationProperties;
	private Properties configMail;
	private Properties configAuth;

	public SendMailConfig(String to, String message, String subject, String pathToConfigMailProperties,
			String pathToConfigAuthenticationProperties) throws IllegalArgumentException {
		super();
		setTo(to);
		this.message = message;
		this.subject = subject;
		this.pathToConfigMailProperties = pathToConfigMailProperties;
		this.pathToConfigAuthenticationProperties = pathToConfigAuthenticationProperties;
		setConfigProperties();
	}

	public String getTo() {
		return String.join(",", Arrays.asList(to).stream().map(x -> x.getAddress()).collect(Collectors.toList()));
	}

	public void setTo(String to) {
		InternetAddress[] toAddrs = null;

		try {
			toAddrs = InternetAddress.parse(to);
		} catch (AddressException e) {
			System.out.println("Could not parse the To e-mail addresses");
		}

		this.to = toAddrs;
		System.out.println("Successfully configured the To e-mails:");
		for (int i = 0; i < this.to.length; i++)
			System.out.println(this.to[i]);

		System.out.println();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPathToConfigMailProperties() {
		return pathToConfigMailProperties;
	}

	public void setPathToConfigMailProperties(String pathToConfigMailProperties) {
		this.pathToConfigMailProperties = pathToConfigMailProperties;
	}

	private void setConfigProperties() {

		this.configMail = new Properties();
		try (FileInputStream inputStream = new FileInputStream(pathToConfigMailProperties)) {
			configMail.load(inputStream);
		} catch (FileNotFoundException ex) {
			System.out.println("ConfigMail file not found at " + pathToConfigMailProperties);
		} catch (IOException ex) {
			System.out.println("ConfigMail file could not be loaded");
		}

		System.out.println("Successfully got the configMail file at " + pathToConfigMailProperties);
		System.out.println();

		this.configAuth = new Properties();
		try (FileInputStream inputStream = new FileInputStream(pathToConfigAuthenticationProperties)) {
			configAuth.load(inputStream);
		} catch (FileNotFoundException ex) {
			System.out.println("ConfigAuth file not found at " + pathToConfigAuthenticationProperties);
		} catch (IOException ex) {
			System.out.println("ConfigAuth file could not be loaded");
		}

		System.out.println("Successfully got the ConfigAuth file at " + pathToConfigAuthenticationProperties);
		System.out.println();
	}

	public void send() {
		Session session = Session.getDefaultInstance(configMail, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(configAuth.getProperty("userName"),
						configAuth.getProperty("password"));
			}
		});
		session.setDebug(Boolean.parseBoolean(configMail.getProperty("mail.debug")));

		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(configAuth.getProperty("userName")));
			msg.setRecipients(Message.RecipientType.TO, to);
			msg.setSubject(subject);
			msg.setSentDate(new Date());
			msg.setContent(message, "text/plain");
			msg.saveChanges();
			Transport.send(msg);

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}

		} catch (MessagingException mex) {

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}

			System.out.println("Sending failed with exception:");
			mex.printStackTrace();
			System.out.println();
			Exception ex = mex;
			do {
				if (ex instanceof SendFailedException) {
					SendFailedException sfex = (SendFailedException) ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if (invalid != null) {
						System.out.println("    ** Invalid Addresses");
						for (int i = 0; i < invalid.length; i++)
							System.out.println("         " + invalid[i]);
					}

					Address[] validUnsent = sfex.getValidUnsentAddresses();
					if (validUnsent != null) {
						System.out.println("    ** ValidUnsent Addresses");
						for (int i = 0; i < validUnsent.length; i++)
							System.out.println("         " + validUnsent[i]);
					}

					Address[] validSent = sfex.getValidSentAddresses();
					if (validSent != null) {
						System.out.println("    ** ValidSent Addresses");
						for (int i = 0; i < validSent.length; i++)
							System.out.println("         " + validSent[i]);
					}
				}

				System.out.println();
				if (ex instanceof MessagingException) {
					ex = ((MessagingException) ex).getNextException();
				} else {
					ex = null;
				}
			} while (ex != null);
		}
	}

}
