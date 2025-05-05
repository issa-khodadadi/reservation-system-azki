package ir.azki.reservationSystem.utils;

import ir.azki.reservationSystem.entities.AvailableSlot;
import ir.azki.reservationSystem.entities.User;
import ir.azki.reservationSystem.repositories.AvailableSlotRepository;
import ir.azki.reservationSystem.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AvailableSlotRepository slotRepository;
    private final UserRepository userRepository;

    public DataInitializer(AvailableSlotRepository slotRepository, UserRepository userRepository) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        for (int i = 9; i < 17; i++) {
            slotRepository.save(new AvailableSlot(
                    LocalDateTime.of(2024, 12, 29, i, 0),
                    LocalDateTime.of(2024, 12, 29, i + 1, 0),
                    false
            ));
        }

        for (int i = 9; i < 11; i++) {
            slotRepository.save(new AvailableSlot(
                    LocalDateTime.of(2024, 12, 30, i, 0),
                    LocalDateTime.of(2024, 12, 30, i + 1, 0),
                    false
            ));
        }

        userRepository.save(new User("user1", "johndoe@example.com", "hashed_password_123"));
        userRepository.save(new User("user2", "janedoe@example.com", "hashed_password_456"));
        userRepository.save(new User("user3", "user123@example.com", "hashed_password_789"));
    }
}
