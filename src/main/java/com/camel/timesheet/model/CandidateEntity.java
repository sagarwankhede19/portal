package com.camel.timesheet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "candidate")
public class CandidateEntity {

	@Id
	private String id;

	private String email;

	private String password;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "CandidateEntity [id=" + id + ", email=" + email + ", password=" + password + "]";
	}

	public CandidateEntity(String id, String email, String password) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
	}

	public CandidateEntity() {
		// TODO Auto-generated constructor stub
	}

}
