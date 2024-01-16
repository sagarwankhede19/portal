package com.camel.timesheet.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import com.camel.timesheet.model.CandidateEntity;

@EnableElasticsearchRepositories
public interface CandidateRepo extends ElasticsearchRepository<CandidateEntity, Integer> {

	CandidateEntity findByEmail(String email);

	CandidateEntity findByEmailAndPassword(String email, String password);
}
