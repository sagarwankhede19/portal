package com.camel.timesheet.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.camel.timesheet.model.Admin;

public interface AdminRepo extends ElasticsearchRepository<Admin, Integer> {

	Admin findByEmployeeNameAndEmployeeNumber(String employeeName, String employeeNumber);
	
	Admin findByEmail(String email);

	Admin findByPassword(String password);

	Admin findByEmailAndPassword(String email, String password);
}
