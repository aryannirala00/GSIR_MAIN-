package com.gsir.ai.context;

import com.gsir.ai.planning.Plan;
import java.util.List;

public interface ContextBuilder {
    String assembleContext(
        String systemPolicy,
        String task,
        List<Object> memory,
        List<Object> evidence,
        List<Object> toolResults,
        Plan plan
    );
}
