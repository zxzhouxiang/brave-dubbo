package com.github.kristofa.brave.dubbox;
import brave.Span;
import brave.Tracer;
import brave.Span.Kind;
import brave.Tracer.SpanInScope;
import brave.internal.Nullable;
import brave.internal.Platform;
import brave.propagation.TraceContextOrSamplingFlags;
import brave.propagation.TraceContext.Extractor;

import zipkin2.Endpoint;
import zipkin2.Endpoint.Builder;
public final class DubboProviderHandler<context,Req,Resp> {
    final Tracer tracer;
    final DubboSampler sampler;
    final DubboProviderParser parser;
    final DubboProviderAdapter<Req, Resp> adapter;

    public static <Req, Resp> DubboProviderHandler create(DubboTracing dubboTracing, DubboProviderAdapter<Req, Resp> adapter) {
        return new DubboProviderHandler(dubboTracing, adapter);
    }

    DubboProviderHandler(DubboTracing dubboTracing, DubboProviderAdapter<Req, Resp> adapter) {
        this.tracer = dubboTracing.tracing().tracer();
        this.sampler = dubboTracing.providerSampler();
        this.parser = dubboTracing.providerParser();
        this.adapter = adapter;
    }

//    public Span handleReceive(Extractor<Req> extractor, context carrier,Req request) {
//        return this.handleReceive(extractor, carrier, request);
//    }

    public <context> Span handleReceive(Extractor<context> extractor, context carrier, Req request) {
        Span span = this.nextSpan(extractor.extract(carrier), request);
        if (span.isNoop()) {
            return span;
        } else {
            span.kind(Kind.SERVER);
            SpanInScope ws = this.tracer.withSpanInScope(span);

            try {
                this.parser.request(this.adapter, request, span);
            } finally {
                ws.close();
            }

            boolean parsedEndpoint = false;
            if (Platform.get().zipkinV1Present()) {
                Builder deprecatedEndpoint = Endpoint.newBuilder().serviceName("");
                if (parsedEndpoint = this.adapter.parseClientAddress(request, deprecatedEndpoint)) {
                    span.remoteEndpoint(deprecatedEndpoint.build());
                }
            }

            if (!parsedEndpoint) {
                zipkin2.Endpoint.Builder remoteEndpoint = zipkin2.Endpoint.newBuilder();
                if (this.adapter.parseClientAddress(request, remoteEndpoint)) {
                    span.remoteEndpoint(remoteEndpoint.build());
                }
            }

            return span.start();
        }
    }

    Span nextSpan(TraceContextOrSamplingFlags extracted, Req request) {
        if (extracted.sampled() == null) {
            extracted = extracted.sampled(this.sampler.trySample(this.adapter, request));
        }

        return extracted.context() != null ? this.tracer.joinSpan(extracted.context()) : this.tracer.nextSpan(extracted);
    }

    public void handleSend(@Nullable Req req,@Nullable Resp response, Span span) {
        if (!span.isNoop()) {
            SpanInScope ws = this.tracer.withSpanInScope(span);

            try {
                this.parser.response(this.adapter, req,response, span);
            } finally {
                ws.close();
                span.finish();
            }

        }
    }

}
