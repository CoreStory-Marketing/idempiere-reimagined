package com.corestory.idempiere.notifications.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.notifications.api.dto.EmailTemplateDto;
import com.corestory.idempiere.notifications.repo.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

/**
 * Email-template browser. List/get work against the seeded V99 template rows.
 * The render-preview endpoint returns 501 today — the {@code TemplateRenderer} adapter
 * is not implemented (SHIP-101).
 */
@RestController
@RequestMapping("/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateRepository emailTemplateRepository;

    @GetMapping
    public PageResponse<EmailTemplateDto> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<EmailTemplateDto> p = emailTemplateRepository.findAll(PageRequest.of(page, size))
            .map(EmailTemplateDto::from);
        return PageResponse.of(p.getContent(), page, size, p.getTotalElements());
    }

    @GetMapping("/{id}")
    public EmailTemplateDto get(@PathVariable Long id) {
        return emailTemplateRepository.findById(id).map(EmailTemplateDto::from)
            .orElseThrow(() -> new NoSuchElementException("template not found: " + id));
    }

    @PostMapping("/{id}/preview")
    public Object preview(@PathVariable Long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Template render preview pending SHIP-101 (TemplateRenderer not yet implemented)");
    }
}
