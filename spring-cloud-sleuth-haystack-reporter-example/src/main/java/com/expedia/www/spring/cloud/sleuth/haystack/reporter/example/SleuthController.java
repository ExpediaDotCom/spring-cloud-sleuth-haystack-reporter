package com.expedia.www.spring.cloud.sleuth.haystack.reporter.example;

import brave.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SleuthController {

    private final static Logger logger = LoggerFactory.getLogger(SleuthController.class);

    @Autowired
    Tracer tracer;

    @GetMapping("/hello")
    public String helloSleuth() {
        logger.info("tracer: " + tracer);
        logger.info("active span: " + tracer.currentSpan());
        logger.info("Hello Sleuth");
        return "Success";
    }
}