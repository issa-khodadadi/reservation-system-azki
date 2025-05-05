package ir.azki.reservationSystem;

import com.github.benmanes.caffeine.cache.Cache;
import ir.azki.reservationSystem.entities.AvailableSlot;
import ir.azki.reservationSystem.repositories.AvailableSlotRepository;
import ir.azki.reservationSystem.repositories.ReservationRepository;
import ir.azki.reservationSystem.services.ReservationService;
import ir.azki.reservationSystem.utils.ResponseResult;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("performance")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationPerformanceTests {
    @Autowired
    ReservationService reservationService;

    @Autowired
    AvailableSlotRepository slotRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    Cache<String, Optional<AvailableSlot>> availableSlotCache;

    @BeforeAll
    void warmUpSystem() {
        System.out.println("Warming up...");

        // First dummy call to trigger Hibernate, cache, DB connection, etc.
        try {
            reservationService.reserveSlot("user1");
        } catch (Exception ignored) {}

        // Optional: wait a few milliseconds to let background tasks complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {}
    }

    @BeforeEach
    void resetTestData() {
        availableSlotCache.invalidateAll();

        reservationRepository.deleteAll();
        slotRepository.deleteAll();

        // 10 available slots as in your SQL
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
    }

    @Test
    @Order(1)
    void testHighLoadReservationSpeed() {
        int tps = 20;
        long maxAllowed = 100; // ms

        for (int i = 0; i < tps; i++) {
            long start = System.nanoTime();
            ResponseResult result = reservationService.reserveSlot("user2");
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            System.out.println("Request " + (i + 1) + " took: " + durationMs + " ms");
            assertTrue(durationMs < maxAllowed, "Request exceeded 100ms");
        }
    }

    @Test
    @Order(2)
    void testSingleReservationUnder100ms() {
        long start = System.nanoTime();
        ResponseResult result = reservationService.reserveSlot("user3");
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Response time: " + durationMs + " ms");
        assertTrue(durationMs < 100, "Reservation should respond under 100ms");
        assertTrue(result.getIsSuccessful(), "Reservation failed");
    }

    @Test
    @Order(3)
    void testConcurrentReservations() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final String username = "user" + ((i % 3) + 1); // user1, user2, user3
            executor.submit(() -> {
                try {
                    ResponseResult result = reservationService.reserveSlot(username);
                    System.out.println(Thread.currentThread().getName() + ": " + result.getStatus());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long reservationCount = reservationRepository.count();
        long slotCount = slotRepository.count();

        System.out.println("Total Reservations: " + reservationCount);
        System.out.println("Total Slots: " + slotCount);

        assertTrue(reservationCount <= slotCount, "More reservations than slots!");
    }
}
