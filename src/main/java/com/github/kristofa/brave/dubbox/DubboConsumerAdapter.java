package com.github.kristofa.brave.dubbox;

public abstract class DubboConsumerAdapter <Req, Resp> extends DubboAdapter<Req, Resp>
{

    public DubboConsumerAdapter() {
    }

    public boolean parseServerAddress(Req req, zipkin2.Endpoint.Builder builder) {
        return false;
    }



}
