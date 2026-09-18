package com.gsir.provenance.model;

/**
 * Part 80 (80.6) — Provenance Node Types.
 * Each important research object is represented as a provenance node.
 */
public enum NodeType {
    DATA,
    CODE,
    MODEL,
    WORKFLOW,
    EXECUTION,
    PARAMETER,
    TOOL,
    ENVIRONMENT,
    RESULT,
    DECISION,
    KNOWLEDGE,
    DOCUMENT,
    CHUNK,
    EMBEDDING,
    CLAIM,
    EVIDENCE,
    SOURCE,
    EXTRACTION,
    HYPOTHESIS,
    PLAN,
    EXPERIMENT,
    OBSERVATION,
    ACTION,
    AI_RUN
}

