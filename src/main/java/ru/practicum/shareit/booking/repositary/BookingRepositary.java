package ru.practicum.shareit.booking.repositary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepositary extends JpaRepository<Booking, Integer> {
    Optional<Booking> findBookingById(Integer bookingId);

    List<Booking> findBookingByBookerOrderByStartDesc(User user);

    List<Booking> findBookingByBookerAndStatusOrderByStartDesc(User user, String state);

    List<Booking> findBookingByBookerAndStartBeforeAndEndAfterOrderByStartDesc(User user, LocalDateTime dateTime, LocalDateTime dateTime1);

    List<Booking> findBookingByBookerAndEndBeforeOrderByStartDesc(User user, LocalDateTime dateTime);

    List<Booking> findBookingByBookerAndStartAfterOrderByStartDesc(User user, LocalDateTime dateTime);

    @Query( nativeQuery = true, value = "SELECT B.* FROM BOOKINGS B, ITEMS I " +
            "WHERE I.owner_id = ?1 AND B.item_id = I.id AND B.STATUS = ?2 ORDER BY B.START_DATE DESC"
    )
    List<Booking> getBookingsForOwnerByStatus(Integer ownerId, String Status);

    @Query( nativeQuery = true, value = "SELECT B.* FROM BOOKINGS B, ITEMS I " +
            "WHERE I.owner_id = ?1 AND B.item_id = I.id  ORDER BY B.START_DATE DESC"
    )
    List<Booking> getAllBookingsForOwner(Integer ownerId);

    @Query( nativeQuery = true, value = "SELECT B.* FROM BOOKINGS B, ITEMS I " +
            "WHERE I.owner_id = ?1 AND B.item_id = I.id  AND " +
            "B.START_DATE < ?2 AND B.END_DATE > ?3 " +
            "ORDER BY B.START_DATE DESC"
    )
    List<Booking> getCurrenBookingForOwner(Integer ownerId, LocalDateTime date1, LocalDateTime date2);

    @Query( nativeQuery = true, value = "SELECT B.* FROM BOOKINGS B, ITEMS I " +
            "WHERE I.owner_id = ?1 AND B.item_id = I.id  AND " +
            "B.END_DATE < ?2 ORDER BY B.START_DATE DESC"
    )
    List<Booking> getPastBookingForOwner(Integer ownerId, LocalDateTime date);

    @Query( nativeQuery = true, value = "SELECT B.* FROM BOOKINGS B, ITEMS I " +
            "WHERE I.owner_id = ?1 AND B.item_id = I.id  AND " +
            "B.START_DATE > ?2 ORDER BY B.START_DATE DESC"
    )
    List<Booking> getFutureBookingForOwner(Integer ownerId, LocalDateTime date);

    @Query( nativeQuery = true, value = "SELECT B.* FROM bookings B, items I WHERE " +
            "I.id = ?1 AND I.id = B.item_id AND " +
            "B.STATUS =  'APPROVED' AND " +
            "I.owner_id = ?2  " +
            "ORDER BY end_date"
    )
    List<Booking> getBookingsByItemOwner(Integer itemId, Integer userId);
}
