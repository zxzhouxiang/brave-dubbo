package com.github.kristofa.brave.dubbox;

import brave.internal.Nullable;

import java.net.URI;

public abstract class DubboAdapter<Req,Resp> {
    @Nullable
    public abstract String method(Req var1);

    @Nullable
    public String path(Req request) {
        String url = this.url(request);
        return url == null ? null : URI.create(url).getPath();
    }

    @Nullable
    public abstract String url(Req var1);

    @Nullable
    public abstract String requestHeader(Req var1, String var2);

    @Nullable
    public abstract String statusCode(Resp var1);
    @Nullable
    public abstract String error(Resp var1);

    DubboAdapter() {
    }

}
