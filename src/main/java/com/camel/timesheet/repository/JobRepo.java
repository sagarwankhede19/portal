package com.camel.timesheet.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.camel.timesheet.model.CreateJob;

public interface JobRepo extends ElasticsearchRepository<CreateJob, Integer> {

	CreateJob findByJobId(String jobId);

}
