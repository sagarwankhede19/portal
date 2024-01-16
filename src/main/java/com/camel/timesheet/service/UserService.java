package com.camel.timesheet.service;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.camel.timesheet.model.TimesheetEntity;
import com.camel.timesheet.model.User;
import com.camel.timesheet.repository.UserRepo;

@Service
public class UserService {

	@Autowired
	UserRepo repo;

	public User saveUser(User user) {
		return repo.save(user);
	}

	public boolean userExists(User user) {

		if (repo.findByEmployeeNumber(user.getEmployeeNumber()) != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean validateUser(String username, String password) {
		User user = repo.findByEmail(username);
		return user != null && user.getPassword().equals(password);
	}

	public User getUserByUsernameAndPassword(String email, String password) {
		return repo.findByEmailAndPassword(email, password);
	}

	public String getEmailBody(String recipientEmail, String generatedPassword, String employeeName) {
		return "Hi " + employeeName + ",\n\n"
				+ "Welcome to our Timesheet portal..! Your account details are as follows:\n\n" + "Email: "
				+ recipientEmail + "\n" + "Password: " + generatedPassword + "\n\n"
				+ "Please keep this information secure. If you have any questions or need assistance, feel free to contact us.\n\n"
				+ "Thank you!\n\n" + "Best regards,\n"
				+ "HR Team, \nVkraft Software Services Pvt Ltd\nwww.vkraftsoftware.com\nwww.kraftsoftwaresolution.com";
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

	public Long getCountOfEmployee() {
		return repo.count();
	}

	public Iterable<User> getAllEmployees() {
			Iterable<User> findAll = repo.findAll();
			
		return findAll;
	}

}
