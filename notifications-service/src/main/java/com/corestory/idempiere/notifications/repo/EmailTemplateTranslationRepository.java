package com.corestory.idempiere.notifications.repo;

import com.corestory.idempiere.notifications.model.EmailTemplateTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateTranslationRepository extends JpaRepository<EmailTemplateTranslation, Long> {

    Optional<EmailTemplateTranslation> findByTemplateIdAndLanguage(Long templateId, String language);
}
