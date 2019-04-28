/*
 * Alex Dwivedi
 * 4/27/2019
 * CMSC 495
 */

package app.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Reservation {
    public final Room room;
    public final LocalDate checkInDate;
    public final LocalDate checkOutDate;
    public final BigDecimal total;
    public final int occupants;

    public Reservation (Room room, LocalDate checkIn, LocalDate checkOut,
                        BigDecimal total, int occupants) {
        this.room = room;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.total = total;
        this.occupants = occupants;
    }
}
