package com.camel.timesheet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.camel.timesheet.model.CreateJob;
import com.camel.timesheet.repository.JobRepo;

@Service
public class JobServices {

	@Autowired
	JobRepo repo;

	public CreateJob getJobDetails(String jobId) {
		CreateJob findByJobId = repo.findByJobId(jobId);
		return findByJobId;
	}

	public void saveJob(CreateJob body) {
		repo.save(body);
	}

	public Iterable<CreateJob> getAllJobs() {
		Iterable<CreateJob> findAll = repo.findAll();
		return findAll;
	}
}
