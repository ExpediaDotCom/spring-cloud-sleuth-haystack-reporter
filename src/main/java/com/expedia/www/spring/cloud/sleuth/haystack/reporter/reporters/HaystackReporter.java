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

import brave.internal.HexCodec;
import com.expedia.open.tracing.Log;
import com.expedia.open.tracing.Tag;
import com.expedia.www.haystack.client.dispatchers.clients.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static zipkin2.Span.Kind.CLIENT;

public class HaystackReporter implements Reporter<Span> {

    private final static Logger logger = LoggerFactory.getLogger(HaystackReporter.class);

    @Autowired
    private final List<Client> clients;

    private final String serviceName;

    public HaystackReporter(String serviceName, List<Client> clients) {
        this.clients = clients;
        this.serviceName = serviceName;
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
                .setTraceId(getTraceId(span))
                .setSpanId(getSpanId(span))
                .setParentSpanId(getParentSpanId(span))
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

        if (span.kind() == null) {
            logger.debug("No span kind found in span so we will treat this as clients span", span);
            if (span.timestampAsLong() != 0L) {
                logList.add(buildLog(span.timestamp(), "event", "cs"));
            }

            if (span.durationAsLong() != 0L) {
                logList.add(buildLog(span.timestamp() + span.duration(), "event", "cr"));
            }
        } else {
            switch (span.kind()) {
                case CLIENT:
                    if (span.timestampAsLong() != 0L) {
                        logList.add(buildLog(span.timestamp(), "event", "cs"));
                    }

                    if (span.durationAsLong() != 0L) {
                        logList.add(buildLog(span.timestamp() + span.duration(), "event", "cr"));
                    }
                    break;
                case SERVER:
                    if (span.timestampAsLong() != 0L) {
                        logList.add(buildLog(span.timestamp(), "event", "sr"));
                    }

                    if (span.durationAsLong() != 0L) {
                        logList.add(buildLog(span.timestamp() + span.duration(), "event", "ss"));
                    }
                    break;
                case PRODUCER:
                    if (span.timestampAsLong() != 0L) {
                        logList.add(buildLog(span.timestamp(), "event", "ms"));
                    }

                    if (span.durationAsLong() != 0L) {
                        logList.add(buildLog(span.timestamp() + span.duration(), "event", "ws"));
                    }
                    break;
                case CONSUMER:
                    if (span.timestampAsLong() != 0L) {
                        logList.add(buildLog(span.timestamp(), "event", "ms"));
                    }

                    if (span.durationAsLong() != 0L) {
                        logList.add(buildLog(span.timestamp() + span.duration(), "event", "mr"));
                    }
                    break;
                default:
                    logger.debug("No span kind found in span so we will treat this as clients span", span);
                    if (span.timestampAsLong() != 0L) {
                        logList.add(buildLog(span.timestamp(), "event", "cs"));
                    }

                    if (span.durationAsLong() != 0L) {
                        logList.add(buildLog(span.timestamp() + span.duration(), "event", "cr"));
                    }
                    break;
            }
        }

        return logList;
    }

    private Log buildLog(Long timestamp, String key, String value) {
        final Log.Builder logBuilder = Log.newBuilder();

        logBuilder.setTimestamp(timestamp);
        logBuilder.addFields(buildTag(key, value));

        return logBuilder.build();
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

        if (CLIENT.equals(span.kind())) {
            tagList.add(buildTag("kind", "client"));
        } else {
            tagList.add(buildTag("kind", "server"));
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

    private String getTraceId(Span span) {
        final Long lowTraceId = HexCodec.lowerHexToUnsignedLong(span.traceId());
        final Long highTraceId = span.traceId().length() == 32 ? HexCodec.lowerHexToUnsignedLong(span.traceId(), 0) : 0;

        final UUID traceId = new UUID(highTraceId, lowTraceId);
        return traceId.toString();
    }

    private String getSpanId(Span span) {
        return new UUID(0, HexCodec.lowerHexToUnsignedLong(span.id())).toString();
    }

    private String getParentSpanId(Span span) {
        if (span.parentId() == null) {
            return "";
        }
        return new UUID(0, HexCodec.lowerHexToUnsignedLong(span.parentId())).toString();
    }

    private String getSpanName(Span span) {
        if (span.name() != null) {
            return span.name();
        } else {
            return "";
        }
    }
}


