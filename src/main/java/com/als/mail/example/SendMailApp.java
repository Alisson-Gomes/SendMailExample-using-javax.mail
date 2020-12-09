package com.als.mail.example;

public class SendMailApp {

	public static void main(String[] args) {

		String to = "nenjutso@gmail.com,als_08.net@hotmail.com";
		String message = "Hello. Testing Java Mail API";
		String subject = "Hello Guy";
		SendMailConfig mailConfig = null;

		try {
			mailConfig = new SendMailConfig(to, message, subject,
					SendMailConfig.CONFIG_MAIL_FOLDER + "hotmailConfigOut.properties",
					SendMailConfig.CONFIG_AUTH_FOLDER + "hotmailAuthentication.properties");
		} catch (IllegalArgumentException ex) {
			System.out.println("Bad parameters sent to SendMailConfig constructor");
		} catch (Exception ex) {
			System.out.println("An error has occurred: " + ex.getMessage());
			ex.printStackTrace();
		}

		mailConfig.send();
	}
}
