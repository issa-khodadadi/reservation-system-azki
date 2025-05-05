package ir.azki.reservationSystem.controllers;

import ir.azki.reservationSystem.dto.filters.UsernameFilter;
import ir.azki.reservationSystem.enums.ServiceResultStatus;
import ir.azki.reservationSystem.services.ReservationService;
import ir.azki.reservationSystem.utils.ResponseResult;
import ir.azki.reservationSystem.utils.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ResponseResult> reserveSlot(@RequestBody UsernameFilter filter) {
        try {
            if (Validation.isNullOrBlank(filter.getUsername())) {
                log.error("necessary fields not filled.");
                return ResponseEntity.ok(
                        new ResponseResult(
                                ServiceResultStatus.FIELDS_REQUIRED,
                                false));
            }

            ResponseResult serviceResult = reservationService.reserveSlot(filter.getUsername());
            return ResponseEntity.ok(serviceResult);
        } catch (Exception e) {
            log.error("error in reserve slot", e);
            return ResponseEntity.ok(
                    new ResponseResult(
                            ServiceResultStatus.ERROR,
                            false)
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            if (Validation.isNullOrZero(id)) {
                log.error("necessary fields not filled.");
                return ResponseEntity.ok(
                        new ResponseResult(
                                ServiceResultStatus.FIELDS_REQUIRED,
                                false));
            }

            ResponseResult serviceResult = reservationService.cancelReservation(id);
            return ResponseEntity.ok(serviceResult);
        } catch (Exception e) {
            log.error("error in cancel reservation", e);
            return ResponseEntity.ok(
                    new ResponseResult(
                            ServiceResultStatus.ERROR,
                            false)
            );
        }
    }
}
