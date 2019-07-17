package com.expedia.www.spring.cloud.sleuth.haystack.reporter.idextractors;

import zipkin2.Span;

public interface IdExtractor {
    Object getTraceId(Span span);

    Object getSpanId(Span span);

    Object getParentSpanId(Span span);
}
