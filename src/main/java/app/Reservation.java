package app;


import java.util.Date;

/**
 *
 * @author ABIY
 */
public class Reservation {

    private int reservationid;
    private int guestid;
    private Date checkindate;
    private Date checkoutdate;
    private char Roomtype;
    private int numberofguest;
    private char Amenities;

    public Reservation() {
        reservationid = 0;
        guestid = 0;
        checkindate = null;
        checkoutdate = null;
        Roomtype = 0;
        numberofguest = 0;
        Amenities = 0;

    }

    public Reservation(int reservationid, int guestid, Date checkindate, Date checkoutdate, char Roomtype, int nuberofguest,
            char Amenities) {
        this.reservationid = reservationid;
        this.guestid = guestid;
        this.checkindate = checkindate;
        this.checkoutdate = checkoutdate;
        this.Roomtype = Roomtype;
        this.numberofguest = numberofguest;
        this.Amenities = Amenities;

    }

    //getter and setter
    /**
     * @return the reservationid
     */
    public int getreservationid() {
        return reservationid;
    }

    /**
     * @param reservationid the reservationid to set
     */
    public void setreservationid(int reservationid) {
        this.reservationid = reservationid;
    }

    /**
     * @return the guestid
     */
    public int getguestid() {
        return guestid;
    }

    /**
     * @param guestid the guestid to set
     */
    public void setguestid(int guestid) {
        this.guestid = guestid;
    }

    /**
     * @return the checkindate
     */
    public Date getCheckindate() {
        return checkindate;
    }

    /**
     * @param checkindate the checkindate to set
     */
    public void setCheckindate(Date checkindate) {
        this.checkindate = checkindate;
    }

    /**
     * @return the checkoutdate
     */
    public Date getCheckoutdate() {
        return checkoutdate;
    }

    /**
     * @param checkoutdate the checkoutdate to set
     */
    public void setCheckoutdate(Date checkoutdate) {
        this.checkoutdate = checkoutdate;
    }

    /**
     * @return the Roomtype
     */
    public char getRoomtype() {
        return Roomtype;
    }

    /**
     * @param Roomtype the Roomtype to set
     */
    public void setRoomtype(char Roomtype) {
        this.Roomtype = Roomtype;
    }

    /**
     * @return the numberofguest
     */
    public int getNumberofguest() {
        return numberofguest;
    }

    /**
     * @param numberofguest the numberofguest to set
     */
    public void setNumberofguest(int numberofguest) {
        this.numberofguest = numberofguest;
    }

    /**
     * @return the Amenities
     */
    public char getAmenities() {
        return Amenities;
    }

    /**
     * @param Amenities the Amenities to set
     */
    public void setAmenities(char Amenities) {
        this.Amenities = Amenities;
    }

}
