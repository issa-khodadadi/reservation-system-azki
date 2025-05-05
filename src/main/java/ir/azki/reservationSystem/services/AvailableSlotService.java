package ir.azki.reservationSystem.services;

import com.github.benmanes.caffeine.cache.Cache;
import ir.azki.reservationSystem.entities.AvailableSlot;
import ir.azki.reservationSystem.repositories.AvailableSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailableSlotService {
    private final AvailableSlotRepository availableSlotRepository;
    private final Cache<String, Optional<AvailableSlot>> availableSlotCache;

    public Optional<AvailableSlot> findFirstAvailable() {
        return availableSlotCache.get("firstAvailable", key -> {
            log.info("finding first available slot.");
            Pageable page = PageRequest.of(0, 1);
            List<AvailableSlot> slots = availableSlotRepository.findByIsReservedFalseOrderByStartTimeAsc(page);
            return slots.isEmpty() ? Optional.empty() : Optional.of(slots.get(0));
        });
    }

    public AvailableSlot updateReserveSlot(AvailableSlot availableSlot, boolean isReserved) {
        availableSlot.setReserved(isReserved);
        availableSlot.setModifiedTime(LocalDateTime.now());
        availableSlotCache.invalidate("firstAvailable");


        try {
            AvailableSlot saved = availableSlotRepository.save(availableSlot);
            log.info("available slot updated successfully; {}", saved);
            return saved;
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("conflict on slot id:{}.", availableSlot.getId());
            throw e;
        }
    }
}
