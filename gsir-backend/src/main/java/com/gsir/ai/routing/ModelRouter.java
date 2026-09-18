package com.gsir.ai.routing;

import java.util.Map;

public interface ModelRouter {
    String selectModel(ModelTier minimumTier, Map<String, Object> taskConstraints);
}
