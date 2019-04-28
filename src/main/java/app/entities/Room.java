/*
 * Alex Dwivedi
 * 4/27/2019
 * CMSC 495
 */

package app.entities;

import java.math.BigDecimal;

public class Room {
    public final int number;
    public final String description;
    public final int maxOccupants;
    public final BigDecimal pricePerDay;

    public Room(int number, String desc, int maxOccupants, BigDecimal pricePerDay) {
        this.number = number;
        this.description = desc;
        this.maxOccupants = maxOccupants;
        this.pricePerDay = pricePerDay;
    }

    @Override
    public String toString() {
        return description;
    }

}
