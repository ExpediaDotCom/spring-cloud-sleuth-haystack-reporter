package com.expedia.www.spring.cloud.sleuth.haystack.reporter.idextractors;

import zipkin2.Span;

public class StringIdExtractor implements IdExtractor {

    @Override
    public String getTraceId(Span span) {
        return span.traceId();
    }

    @Override
    public String getSpanId(Span span) {
        return span.id();
    }

    @Override
    public String getParentSpanId(Span span) {
        if (span.parentId() == null) {
            return "";
        }
        return span.parentId();
    }
}
