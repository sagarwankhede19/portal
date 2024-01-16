package com.camel.timesheet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.camel.timesheet.model.Admin;
import com.camel.timesheet.repository.AdminRepo;

@Service
public class AdminService {

	@Autowired
	private	AdminRepo repo;

	public Admin saveAdminCredential(Admin admin) {

		return repo.save(admin);
	}
	
	public boolean adminExists(Admin admin) {

		if (repo.findByEmployeeNameAndEmployeeNumber(admin.getEmployeeName(),admin.getEmployeeNumber()) != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean validateAdmin(String username, String password) {
        Admin admin = repo.findByEmail(username);
        return admin != null && admin.getPassword().equals(password);
    }
	
	public Admin getAdminByUsernameAndPassword(String email, String password ) {
		return repo.findByEmailAndPassword(email, password);
	}

}
