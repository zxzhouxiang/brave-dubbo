package com.github.kristofa.brave.dubbox;

import brave.internal.Nullable;

public abstract class DubboSampler {
    public static final DubboSampler TRACE_ID = new DubboSampler() {
        @Nullable
        public <Req> Boolean trySample(DubboAdapter<Req, ?> adapter, Req request) {
            return null;
        }

        public String toString() {
            return "DeferDecision";
        }
    };
    public static final DubboSampler NEVER_SAMPLE = new DubboSampler() {
        public <Req> Boolean trySample(DubboAdapter<Req, ?> adapter, Req request) {
            return false;
        }

        public String toString() {
            return "NeverSample";
        }
    };

    public DubboSampler() {
    }

    @Nullable
    public abstract <Req> Boolean trySample(DubboAdapter<Req, ?> var1, Req var2);

}
