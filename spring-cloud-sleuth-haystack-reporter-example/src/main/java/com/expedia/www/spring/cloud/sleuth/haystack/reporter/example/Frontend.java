/*
 * Copyright 2018 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.www.spring.cloud.sleuth.haystack.reporter.example;

import brave.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class Frontend {
    private final static Logger logger = LoggerFactory.getLogger(Frontend.class);

    @Value("${backend.url:http://localhost:9091}")
    private String backendBaseUrl;

    @Autowired
    Tracer tracer;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/hello")
    public String callBackend() {
        logger.info("tracer: " + tracer);
        logger.info("active span: " + tracer.currentSpan());
        return restTemplate.getForObject(backendBaseUrl + "/api/hello", String.class);
    }
}
