package com.github.kristofa.brave.dubbox.filter;

import brave.Tracer;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.github.kristofa.brave.dubbox.DubboProviderAdapter;
import com.github.kristofa.brave.dubbox.DubboProviderHandler;
import com.github.kristofa.brave.dubbox.DubboTracing;

@Activate(group = Constants.PROVIDER)
public class DubboBraveProviderFilter implements Filter {
    private static volatile Tracer tracer;
    private static volatile DubboProviderHandler<RpcContext,Invocation,Result> handler;
    private static volatile TraceContext.Extractor<RpcContext> extractor;
    private static final Propagation.Getter<RpcContext, String> GETTER = new Propagation.Getter<RpcContext, String>() {
        public String get(RpcContext carrier, String key) {
            return carrier.getAttachment(key);
        }
    };
    public static void setDubboTracing(DubboTracing dubboTracing){
        DubboBraveProviderFilter.extractor = dubboTracing.tracing().propagation().extractor(GETTER);
        DubboBraveProviderFilter.tracer=dubboTracing.tracing().tracer();
        DubboBraveProviderFilter.handler=DubboProviderHandler.create(dubboTracing, new DubboProviderAdapter<Invocation, Result>() {
            @Override
            public String method(Invocation var1) {
                return var1.getMethodName();
            }

            @Override
            public String url(Invocation var1) {
                return RpcContext.getContext().getUrl().toFullString();
            }

            @Override
            public String requestHeader(Invocation var1, String var2) {
                return null;
            }

            @Override
            public String statusCode(Result var1) {
                if(var1.hasException()){
                    return "error";
                }
                return "success";
            }

            @Override
            public String error(Result var1) {
                if(var1.hasException()){
                    return  var1.getException().getMessage();
                }
                return null;
            }
        });

    }
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result=null;
        this.handler.handleReceive(extractor,RpcContext.getContext(),invocation);
        try {
            result = invoker.invoke(invocation);
            this.handler.handleSend(invocation,result,this.tracer.currentSpan());
        }catch (Exception ex){
            result=new RpcResult();
            ((RpcResult)result).setException(ex);
            this.handler.handleSend(invocation,result,this.tracer.currentSpan());
        }finally {

        }
        return result;
    }
}
