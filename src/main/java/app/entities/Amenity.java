/*
 * Alex Dwivedi
 * 4/27/2019
 * CMSC 495
 */

package app.entities;

import java.math.BigDecimal;

public class Amenity {
    public final int id;
    public final String description;
    public final BigDecimal pricePerDay;

    public Amenity(int id, String desc, BigDecimal price) {
        this.id = id;
        this.description = desc;
        this.pricePerDay = price;
    }

    @Override
    public String toString() {
        return description;
    }
}
