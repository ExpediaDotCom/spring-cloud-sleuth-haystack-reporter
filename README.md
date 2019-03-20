[![Build Status](https://travis-ci.org/ExpediaDotCom/spring-cloud-sleuth-haystack-reporter.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/spring-cloud-sleuth-haystack-reporter)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE)

Table of Contents
=================

* [Table of Contents](#table-of-contents)
   * [Configuring spring sleuth applications to report to haystack](#configuring-spring-sleuth-applications-to-report-to-haystack)
   * [Quick Start](#quick-start)
      * [Spring Sleuth dependency](#spring-sleuth-dependency)
      * [Spring Sleuth Haystack dependency](#spring-sleuth-haystack-dependency)
      * [Other dependencies](#other-dependencies)
      * [Sample yaml/properties file](#sample-yamlproperties-file)
      * [Example Project](#example-project)
   * [Details](#details)
      * [Using this library](#using-this-library)
      * [Defaults](#defaults)
      * [Configuration](#configuration)
         * [Disabling Tracing](#disabling-tracing)
         * [Grpc Client](#grpc-client)
         * [Customizing Sleuth](#customizing-sleuth)


## Instrumenting Spring Boot or Spring Web applications

One can use [spring-cloud-sleuth-haystack-reporter](spring-cloud-sleuth-haystack-reporter) to configure spring sleuth applications to send tracing information to Opentracing complicant [Haystack](https://expediadotcom.github.io/haystack/) server, distributed tracing platform. 

This library in turn uses [ospring-cloud-starter-sleuth](spring-cloud-starter-sleuth) which helps build the 
`brave.Tracer` instance required to trace the application. This tracer creates a brave.span which in turn gets converted to zipkin2.span to be used by this library to report to haystack.

## Quick Start

This section provides steps required to quickly configure your spring application to be wired using Spring sleuth's integration to Haystack. If you need additional information, please read the subsequent sections in this documentation

### Spring Sleuth dependency

Add the following dependency to your application to get an instance of `brave.Tracer`. This allows spans to be mostly automatically created inside the application.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
    <version>${spring-sleuth.version}</version>
</dependency>
```

### Spring Sleuth Haystack dependency

To allow the spans to be reported to haystack

```xml
<dependency>
    <groupId>com.expedia.www</groupId>
    <artifactId>spring-cloud-sleuth-haystack-reporter</artifactId>
    <version>${spring.cloud.sleuth.haystack.reporter.version}</version>
</dependency>
```

### Sample yaml/properties file

Add the following to the properties or yaml file of the application being instrumented  (this is just a sample. change the name of the application, host name/port of the client etc)

```yaml
server:
  port: 3379

spring:
  application:
    name: spring-sleuth-haystack
  sleuth:
    enabled: true
    haystack:
      enabled: true
      client:
        grpc:
          host: localhost
          port: 34000
    sampler:
      probability: 1.0

logging.level.org.springframework.web: info
```

### Example Project

One can checkout the example project using this integration @ [https://github.com/ExpediaDotCom/spring-cloud-sleuth-haystack-reporter-example](https://github.com/ExpediaDotCom/spring-cloud-sleuth-haystack-reporter-example) 

## Details

### Using this library

Check maven for latest versions of this library. At present, this library has been built and tested with Spring Boot 2.x

```xml
<dependency>
    <groupId>com.expedia.www</groupId>
    <artifactId>spring-cloud-sleuth-haystack-reporter</artifactId>
    <version>${spring.cloud.sleuth.haystack.reporter.version}</version>
</dependency>
```

### Defaults

Adding this library auto configures an instance of [HaystackReporter](https://github.com/ExpediaDotCom/spring-cloud-sleuth-haystack-reporter), which is responsible for converting the zipkin2.Span to Haystack Proto Span,
and makes sure appropriate tags are applied on the spans such as client and server.

### Configuration

One can also configure the reporter created by the library using few configuration properties.

#### Disabling Tracing

One can completely disable tracing with configuration property `spring.sleuth.haystack.enabled`. If the property is missing (default), this property value is assumed as `true`.

```yaml
spring:
  sleuth:
    haystack:
      enabled: false
```

#### Grpc Client

Haystack Reporter provides a [GRPC client](https://github.com/ExpediaDotCom/haystack-agent) this can be configured with host and port as below. 

```yaml
spring:
  application:
    name: spring-sleuth-haystack
  sleuth: 
    enabled: true
    haystack:
      enabled: true
      client:
        grpc:
          host: localhost
          port: 34000
```

#### Customizing Sleuth

Sleuth can be customised using properties as shown below. For more configuration options please refer [Spring Sleuth](https://github.com/spring-cloud/spring-cloud-sleuth).

```yaml
spring:
  application:
    name: spring-sleuth-haystack
  sleuth:
    sampler:
      probability: 1.0
    enabled: true
    haystack:
      enabled: true
      client:
        grpc:
          host: localhost
          port: 34000
```

