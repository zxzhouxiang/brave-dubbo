package com.github.kristofa.brave.dubbox;

import brave.SpanCustomizer;
import brave.internal.Nullable;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;

public class DubboParser {
    public <Req> void request(DubboAdapter<Req, ?> adapter, Req req, SpanCustomizer customizer) {
        customizer.name(this.spanName(adapter, req));
        String path = adapter.path(req);
        if (path != null) {
            customizer.tag("dubbo.path", path);
        }

    }

    protected <Req> String spanName(DubboAdapter<Req, ?> adapter, Req req) {
        return adapter.method(req);
    }

    public <Req,Resp> void response(DubboAdapter<?, Resp> adapter,@Nullable Req req, @Nullable Resp resp, SpanCustomizer customizer) {
        String dubboStatus = resp != null ? adapter.statusCode(resp) : null;
        if (dubboStatus != null ) {
            customizer.tag("dubbo.status", dubboStatus);
        }
        if(dubboStatus!="success"){
            String message=resp!=null?adapter.error(resp):null;
            customizer.tag("dubbo.error",message);
        }

    }


    DubboParser() {
    }

}
