package com.github.kristofa.brave.dubbox;

import brave.SpanCustomizer;
import brave.internal.Nullable;

public class DubboProviderParser extends DubboParser {
    public DubboProviderParser() {
    }

    public <Req> void request(DubboAdapter<Req, ?> adapter, Req req, SpanCustomizer customizer) {
        super.request(adapter, req, customizer);
    }

    public <Req,Resp> void response(DubboAdapter<?, Resp> adapter, @Nullable Req req, @Nullable Resp resp, SpanCustomizer customizer) {
        super.response(adapter, req, resp, customizer);
    }

}
