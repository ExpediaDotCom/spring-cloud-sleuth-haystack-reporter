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
package com.expedia.www.spring.cloud.sleuth.haystack.reporter.reporters;

import com.expedia.open.tracing.Log;
import com.expedia.open.tracing.Tag;
import com.expedia.www.haystack.remote.clients.Client;
import com.expedia.www.spring.cloud.sleuth.haystack.reporter.idextractors.IdExtractor;
import com.expedia.www.spring.cloud.sleuth.haystack.reporter.idextractors.UUIDIdExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

import java.util.List;
import java.util.stream.Collectors;

import static zipkin2.Span.Kind.*;

public class HaystackReporter implements Reporter<Span> {

    private final static Logger logger = LoggerFactory.getLogger(HaystackReporter.class);

    @Autowired
    private final List<Client> clients;

    private final String serviceName;

    private final IdExtractor idExtractor;

    public HaystackReporter(String serviceName, List<Client> clients) {
        this(serviceName, clients, new UUIDIdExtractor());
    }

    public HaystackReporter(String serviceName, List<Client> clients, IdExtractor idExtractor) {
        this.clients = clients;
        this.serviceName = serviceName;
        this.idExtractor = idExtractor;
    }

    @Override
    public void report(Span span) {
        com.expedia.open.tracing.Span haystackOpenTracingSpan = convertZipKinSpanToHaystackSpan(span);
        clients.forEach(c -> c.send(haystackOpenTracingSpan));
    }

    private com.expedia.open.tracing.Span convertZipKinSpanToHaystackSpan(Span span) {
        final com.expedia.open.tracing.Span.Builder builder = com.expedia.open.tracing.Span.newBuilder()
                .setServiceName(serviceName)
                .setOperationName(getSpanName(span))
                .setTraceId(idExtractor.getTraceId(span).toString())
                .setSpanId(idExtractor.getSpanId(span).toString())
                .setParentSpanId(idExtractor.getParentSpanId(span).toString())
                .setStartTime(span.timestampAsLong())
                .setDuration(span.durationAsLong())
                .addAllTags(getTags(span))
                .addAllLogs(getLogs(span));

        return builder.build();
    }

    private List<Log> getLogs(Span span) {
        final List<Log> logList = span.annotations().stream()
                .map(p -> buildLog(p.timestamp(), p.value()))
                .collect(Collectors.toList());

        return logList;
    }

    private Log buildLog(Long timestamp, String value) {
        final Log.Builder logBuilder = Log.newBuilder();

        logBuilder.setTimestamp(timestamp);
        if (value != null) {
            logBuilder.addFields(buildTag(value, null));
        }

        return logBuilder.build();
    }

    private List<Tag> getTags(Span span) {
        final List<Tag> tagList = span.tags().entrySet().stream()
                .filter(tag -> filterTags(tag.getKey()))
                .map(tag -> buildTag(tag.getKey(), tag.getValue()))
                .collect(Collectors.toList());

        if(span.localEndpoint() != null) {
            tagList.add(buildTag("localEndpoint", span.localEndpoint().toString()));
        }

        if (span.remoteEndpoint() != null) {
            tagList.add(buildTag("remoteEndpoint", span.remoteEndpoint().toString()));
        }

        if (CLIENT.equals(span.kind()) || PRODUCER.equals(span.kind())) {
            tagList.add(buildTag("span.kind", "client"));
        } else if (SERVER.equals(span.kind()) || CONSUMER.equals(span.kind())){
            tagList.add(buildTag("span.kind", "server"));
        }

        return tagList;
    }

    private Tag buildTag(String key, String value) {
        final Tag.Builder tagBuilder = Tag.newBuilder().setKey(key);

        if (value == null) {
            tagBuilder.setType(Tag.TagType.STRING);
            tagBuilder.setVStr("");
        } else {
            tagBuilder.setType(Tag.TagType.STRING);
            tagBuilder.setVStr(value);
        }

        return tagBuilder.build();
    }

    private boolean filterTags(String key) {
        return !("REQUEST".equals(key) || "RESPONSE".equals(key));
    }

    private String getSpanName(Span span) {
        if (span.name() != null) {
            return span.name();
        } else {
            return "";
        }
    }
}


