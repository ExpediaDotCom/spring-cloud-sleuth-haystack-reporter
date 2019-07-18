package com.expedia.www.spring.cloud.sleuth.haystack.reporter.idextractors;

import brave.internal.HexCodec;
import zipkin2.Span;

import java.util.UUID;

public class UUIDIdExtractor implements IdExtractor {

    @Override
    public String getTraceId(Span span) {
        final Long lowTraceId = HexCodec.lowerHexToUnsignedLong(span.traceId());
        final Long highTraceId = span.traceId().length() == 32 ? HexCodec.lowerHexToUnsignedLong(span.traceId(), 0) : 0;

        final UUID traceId = new UUID(highTraceId, lowTraceId);
        return traceId.toString();
    }

    @Override
    public String getSpanId(Span span) {
        return new UUID(0, HexCodec.lowerHexToUnsignedLong(span.id())).toString();
    }

    @Override
    public String getParentSpanId(Span span) {
        if (span.parentId() == null) {
            return "";
        }
        return new UUID(0, HexCodec.lowerHexToUnsignedLong(span.parentId())).toString();
    }
}
