package com.github.kristofa.brave.dubbox;

import zipkin2.Endpoint;

public abstract class DubboProviderAdapter <Req,Resp>extends DubboAdapter<Req,Resp>{


    public boolean parseClientAddress(Req req, Endpoint.Builder builder) {
        String xForwardedFor = this.requestHeader(req, "X-Forwarded-For");
        return xForwardedFor != null && builder.parseIp(xForwardedFor);
    }

//    /** @deprecated */
//    @Deprecated
//    public boolean parseClientAddress(Req req, zipkin.Endpoint.Builder builder) {
//        return false;
//    }

}
