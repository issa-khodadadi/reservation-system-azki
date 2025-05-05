package ir.azki.reservationSystem.services;

import ir.azki.reservationSystem.entities.AvailableSlot;
import ir.azki.reservationSystem.entities.Reservation;
import ir.azki.reservationSystem.entities.User;
import ir.azki.reservationSystem.enums.ServiceResultStatus;
import ir.azki.reservationSystem.repositories.ReservationRepository;
import ir.azki.reservationSystem.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final UserService userService;
    private final ReservationRepository reservationRepository;
    private final AvailableSlotService availableSlotService;

    @Transactional
    public ResponseResult reserveSlot(String username) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.error("user not found: {}", username);
            return new ResponseResult(ServiceResultStatus.NO_USER_FOUND, false);
        }

        Optional<AvailableSlot> availableSlotOptional = availableSlotService.findFirstAvailable();
        if (availableSlotOptional.isEmpty()) {
            log.error("available slot not found");
            return new ResponseResult(ServiceResultStatus.NO_AVAILABLE_SLOT_FOUND, false);
        }

        log.info("Reserving slot for user: {}", username);
        try {
            log.info("Reserving slot for user: {}", username);
            AvailableSlot reservedSlot = availableSlotService.updateReserveSlot(availableSlotOptional.get(), true);
            this.createNew(userOpt.get(), reservedSlot);
            return new ResponseResult(ServiceResultStatus.DONE, true);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("other user reserved this slot");
            return new ResponseResult(ServiceResultStatus.NO_AVAILABLE_SLOT_FOUND, false);
        }
    }

    @Transactional
    public ResponseResult cancelReservation(Long id) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        if (reservationOpt.isEmpty()) {
            log.error("reservation not found with id: {}", id);
            return new ResponseResult(ServiceResultStatus.NO_RESERVATION_FOUND, false);
        }

        try {
            Reservation reservation = reservationOpt.get();
            availableSlotService.updateReserveSlot(reservation.getSlot(), false);
            reservationRepository.delete(reservation);
            log.info("reservation cancelled successfully.");

            return new ResponseResult(ServiceResultStatus.DONE, true);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Slot update conflict during cancellation.");
            return new ResponseResult(ServiceResultStatus.ERROR, false);
        }
    }

    private void createNew(User user, AvailableSlot slot) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setSlot(slot);
        reservation.setCreatedTime(LocalDateTime.now());

        reservationRepository.save(reservation);
        log.info("Reservation saved for user: {}", user.getUsername());
    }
}