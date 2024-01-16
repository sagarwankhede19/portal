package com.camel.timesheet.controller;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.camel.timesheet.model.Admin;
import com.camel.timesheet.model.AppliedCandidateInformation;
import com.camel.timesheet.model.CandidateEntity;
import com.camel.timesheet.model.CreateJob;
import com.camel.timesheet.model.TimesheetEntity;
import com.camel.timesheet.model.User;
import com.camel.timesheet.service.AdminService;
import com.camel.timesheet.service.AppliedCandidateServices;
import com.camel.timesheet.service.CandidateService;
import com.camel.timesheet.service.JobServices;
import com.camel.timesheet.service.TimesheetServices;
import com.camel.timesheet.service.UserService;

@org.springframework.stereotype.Controller

@RestController
public class Controller extends RouteBuilder {

	@Autowired
	TimesheetServices services;
	@Autowired
	UserService userService;
	@Autowired
	AdminService adminService;
	@Autowired
	CandidateService candidateService;
	@Autowired
	JobServices jobServices;
	@Autowired
	AppliedCandidateServices candidateServices;

	@Override
	public void configure() throws Exception {
		restConfiguration().component("servlet").port(8082).enableCORS(true).host("localhost")
				.bindingMode(RestBindingMode.json);

		onException(Exception.class).handled(true).log("Exception occurred: ${exception.message}")
				.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
				.setBody(simple("Internal Server Error: ${exception.message}"));
		// -------Register Employee---------------

		rest("/registeruser").post().type(User.class).to("direct:processRegisterUser");
		from("direct:processRegisterUser").log("User : ${body}").process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				User user = exchange.getIn().getBody(User.class);
				boolean userExists = userService.userExists(user);

				if (userExists) {
					exchange.getMessage().setBody("User already exists for " + user.getEmployeeNumber());
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 409);
				} else {
					String randomPassword = userService.generateAndSetRandomPassword();
					System.out.println(randomPassword + " generated");
					user.setPassword(randomPassword);

					userService.saveUser(user);
					System.out.println("saved");
					System.out.println("data " + user);

					String recipientEmail = user.getEmail();
					System.out.println("emaillllllll" + recipientEmail);
					String generatedPassword = randomPassword;
					String emailBody = userService.getEmailBody(recipientEmail, generatedPassword,
							user.getEmployeeName());

					// Set email body and recipient email as message headers
					exchange.getMessage().setBody(emailBody);
					exchange.getMessage().setHeader("recipientEmail", recipientEmail);
					exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "text/plain");
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);

					// Sending the email with headers containing email body and recipient email
					ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate();
					producerTemplate.sendBodyAndHeaders("direct:sendMail", exchange.getMessage().getBody(),
							exchange.getMessage().getHeaders());

				}
			}
		});

		from("direct:sendMail").setHeader("Subject", constant("Your Account Information for Timesheet portal"))
				.process(exchange -> {
					String recipientEmail = exchange.getMessage().getHeader("recipientEmail", String.class);
					String emailBody = exchange.getIn().getBody(String.class); // Get the email body from the message
																				// body
					System.out.println(emailBody);
					System.out.println("Recipient Email: " + recipientEmail); // Log recipient email

					// Set the email body explicitly
					exchange.getMessage().setBody(emailBody);
				}).recipientList(simple(
						"smtps://smtp.gmail.com:465?username=babasahebudamle1007@gmail.com&password=qgge nnbr xjvj tqmn&to=${header.recipientEmail}"));
		// ------------------------Save Timesheet----------------------------
		rest().post("/savetimesheet").type(TimesheetEntity.class).to("direct:processTimesheet");
		from("direct:processTimesheet").log("Timesheet : ${body}").process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				TimesheetEntity timesheetDataToSave = exchange.getIn().getBody(TimesheetEntity.class);
				if (services.timesheetExists(timesheetDataToSave)) {
					exchange.getMessage()
							.setBody("Timesheet already exists for " + timesheetDataToSave.getMonth() + " month");
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 409);
				} else {
					timesheetDataToSave.setStatus("pending");
					services.saveTimesheet(timesheetDataToSave);
					System.out.println(timesheetDataToSave);
					exchange.getMessage()
							.setBody("Timesheet is saved for " + timesheetDataToSave.getMonth() + " month");
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);
				}
			}
		}).end();

		// ------------------------Update Timesheets--------------------------
		rest().put("/updatetimesheet").param().name("employeeNumber").type(RestParamType.query).endParam().param()
				.name("month").type(RestParamType.query).endParam().param().name("year").type(RestParamType.query)
				.endParam().param().name("clientName").type(RestParamType.query).endParam().param()
				.name("assignmentName").type(RestParamType.query).endParam().param().name("holidaysInput")
				.type(RestParamType.query).endParam().to("direct:updateTimeSheet");
		from("direct:updateTimeSheet").process(exchange -> {
			String employeeNumber = exchange.getIn().getHeader("employeeNumber", String.class);
			String month = exchange.getIn().getHeader("month", String.class);
			String year = exchange.getIn().getHeader("year", String.class);
			String clientName = exchange.getIn().getHeader("clientName", String.class);
			String assignmentName = exchange.getIn().getHeader("assignmentName", String.class);
			String holidaysInput = exchange.getIn().getHeader("holidaysInput", String.class);
			String errorMessage = "";
			if (!isValidNumber(employeeNumber)) {
				errorMessage += "Invalid number provided. ";
			}
			if (!isValidMonth(month)) {
				errorMessage += "Invalid month provided. ";
			}
			if (!isValidYear(year)) {
				errorMessage += "Invalid year provided. ";
			}

			if (!errorMessage.isEmpty()) {
				exchange.getMessage().setBody("Error: " + errorMessage.trim());
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
			} else {
				TimesheetEntity existingEntity = services.findByEmployeeNumberAndMonthAndYear(employeeNumber, month,
						year);
				if (existingEntity != null) {
					if (clientName != null) {
						existingEntity.setClientName(clientName);
					}
					if (assignmentName != null) {
						existingEntity.setAssignmentName(assignmentName);
					}
					if (holidaysInput != null) {
						existingEntity.setHolidaysInput(holidaysInput);
					}
					services.saveTimesheet(existingEntity);
					exchange.getMessage().setBody(existingEntity);
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

				} else {
					exchange.getMessage()
							.setBody("Error: TimeSheetEntity with number '" + employeeNumber + "' not found");
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
				}
			}
		});
		// ---------------- Delete Timesheet-------------------------
		rest().delete("/deleteemp").param().name("employeeNumber").type(RestParamType.query).endParam().param()
				.name("month").type(RestParamType.query).endParam().param().name("year").type(RestParamType.query)
				.endParam().to("direct:delete");
		from("direct:delete").process(exchange -> {
			String empNumber = exchange.getIn().getHeader("employeeNumber", String.class);
			String month = exchange.getIn().getHeader("month", String.class);
			String year = exchange.getIn().getHeader("year", String.class);
			System.out.println(empNumber + month + year);
			if (isValidNumber(empNumber) && isValidMonth(month) && isValidYear(year)) {
				boolean timesheetEntity = services.deleteByEmployeeNumberAndDate(empNumber, month, year);
				System.out.println(timesheetEntity);
				if (timesheetEntity) {
					System.out.println("valid");
					exchange.getMessage().setBody("Status: Record for Employee " + empNumber + " for Month " + month
							+ " and Year " + year + " Deleted");
				} else {
					exchange.getMessage().setBody("Error: Record not found for Employee " + empNumber + " for Month "
							+ month + " and Year " + year);
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
				}
			} else {
				exchange.getMessage().setBody("Error: Invalid input provided");
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
			}
		});

		// -------------------------------------

		rest().get("/gettimesheet").param().name("employeeNumber").type(RestParamType.query).endParam().param()
				.name("month").type(RestParamType.query).endParam().param().name("year").type(RestParamType.query)
				.endParam().to("direct:processName");
		from("direct:processName").process(exchange -> {
			String number = exchange.getIn().getHeader("employeeNumber", String.class);
			String month = exchange.getIn().getHeader("month", String.class);
			String year = exchange.getIn().getHeader("year", String.class);

			String errorMessage = "";
			if (!isValidNumber(number)) {
				errorMessage += "Invalid number provided. ";
			}
			if (!isValidMonth(month)) {
				errorMessage += "Invalid month provided. ";
			}
			if (!isValidYear(year)) {
				errorMessage += "Invalid year provided. ";
			}
			if (!errorMessage.isEmpty()) {
				exchange.getMessage().setBody("Error: " + errorMessage.trim());
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
			} else {
				TimesheetEntity timeSheetEntity = services.findByEmployeeNumberAndMonthAndYear(number, month, year);

				if (timeSheetEntity != null) {
					exchange.getMessage().setBody(timeSheetEntity);
				} else {
					exchange.getMessage().setBody("Error: TimeSheetEntity with number '" + number + "' not found");
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
				}
			}
		});

		// ----------------Get All Registered Employees-------------------------

		rest().get("/getallemployees").to("direct:getAllEmployees");

		from("direct:getAllEmployees").log("Get All Employees request received").process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				// Retrieve the list of all employees
				Iterable<User> allEmployees = userService.getAllEmployees();
				System.out.println(allEmployees);
				exchange.getMessage().setBody(allEmployees);

				// Set HTTP status code to 200 (OK)
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
			}
		}).end();
		// ---------------------------Register User------------------------------------

		// --------------------------------------------Verify
		// User-----------------------------------------
		rest("/verifyuser").get().param().name("username").type(RestParamType.query).endParam().param().name("password")
				.type(RestParamType.query).endParam().to("direct:user");
		from("direct:user").process(exchange -> {
			String username = exchange.getIn().getHeader("username", String.class);
			String password = exchange.getIn().getHeader("password", String.class);
			log.info("Received request with username: {} and password: {}", username, password);
			User user = userService.getUserByUsernameAndPassword(username, password);
			boolean isUserValid = verifyUser(user);
			if (isUserValid) {
				exchange.getMessage().setBody(user);
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
			} else {
				exchange.getMessage().setBody("User not found");
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
			}
		});

		// -----------------GetApprovedTimesheets--------------------
		rest().get("/getapprovedtimesheeets").to("direct:approvedTimesheeets");
		from("direct:approvedTimesheeets").process(exchange -> {
			List<TimesheetEntity> approvedEmployees = services.getEmployeesByStatus("approved");

			if (approvedEmployees != null && !approvedEmployees.isEmpty()) {
				exchange.getMessage().setBody(approvedEmployees);
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
			} else {
				exchange.getMessage().setBody("No employees found with status 'approved'");
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
			}
		});
		// -----------SaveAdmin-------------------------------
		rest().post("/saveadmin").type(Admin.class).to("direct:processAdmin");
		from("direct:processAdmin").log("User : ${body}").process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Admin admin = exchange.getIn().getBody(Admin.class);
				if (adminService.adminExists(admin)) {
					exchange.getMessage().setBody("User already exists for " + admin.getEmployeeName() + " with "
							+ admin.getEmployeeNumber() + " this employeeNumber");
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 409);
				} else {
					adminService.saveAdminCredential(admin);
					exchange.getMessage().setBody(admin.getEmployeeName() + " your registration successful.");
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);
				}
			}
		}).end();
		// -------Get Pending Employees--------
		rest().get("/pendingemployees").to("direct:getPendingEmployees");
		from("direct:getPendingEmployees").process(exchange -> {
			List<TimesheetEntity> pendingEmployees = services.getEmployeesByStatus("pending");

			if (pendingEmployees != null && !pendingEmployees.isEmpty()) {
				exchange.getMessage().setBody(pendingEmployees);
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
			} else {
				exchange.getMessage().setBody("No employees found with status 'pending'");
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
			}
		});
		// --------------------Verify Admin------------
		rest("/verifyadmin").post().param().name("username").type(RestParamType.query).endParam().param()
				.name("password").type(RestParamType.query).endParam().to("direct:Admin");
		from("direct:Admin").process(exchange -> {
			String username = exchange.getIn().getHeader("username", String.class);
			String password = exchange.getIn().getHeader("password", String.class);
			log.info("Received request with username: {} and password: {}", username, password);
			Admin user = adminService.getAdminByUsernameAndPassword(username, password);
			boolean isUserValid = verifyAdmin(user);
			if (isUserValid) {
				exchange.getMessage().setBody(user);
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
			} else {
				exchange.getMessage().setBody("User not found");
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
			}
		});
		// ------------------------Get Employee Count-----------------------------------
		rest("/getemployeescount").get().to("direct:employee");
		from("direct:employee").process(exchange -> {
			Long employeeCount = userService.getCountOfEmployee();
			System.out.println(employeeCount);
			exchange.getMessage().setBody(employeeCount);
			exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

		});

//		-----------------Candidate Register-------------------------
		rest("/registercandidate").post().type(CandidateEntity.class).to("direct:processRegisterCandidate");
		from("direct:processRegisterCandidate").log("CandidateEntity : ${body}").process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				CandidateEntity candidate = exchange.getIn().getBody(CandidateEntity.class);
				boolean candidateExists = candidateService.candidateExists(candidate);

				if (candidateExists) {
					exchange.getMessage().setBody("Candidate already exists for " + candidate.getEmail());
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 409);
				} else {
					String randomPassword = candidateService.generateAndSetRandomPassword();
					System.out.println(randomPassword + " generated");
					candidate.setPassword(randomPassword);

					candidateService.saveCandidate(candidate);
					System.out.println("saved");
					System.out.println("data " + candidate);

					String recipientEmail = candidate.getEmail();

					String generatedPassword = randomPassword;
					String emailBody = candidateService.getEmailBody(recipientEmail, generatedPassword);

					// Set email body and recipient email as message headers
					exchange.getMessage().setBody(emailBody);
					exchange.getMessage().setHeader("recipientEmail", recipientEmail);
					exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "text/plain");
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);

					// Sending the email with headers containing email body and recipient email
					ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate();
					producerTemplate.sendBodyAndHeaders("direct:sendingMail", exchange.getMessage().getBody(),
							exchange.getMessage().getHeaders());

				}
			}
		});

		from("direct:sendingMail").setHeader("Subject", constant("Your Login details for On-Boarding portal"))
				.process(exchange -> {
					String recipientEmail = exchange.getMessage().getHeader("recipientEmail", String.class);
					String emailBody = exchange.getIn().getBody(String.class); // Get the email body from the message
																				// body
					System.out.println(emailBody);
					System.out.println("Recipient Email: " + recipientEmail); // Log recipient email

					// Set the email body explicitly
					exchange.getMessage().setBody(emailBody);
				}).recipientList(simple(
						"smtps://smtp.gmail.com:465?username=babasahebudamle1007@gmail.com&password=qgge nnbr xjvj tqmn&to=${header.recipientEmail}"));

//		-----------------------Candidate Login----------------------------

		rest("/verifycandidate").get().param().name("email").type(RestParamType.query).endParam().param()
				.name("password").type(RestParamType.query).endParam().to("direct:Candidate");
		from("direct:Candidate").process(exchange -> {
			String email = exchange.getIn().getHeader("email", String.class);
			String password = exchange.getIn().getHeader("password", String.class);
			System.out.println(email + "   " + password);
			log.info("Received request with email: {} and password: {}", email, password);
			CandidateEntity candidate = candidateService.getCandidateByEmail(email, password);
			boolean isCandidateValid = verifyCandidate(candidate);
			if (isCandidateValid) {
				exchange.getMessage().setBody(candidate);
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
			} else {
				exchange.getMessage().setBody("Candidate not found");
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
			}
		});

//		--------------------Register Job-------------------------
		rest("/registerjob").post().type(CreateJob.class).to("direct:saveJob");
		from("direct:saveJob").log("Job : ${body}").process(new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {
				CreateJob body = exchange.getIn().getBody(CreateJob.class);
				CreateJob jobDetails = jobServices.getJobDetails(body.getJobId());
				System.out.println(jobDetails);
				if (jobDetails != null) {
					exchange.getMessage().setBody("Job details already exists for job id : " + body.getJobId());
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 409);

				} else {
					jobServices.saveJob(body);
					exchange.getMessage().setBody("Job details are saved for : " + body.getJobId());
					exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);
				}
			}
		});
		rest("/getalljobs").get().to("direct:availableJobs");
		from("direct:availableJobs").log("Get all jobs request received").process(new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {
				Iterable<CreateJob> allJobs = jobServices.getAllJobs();
				System.out.println(allJobs);
				exchange.getMessage().setBody(allJobs);
				// Set HTTP status code to 200 (OK)
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
			}
		});

		rest("listOfAppliedCandidates").get().to("direct:appliedCandidates");
		from("direct:appliedCandidates").log("Get all applied candidates request received").process(new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {
				Iterable<AppliedCandidateInformation> allAppliedCandidates = candidateServices
						.getAllAppliedCandidates();
				System.out.println(allAppliedCandidates);
				exchange.getMessage().setBody(allAppliedCandidates);
				// Set HTTP status code to 200 (OK)
				exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
			}
		});
	}

//	-----------------validations-----------------------------

	private boolean verifyAdmin(Admin admin) {

		return adminService.validateAdmin(admin.getEmail(), admin.getPassword());
	}

	private boolean verifyUser(User user) {
		return userService.validateUser(user.getEmail(), user.getPassword());
	}

	private boolean isValidYear(String year) {
		System.out.println("Valid" + year);
		return year != null && year.matches("^20[2-9]\\d|2[1-9]\\d{2}|3\\d{3}");
	}

	private boolean isValidMonth(String month) {
		System.out.println("Valid" + month);
		return month != null && month.matches(
				"^(January|February|March|April|May|June|July|August|September|October|November|December)|(0?[1-9"
						+ "]|1[0-2])$");
	}

	private boolean isValidNumber(String number) {
		System.out.println("Valid" + number);
		return number != null && number.matches("^(KSS|VKSS)\\d{2,4}$");
	}

	private boolean verifyCandidate(CandidateEntity candidate) {

		return candidateService.validateCandidate(candidate.getEmail(), candidate.getPassword());
	}

}
