package com.corestory.idempiere.notifications.api.dto;

import com.corestory.idempiere.notifications.model.EmailTemplate;

public record EmailTemplateDto(
    Long id,
    String code,
    String subjectTemplate,
    String bodyTemplate,
    String bodyFormat,
    String language,
    Boolean active
) {

    public static EmailTemplateDto from(EmailTemplate t) {
        return new EmailTemplateDto(
            t.getId(), t.getCode(), t.getSubjectTemplate(), t.getBodyTemplate(),
            t.getBodyFormat(), t.getLanguage(), t.getActive()
        );
    }
}
