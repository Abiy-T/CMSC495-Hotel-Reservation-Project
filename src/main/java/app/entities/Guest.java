/*
 * Alex Dwivedi
 * 4/27/2019
 * CMSC 495
 */

package app.entities;

public class Guest {
    public final String firstName;
    public final String lastName;
    public final String address;
    public final String email;

    public Guest(String first, String last, String address, String email) {
        this.firstName = first;
        this.lastName = last;
        this.address = address;
        this.email = email;
    }
}
