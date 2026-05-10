package com.corestory.idempiere.notifications.repo;

import com.corestory.idempiere.notifications.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByCode(String code);

    Optional<EmailTemplate> findByCodeAndLanguage(String code, String language);
}
