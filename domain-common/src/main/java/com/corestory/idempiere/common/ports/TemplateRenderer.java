package com.corestory.idempiere.common.ports;

import java.util.Map;

/**
 * Port for rendering message templates with variable substitution.
 *
 * <p>iDempiere parity: {@code MMailText.getMailText(boolean all, boolean parsed)}
 * (lib/org.adempiere.base/src/org/compiere/model/MMailText.java) supports {@code @variable@}
 * style token substitution against a contextual {@code PO}. We mirror the
 * {@code @variable@} syntax in the default adapter.
 */
public interface TemplateRenderer {

    String render(String template, Map<String, Object> context);

    default String renderSubject(String subjectTemplate, Map<String, Object> context) {
        return render(subjectTemplate, context);
    }
}
