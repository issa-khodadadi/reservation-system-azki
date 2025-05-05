package ir.azki.reservationSystem.repositories;

import ir.azki.reservationSystem.entities.AvailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface AvailableSlotRepository extends JpaRepository<AvailableSlot, Long> {
    List<AvailableSlot> findByIsReservedFalseOrderByStartTimeAsc(Pageable pageable);
}

