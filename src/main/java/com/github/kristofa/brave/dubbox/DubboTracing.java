package com.github.kristofa.brave.dubbox;

import brave.Tracing;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DubboTracing {
    public static DubboTracing create(Tracing tracing) {
     return newBuilder(tracing).build();
    }

    public static DubboTracing.Builder newBuilder(Tracing tracing) {
        return (new com.github.kristofa.brave.dubbox.AutoValue_DubboTracing.Builder()).tracing(tracing).serverName("").consumerParser(new DubboConsumerParser()).providerParser(new DubboProviderParser()).consumerSampler(DubboSampler.TRACE_ID).providerSampler(DubboSampler.TRACE_ID);

    }

    public abstract Tracing tracing();

    public abstract DubboConsumerParser consumerParser();

    public abstract String serverName();

    public DubboTracing clientOf(String serverName) {
        return this.toBuilder().serverName(serverName).build();
    }

    public abstract DubboProviderParser providerParser();

    public abstract DubboSampler consumerSampler();

    public abstract DubboSampler providerSampler();

    public abstract DubboTracing.Builder toBuilder();

    DubboTracing() {
    }
@AutoValue.Builder
    public abstract static class Builder {
        public abstract DubboTracing.Builder tracing(Tracing var1);

        public abstract DubboTracing.Builder consumerParser(DubboConsumerParser var1);

        public abstract DubboTracing.Builder providerParser(DubboProviderParser var1);

        public abstract DubboTracing.Builder consumerSampler(DubboSampler var1);

        public abstract DubboTracing.Builder providerSampler(DubboSampler var1);

        public abstract DubboTracing build();

        abstract DubboTracing.Builder serverName(String var1);

        Builder() {
        }
    }

}
