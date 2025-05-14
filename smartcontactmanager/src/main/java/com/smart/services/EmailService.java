package com.smart.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	
	@Autowired
	private JavaMailSender mailSender;

	public boolean sendEmail(String subject, String message, String to) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(message, true); // true = HTML enabled
			
			helper.setFrom(new InternetAddress("kushwaharitesh123456@gmail.com", "Smart Contact Manager"));
			
			mailSender.send(mimeMessage);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace(); // For production, use proper logging
			return false;
		}
	}

}
