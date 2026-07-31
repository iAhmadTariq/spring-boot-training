package com.redmath.training;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = Application.class)
public class CucumberSpringConfiguration {
}
