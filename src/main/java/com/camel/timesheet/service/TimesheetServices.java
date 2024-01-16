package com.camel.timesheet.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.camel.timesheet.model.TimesheetEntity;
import com.camel.timesheet.repository.TimesheetRepo;

@Service
public class TimesheetServices {

	@Autowired
	private TimesheetRepo repo;

	public void saveTimesheet(TimesheetEntity entity) {
		repo.save(entity);
	}

	public TimesheetEntity findByEmpName(String name) {
		TimesheetEntity findByEmployeeName = repo.findByEmployeeName(name);
		return findByEmployeeName;
	}

	public TimesheetEntity findByEmpNumber(String empNumber) {
		TimesheetEntity entity = repo.findByEmployeeNumber(empNumber);
		return entity;
	}

	public boolean deleteByEmployeeNumberAndDate(String empNumber, String month, String year) {
		TimesheetEntity findByEmployeeNumber = repo.findByEmployeeNumberAndMonthAndYear(empNumber, month, year);
		if (findByEmployeeNumber != null) {
			repo.deleteByEmployeeNumberAndMonthAndYear(empNumber, month, year);
			return true;
		} else {
			return false;
		}
	}

	public TimesheetEntity findByEmployeeNumberAndMonthAndYear(String employeeNumber, String month, String year) {
		return repo.findByEmployeeNumberAndMonthAndYear(employeeNumber, month, year);
	}

	public boolean timesheetExists(TimesheetEntity timesheetDataToSave) {

		if (repo.findByEmployeeNumberAndMonthAndYear(timesheetDataToSave.getEmployeeNumber(),
				timesheetDataToSave.getMonth(), timesheetDataToSave.getYear()) != null) {
			return true;
		} else {
			return false;
		}
	}

	public List<TimesheetEntity> getApprovedTimesheet(String status, String month, String year) {
		return repo.findByStatusAndMonthAndYear(status, month, year);
	}

	public List<TimesheetEntity> getEmployeesByStatus(String string) {
		if (string.equals("pending")) {
			System.out.println("in pending ");
			List<TimesheetEntity> findByStatus = repo.findByStatus(string);
			return findByStatus;
		} else if (string.equals("approved")) {
			System.out.println("in approved ");
			List<TimesheetEntity> findByStatus = repo.findByStatus(string);
			return findByStatus;
		} else
			return null;
	}

	public Iterable<TimesheetEntity> getAllEmployees() {
		// TODO Auto-generated method stub
		 Iterable<TimesheetEntity> findAll = repo.findAll();
		 System.out.println(findAll+"Sss");
		 return findAll;
	}
}
