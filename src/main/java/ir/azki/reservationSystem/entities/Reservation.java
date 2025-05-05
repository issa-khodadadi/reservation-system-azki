package ir.azki.reservationSystem.entities;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "reservations", uniqueConstraints = @UniqueConstraint(columnNames = "slot_id"))
public class Reservation extends CommonEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private AvailableSlot slot;
}

