package ir.azki.reservationSystem;

import ir.azki.reservationSystem.entities.AvailableSlot;
import ir.azki.reservationSystem.entities.Reservation;
import ir.azki.reservationSystem.entities.User;
import ir.azki.reservationSystem.enums.ServiceResultStatus;
import ir.azki.reservationSystem.repositories.ReservationRepository;
import ir.azki.reservationSystem.services.AvailableSlotService;
import ir.azki.reservationSystem.services.ReservationService;
import ir.azki.reservationSystem.services.UserService;
import ir.azki.reservationSystem.utils.ResponseResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceMockTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private UserService userService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private AvailableSlotService availableSlotService;

    @Test
    void reserveSlot_shouldReserveSuccessfully() {
        String username = "user1";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        AvailableSlot slot = new AvailableSlot();
        slot.setId(10L);

        Mockito.when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(availableSlotService.findFirstAvailable()).thenReturn(Optional.of(slot));
        Mockito.when(availableSlotService.updateReserveSlot(slot, true)).thenReturn(slot);

        ResponseResult result = reservationService.reserveSlot(username);

        Assertions.assertTrue(result.getIsSuccessful());
    }

    @Test
    void reserveSlot_shouldReturnNoUserFound() {
        String username = "unknown_user";
        Mockito.when(userService.findByUsername(username)).thenReturn(Optional.empty());

        ResponseResult result = reservationService.reserveSlot(username);

        Assertions.assertFalse(result.getIsSuccessful());
        Assertions.assertEquals(ServiceResultStatus.NO_USER_FOUND, result.getStatus());
    }

    @Test
    void reserveSlot_shouldReturnNoAvailableSlot() {
        String username = "user1";
        User user = new User();
        user.setUsername(username);
        Mockito.when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(availableSlotService.findFirstAvailable()).thenReturn(Optional.empty());

        ResponseResult result = reservationService.reserveSlot(username);

        Assertions.assertFalse(result.getIsSuccessful());
        Assertions.assertEquals(ServiceResultStatus.NO_AVAILABLE_SLOT_FOUND, result.getStatus());
    }


    @Test
    void cancelReservation_shouldCancelSuccessfully() {
        Long reservationId = 1L;
        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setSlot(new AvailableSlot());

        Mockito.when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        ResponseResult result = reservationService.cancelReservation(reservationId);

        Assertions.assertTrue(result.getIsSuccessful());
        Assertions.assertEquals(ServiceResultStatus.DONE, result.getStatus());
        Mockito.verify(availableSlotService).updateReserveSlot(reservation.getSlot(), false);
        Mockito.verify(reservationRepository).delete(reservation);
    }

    @Test
    void cancelReservation_shouldReturnNotFound() {
        Long id = 99L;
        Mockito.when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        ResponseResult result = reservationService.cancelReservation(id);

        Assertions.assertFalse(result.getIsSuccessful());
        Assertions.assertEquals(ServiceResultStatus.NO_RESERVATION_FOUND, result.getStatus());
    }

    @Test
    void reserveSlot_shouldThrowOptimisticLockingFailureException() {
        // Arrange
        String username = "user1";
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(username);

        AvailableSlot mockSlot = new AvailableSlot();
        mockSlot.setId(100L);
        mockSlot.setReserved(false);

        // Mock behavior
        Mockito.when(userService.findByUsername(username)).thenReturn(Optional.of(mockUser));
        Mockito.when(availableSlotService.findFirstAvailable()).thenReturn(Optional.of(mockSlot));

        // Simulate conflict during update
        Mockito.when(availableSlotService.updateReserveSlot(mockSlot, true))
                .thenThrow(new ObjectOptimisticLockingFailureException(AvailableSlot.class, 100L));

        // Act
        ResponseResult result = reservationService.reserveSlot(username);

        // Assert
        Assertions.assertFalse(result.getIsSuccessful());
        Assertions.assertEquals(ServiceResultStatus.NO_AVAILABLE_SLOT_FOUND, result.getStatus());
    }
}
