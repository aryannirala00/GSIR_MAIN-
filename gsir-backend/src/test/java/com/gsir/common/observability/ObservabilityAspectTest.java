package com.gsir.common.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ObservabilityAspectTest {

    @Test
    void observesWorkflowStart() throws Throwable {
        MeterRegistry registry = new SimpleMeterRegistry();
        ObservabilityAspect aspect = new ObservabilityAspect(registry);
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        
        when(pjp.proceed()).thenReturn(new Object());
        
        aspect.observeWorkflowStart(pjp);
        
        assertThat(registry.counter("gsir.workflows.started").count()).isEqualTo(1.0);
    }
}
