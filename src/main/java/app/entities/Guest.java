/*
 * Alex Dwivedi
 * 4/27/2019
 * CMSC 495
 */

package app.entities;

public class Guest {
    public final String firstName;
    public final String lastName;
    public final String email;

    public Guest(String first, String last, String email) {
        this.firstName = first;
        this.lastName = last;
        this.email = email;
    }
}
