package com.github.kristofa.brave.dubbox;
import brave.Span;
import brave.Tracer;
import brave.Span.Kind;
import brave.Tracer.SpanInScope;
import brave.internal.Nullable;
import brave.internal.Platform;
import brave.propagation.CurrentTraceContext;
import brave.propagation.SamplingFlags;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Injector;
import com.alibaba.dubbo.rpc.RpcContext;
import zipkin2.Endpoint;
import zipkin2.Endpoint.Builder;

public final class DubboConsumerHandler<context,Req,Resp>{
    final Tracer tracer;
    final DubboSampler sampler;
    final CurrentTraceContext currentTraceContext;
    final DubboConsumerParser parser;
    final DubboConsumerAdapter<Req,Resp> adapter;
    final String serverName;
    final boolean serverNameSet;

    public static <Req, Resp> DubboConsumerHandler create(DubboTracing dubboTracing, DubboConsumerAdapter<Req, Resp> adapter) {
        return new DubboConsumerHandler(dubboTracing, adapter);
    }

    DubboConsumerHandler(DubboTracing dubboTracing, DubboConsumerAdapter<Req, Resp> adapter) {
        this.tracer = dubboTracing.tracing().tracer();
        this.sampler = dubboTracing.consumerSampler();
        this.currentTraceContext = dubboTracing.tracing().currentTraceContext();
        this.parser = dubboTracing.consumerParser();
        this.serverName = dubboTracing.serverName();
        this.serverNameSet = !this.serverName.equals("");
        this.adapter = adapter;
    }

//    public Span handleSend(Injector<context> injector,context carrier, Req request,Exception ex) {
//        return this.handleSend(injector, carrier, request,resp);
//    }

    public <context> Span handleSend(Injector<context> injector, context carrier, Req request,Resp resp) {
        return this.handleSend(injector, carrier, request,resp, this.nextSpan(request));
    }

//    public Span handleSend(Injector<context> injector, Req request,Resp resp, Span span) {
//        return this.handleSend(injector, , request,resp, span);
//    }

    public <context> Span handleSend(Injector<context> injector, context carrier, Req request,Resp resp, Span span) {
        injector.inject(span.context(), carrier);
        if (span.isNoop()) {
            return span;
        } else {
            span.kind(Kind.CLIENT);
            SpanInScope ws = this.tracer.withSpanInScope(span);

            try {
                this.parser.response(this.adapter, request,resp, span);
            } finally {
                ws.close();
            }

            boolean parsedEndpoint = false;
            if (Platform.get().zipkinV1Present()) {
                Builder deprecatedEndpoint = Endpoint.newBuilder().serviceName(this.serverNameSet ? this.serverName : "");
                if (parsedEndpoint = this.adapter.parseServerAddress(request, deprecatedEndpoint)) {
                    span.remoteEndpoint(deprecatedEndpoint.serviceName(this.serverName).build());
                }
            }

            if (!parsedEndpoint) {
                zipkin2.Endpoint.Builder remoteEndpoint = zipkin2.Endpoint.newBuilder().serviceName(this.serverName);
                if (this.adapter.parseServerAddress(request, remoteEndpoint) || this.serverNameSet) {
                    span.remoteEndpoint(remoteEndpoint.build());
                }
            }

            return span.start();
        }
    }

    public Span nextSpan(Req request) {
        TraceContext parent = this.currentTraceContext.get();
        if (parent != null) {
            return this.tracer.newChild(parent);
        } else {
            Boolean sampled = this.sampler.trySample(this.adapter, request);
            return sampled == null ? this.tracer.newTrace() : this.tracer.newTrace(sampled.booleanValue() ? SamplingFlags.SAMPLED : SamplingFlags.NOT_SAMPLED);
        }
    }

    public void handleReceive(@Nullable Req req, Span span) {
        if (!span.isNoop()) {
            SpanInScope ws = this.tracer.withSpanInScope(span);

            try {
                this.parser.request(this.adapter, req, span);
            } finally {
                ws.close();
                span.finish();
            }

        }
    }

}
