package ru.practicum.shareit.booking.repositary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
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

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 AND B.status = ?2 order by B.start desc")
    List<Booking> getBookingsForOwnerByStatus(Integer ownerId, String status);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 ORDER BY B.start DESC")
    List<Booking> getAllBookingsForOwner(Integer ownerId);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 AND B.start < ?2 AND B.end > ?3 ORDER BY B.start DESC")
    List<Booking> getCurrentBookingForOwner(Integer ownerId, LocalDateTime date1, LocalDateTime date2);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 AND B.end < ?2 ORDER BY B.start DESC")
    List<Booking> getPastBookingForOwner(Integer ownerId, LocalDateTime date);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 AND B.start > ?2 ORDER BY B.start DESC")
    List<Booking> getFutureBookingForOwner(Integer ownerId, LocalDateTime date);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.id = ?1 AND B.status = 'APPROVED' AND B.item.owner.id = ?2 " +
            "ORDER BY B.end")
    List<Booking> getBookingsByItemOwner(Integer itemId, Integer userId);

    List<Booking> findAllByItemInAndStatusOrderByStart(List<Item> itemList, String status);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.id = ?1 AND " +
            " ((B.start > ?2 AND B.start < ?3) OR (B.end > ?2 AND B.end < ?3)) AND " +
            "B.status <> 'REJECTED'")
    List<Booking> getBookingWithSameDates(Integer itemId, LocalDateTime startDate, LocalDateTime endDate);

}
