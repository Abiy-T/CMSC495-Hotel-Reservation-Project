/*
 * Alex Dwivedi
 * 4/27/2019
 * CMSC 495
 */

package app.entities;

import java.time.LocalDate;
import java.util.List;

public class Reservation {
    public final Room room;
    public final LocalDate checkInDate;
    public final LocalDate checkOutDate;
    public final int occupants;
    public final List<Amenity> amenities;

    public Reservation (Room room, LocalDate checkIn, LocalDate checkOut,
                        int occupants, List<Amenity> amenities) {
        this.room = room;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.occupants = occupants;
        this.amenities = amenities;
    }
}
