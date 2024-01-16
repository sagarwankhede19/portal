package com.camel.timesheet.service;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.camel.timesheet.model.Admin;
import com.camel.timesheet.model.CandidateEntity;
import com.camel.timesheet.model.User;
import com.camel.timesheet.repository.CandidateRepo;

@Service
public class CandidateService {

	@Autowired
	CandidateRepo repo;

	public boolean candidateExists(CandidateEntity candidate) {

		if (repo.findByEmail(candidate.getEmail()) != null) {
			return true;
		} else {
			return false;
		}
	}

	public String generateAndSetRandomPassword() {
		SecureRandom secureRandom = new SecureRandom();
		int size = 10;
		byte[] randomBytes = new byte[size];
		secureRandom.nextBytes(randomBytes);

		String generatedPassword = Base64.getEncoder().encodeToString(randomBytes).substring(0, size);

		System.out.println(generatedPassword);
		return generatedPassword;
	}

	public CandidateEntity saveCandidate(CandidateEntity candidate) {
		return repo.save(candidate);
	}

	public String getEmailBody(String recipientEmail, String generatedPassword) {
		return "Hi, Greetings for The Day..! " + ",\n\n"
				+ "Welcome to our On-Boarding portal..! Your Login details are as follows:\n\n" + "UserName: "
				+ recipientEmail + "\n" + "Password: " + generatedPassword + "\n\n"
				+ "Please Login with the above credentials and complete the further process. If you have any queries or need assistance, feel free to contact us.\n\n"
				+ "Thank you!\n\n" + "Best regards,\n"
				+ "HR Team, \nVkraft Software Services Pvt Ltd\nwww.vkraftsoftware.com\nwww.kraftsoftwaresolution.com";
	}

	public CandidateEntity getCandidateByEmail(String email, String password) {
		return repo.findByEmailAndPassword(email, password);
	}

	public boolean validateCandidate(String email, String password) {
		CandidateEntity Candidate = repo.findByEmail(email);
		return Candidate != null && Candidate.getPassword().equals(password);
	}

}
