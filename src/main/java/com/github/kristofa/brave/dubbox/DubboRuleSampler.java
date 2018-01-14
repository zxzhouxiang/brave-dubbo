package com.github.kristofa.brave.dubbox;

import brave.internal.Nullable;
import brave.sampler.ParameterizedSampler;
import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;

public final class DubboRuleSampler extends DubboSampler {
    final ParameterizedSampler<MethodAndPath> sampler;

    public static DubboRuleSampler.Builder newBuilder() {
        return new DubboRuleSampler.Builder();
    }

    DubboRuleSampler(List<MethodAndPathRule> rules) {
        this.sampler = ParameterizedSampler.create(rules);
    }

    public <Req> Boolean trySample(DubboAdapter<Req, ?> adapter, Req request) {
        String method = adapter.method(request);
        String path = adapter.path(request);
        return method != null && path != null ? this.sampler.sample(DubboRuleSampler.MethodAndPath.create(method, path)).sampled() : null;
    }

    static final class MethodAndPathRule extends ParameterizedSampler.Rule<MethodAndPath> {
        @Nullable
        final String method;
        final String path;

        MethodAndPathRule(@Nullable String method, String path, float rate) {
            super(rate);
            this.method = method;
            this.path = path;
        }

        public boolean matches(DubboRuleSampler.MethodAndPath parameters) {
            return this.method != null && !this.method.equals(parameters.method()) ? false : parameters.path().startsWith(this.path);
        }
    }

    @AutoValue
    abstract static class MethodAndPath {
        MethodAndPath() {
        }

        static DubboRuleSampler.MethodAndPath create(String method, String path) {
            return new AutoValue_DubboRuleSampler_MethodAndPath(method, path);
        }

        abstract String method();

        abstract String path();
    }

    public static final class Builder {
        final List<DubboRuleSampler.MethodAndPathRule> rules = new ArrayList();

        public DubboRuleSampler.Builder addRule(@Nullable String method, String path, float rate) {
            this.rules.add(new DubboRuleSampler.MethodAndPathRule(method, path, rate));
            return this;
        }

        public DubboRuleSampler build() {
            return new DubboRuleSampler(this.rules);
        }

        Builder() {
        }
    }


}
