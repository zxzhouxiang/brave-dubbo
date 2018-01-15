package com.github.kristofa.brave.dubbox.filter;


import brave.Span;
import brave.Tracer;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.github.kristofa.brave.dubbox.DubboConsumerAdapter;
import com.github.kristofa.brave.dubbox.DubboConsumerHandler;
import com.github.kristofa.brave.dubbox.DubboTracing;

@Activate(group = Constants.CONSUMER)
public class DubboBraveConsumerFilter implements Filter {
    private static volatile Tracer tracer;
    private static volatile DubboConsumerHandler<RpcContext, Invocation,Result> handler;
    private static volatile TraceContext.Injector<RpcContext> injector;
    private static final Propagation.Setter<RpcContext,String>SETTER=new Propagation.Setter<RpcContext, String>() {
        @Override
        public void put(RpcContext rpcContext, String s, String s2) {
            rpcContext.setAttachment(s,s2);

        }
    };



        public static void setDubboTracing(DubboTracing dubboTracing) {
            DubboBraveConsumerFilter.tracer = dubboTracing.tracing().tracer();
            DubboBraveConsumerFilter.injector=dubboTracing.tracing().propagation().injector(SETTER);
        DubboBraveConsumerFilter.handler=DubboConsumerHandler.create(dubboTracing, new DubboConsumerAdapter<Invocation, Result>() {
            @Override
            public String method(Invocation var1) {
                return var1.getMethodName();
            }

            @Override
            public String url(Invocation var1) {
                return var1.getInvoker().getUrl().toFullString();
            }

            @Override
            public String requestHeader(Invocation var1, String var2) {
                return null;
            }

            @Override
            public String statusCode(Result var1) {
                if(var1.hasException()){
                    return "error";
                }else{
                    return "success";
                }
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
            Span span=this.handler.nextSpan(invocation);
            this.handler.handlReceive(injector,RpcContext.getContext(),invocation,result,span);
            try {
               result=  invoker.invoke(invocation);
                 this.handler.handlSend(invocation,result,span);
            }catch (Exception ex){
                result=new RpcResult();
                ((RpcResult)result).setException(ex);
                this.handler.handlSend(invocation,result,span);
            }finally {

            }
            return result;
        }

}
