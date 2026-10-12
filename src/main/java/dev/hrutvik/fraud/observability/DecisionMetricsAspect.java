package dev.hrutvik.fraud.observability;

import dev.hrutvik.fraud.decision.DecisionResponse;
import io.micrometer.core.instrument.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Aspect @Component
public class DecisionMetricsAspect {
    private final MeterRegistry registry;
    public DecisionMetricsAspect(MeterRegistry registry){this.registry=registry;}
    @Around("execution(* dev.hrutvik.fraud.decision.DecisionService.decide(..))")
    public Object measure(ProceedingJoinPoint call)throws Throwable{
        long started=System.nanoTime();
        try{
            Object result=call.proceed();
            if(result instanceof DecisionResponse response){registry.counter("fraud.decisions","outcome",response.outcome().name(),"replayed",Boolean.toString(response.replayed())).increment();}
            return result;
        }catch(Throwable error){registry.counter("fraud.decision.errors","exception",error.getClass().getSimpleName()).increment();throw error;}
        finally{registry.timer("fraud.decision.latency").record(System.nanoTime()-started,TimeUnit.NANOSECONDS);}
    }
}
