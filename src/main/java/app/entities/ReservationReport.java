/*
 * Alex Dwivedi
 * 4/27/2019
 * CMSC 495
 */

package app.entities;

public class ReservationReport {
    public final String roomNumber;
    public final String occupants;
    public final String firstName;
    public final String lastName;
    public final String checkOutDate;
    public final String amenities;

    public ReservationReport(String rn, String occ, String fname,
                             String lname, String checkOut, String amenities) {
        this.roomNumber = rn;
        this.occupants = occ;
        this.firstName = fname;
        this.lastName = lname;
        this.checkOutDate = checkOut;
        this.amenities = amenities;
    }

    @Override
    public String toString() {
        return String.format("%4s %s %15s %15s %5s %s",
                roomNumber, occupants, firstName, lastName, checkOutDate, amenities);
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getOccupants() {
        return occupants;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public String getAmenities() {
        return amenities;
    }

}
