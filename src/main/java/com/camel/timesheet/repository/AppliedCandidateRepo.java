package com.camel.timesheet.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import com.camel.timesheet.model.AppliedCandidateInformation;

@EnableElasticsearchRepositories
public interface AppliedCandidateRepo extends ElasticsearchRepository<AppliedCandidateInformation, Integer> {

}
