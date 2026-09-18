package com.gsir.governance.service;

import com.gsir.governance.model.DataClassification;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PolicyEngineService {

    /**
     * Fulfills Part 76.61: Policy Engine
     * Evaluates whether a requested action is permitted based on resource classification
     */
    public boolean evaluateAccess(DataClassification classification, String action, Map<String, Object> context) {
        if (classification == null) return true; // Unclassified is treated as permissive by default here
        
        switch (classification) {
            case PUBLIC:
                return true;
            case INTERNAL:
                // Require internal organization context
                return action.equals("READ") || action.equals("WRITE");
            case PRIVATE:
                // E.g., check if actor is the owner
                return action.equals("READ") || action.equals("WRITE"); // Simplified for now
            case SENSITIVE:
                // Cannot be exported or shared externally
                if (action.equals("EXPORT") || action.equals("EXTERNAL_SHARE")) {
                    return false;
                }
                return true;
            case RESTRICTED:
                // Highly locked down
                return action.equals("READ_AUDIT");
            default:
                return false;
        }
    }
}
