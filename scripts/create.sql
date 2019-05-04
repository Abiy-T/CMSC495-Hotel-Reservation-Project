ALTER TABLE APP.RESERVATIONAMENITIES DROP CONSTRAINT FK_RESERVATIONAMENITIES_AMENITY;                                           
ALTER TABLE APP.RESERVATIONAMENITIES DROP CONSTRAINT FK_RESERVATIONAMENITIES_RESERVATION;
ALTER TABLE APP.RESERVATIONS DROP CONSTRAINT FK_RESERVATIONS_GUEST;                                                              
ALTER TABLE APP.RESERVATIONS DROP CONSTRAINT FK_RESERVATIONS_ROOM;                                                               
ALTER TABLE APP.ROOMS DROP CONSTRAINT FK_ROOMS_TYPE;                                                                            
                                                                                                                                  
DROP TABLE APP.RESERVATIONAMENITIES;    
DROP TABLE APP.AMENITIES;   
DROP TABLE APP.RESERVATIONS;                                                                                               
DROP TABLE APP.ROOMS;                                                                                                           
DROP TABLE APP.ROOMTYPES;  
DROP TABLE APP.EMPLOYEES;                                                                                                     
DROP TABLE APP.GUESTS; 

create table Guests (
    guest_id integer not null generated always as identity (start with 1, increment by 1),
    first_name varchar(99) not null,
    last_name varchar(99) not null,
    email varchar(99) not null,
    constraint PK_Guests primary key (guest_id)
);

create table Employees (
    employee_id integer not null generated always as identity (start with 1, increment by 1),
    password varchar(20) not null,
    constraint PK_Employees primary key (employee_id)
);

create table RoomTypes (
    type_id integer not null generated always as identity (start with 1, increment by 1),
    description varchar(15) not null unique,
    max_occupants integer not null,
    price_per_day decimal(5, 2) not null,
    constraint PK_RoomTypes primary key (type_id)
);

create table Rooms (
    room_id integer not null,
    type_id integer not null,
    constraint PK_Rooms primary key (room_id),
    constraint FK_Rooms_Type foreign key (type_id) references RoomTypes(type_id) on delete cascade
);

create table Reservations (
    reservation_id integer not null generated always as identity (start with 1, increment by 1),
    guest_id integer not null,
    room_id integer not null,
    check_in_date date not null,
    check_out_date date not null,
    occupants integer not null,
    constraint PK_Reservations primary key (reservation_id),
    constraint FK_Reservations_Guest foreign key (guest_id) references Guests(guest_id) on delete cascade,
    constraint FK_Reservations_Room foreign key (room_id) references Rooms(room_id) on delete cascade
);

create table Amenities (
    amenity_id integer not null generated always as identity (start with 1, increment by 1),
    description varchar(20) not null unique,
    price_per_day decimal(5, 2) not null,
    constraint PK_Amenities primary key (amenity_id)
);

create table ReservationAmenities (
    reservation_id integer not null,
    amenity_id integer not null,
    constraint PK_ReservationAmenities primary key (reservation_id, amenity_id),
    constraint FK_ReservationAmenities_Reservation 
        foreign key (reservation_id) references Reservations(reservation_id) on delete cascade,
    constraint FK_ReservationAmenities_Amenity
        foreign key (amenity_id) references Amenities(amenity_id) on delete cascade
);
