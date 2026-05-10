package com.corestory.idempiere.notifications.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.notifications.api.dto.NotificationLogDto;
import com.corestory.idempiere.notifications.repo.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

/**
 * Read-side notification surface. The general listing is intentionally a 501 stub —
 * SHIP-101 wires per-recipient querying. The {@code /log} endpoints work today (against
 * the empty {@code notification_log} table).
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;

    @GetMapping
    public Object list() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Per-recipient notification listing pending SHIP-101");
    }

    @GetMapping("/log")
    public PageResponse<NotificationLogDto> log(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<NotificationLogDto> p = notificationLogRepository.findAll(PageRequest.of(page, size))
            .map(NotificationLogDto::from);
        return PageResponse.of(p.getContent(), page, size, p.getTotalElements());
    }

    @GetMapping("/log/{id}")
    public NotificationLogDto getLog(@PathVariable Long id) {
        return notificationLogRepository.findById(id).map(NotificationLogDto::from)
            .orElseThrow(() -> new NoSuchElementException("notification log entry not found: " + id));
    }
}
