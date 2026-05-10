package com.corestory.idempiere.notifications.template;

import com.corestory.idempiere.common.ports.TemplateRenderer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <b>STUB.</b> The recorded brownfield-feature-implementation demo (SHIP-101) implements
 * the {@code @variable@} substitution to mirror iDempiere's
 * {@code MMailText.getMailText(boolean all, boolean parsed)}.
 */
@Component
public class MustacheTemplateRenderer implements TemplateRenderer {

    @Override
    public String render(String template, Map<String, Object> context) {
        throw new UnsupportedOperationException("Pending SHIP-101");
    }
}
