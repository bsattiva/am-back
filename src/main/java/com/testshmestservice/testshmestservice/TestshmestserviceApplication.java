package com.testshmestservice.testshmestservice;

import com.utils.RequestHelper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@EnableAutoConfiguration
@SpringBootApplication
@CrossOrigin

public class TestshmestserviceApplication {


	public static void main(String[] args) {
		SpringApplication.run(TestshmestserviceApplication.class, args);
	}

}
