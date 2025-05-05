package ir.azki.reservationSystem.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "available_slots",
        indexes = {
                @Index(name = "idx_reserved", columnList = "isReserved"),
                @Index(name = "idx_start_time", columnList = "startTime")
        })
public class AvailableSlot extends CommonEntity {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isReserved = false;

    @Version
    private Long version;

    public AvailableSlot(LocalDateTime of, LocalDateTime of1, boolean b) {
        this.startTime = of;
        this.endTime = of1;
        this.isReserved = b;
    }
}
