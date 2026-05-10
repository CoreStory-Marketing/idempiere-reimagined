package com.corestory.idempiere.warehouse.model;

/**
 * Inspection result — mirrors {@code inspection_records.status} CHECK constraint.
 *
 * <p>iDempiere parity: {@code M_QualityTestResult} pass/fail flag.
 */
public enum InspectionStatus {
    PASS,
    FAIL,
    HOLD
}
