package com.camel.timesheet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.camel.timesheet.model.AppliedCandidateInformation;
import com.camel.timesheet.repository.AppliedCandidateRepo;

@Service
public class AppliedCandidateServices {

	@Autowired
	AppliedCandidateRepo candidateRepo;

	public Iterable<AppliedCandidateInformation> getAllAppliedCandidates() {
		Iterable<AppliedCandidateInformation> findAll = candidateRepo.findAll();
		return findAll;
	}
}
