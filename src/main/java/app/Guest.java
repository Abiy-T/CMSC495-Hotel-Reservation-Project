package app;

/**
 *
 * @author ABIY
 */
public class Guest {

    private int guestid;
    private String firstname;
    private String lastname;
    private String address;
    private String email;

    public Guest() {
        guestid = 0;
        firstname = "";
        lastname = "";
        address = "";
        email = "";
    }

    public Guest(int guestid, String firstname, String lastname, String address, String email) {
        this.guestid = guestid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.address = address;
        this.email = email;
    }

    /**
     * @return the guestid
     */
    public int getGuestid() {
        return guestid;
    }

    /**
     * @param guestid the guestid to set
     */
    public void setGuestid(int guestid) {
        this.guestid = guestid;
    }

    /**
     * @return the firstname
     */
    public String getFistname() {
        return firstname;
    }

    /**
     * @param fistname the firstname to set
     */
    public void setFistname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * @return the lastname
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * @param lastname the lastname to set
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
