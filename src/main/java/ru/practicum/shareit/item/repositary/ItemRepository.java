package ru.practicum.shareit.item.repositary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    Optional<Item> getItemById(Integer itemId);

    List<Item> findAllByOwnerOrderById(User owner);

    @Query(nativeQuery = true, value = "SELECT * FROM ITEMS " +
            "WHERE (UPPER(NAME) LIKE '%' ||  ?1 || '%' OR " +
            "UPPER(DESCRIPTION) LIKE '%' ||  ?1 || '%') AND " +
            "IS_AVIALABLE = TRUE")
    List<Item> contextSearch(String context);

    @Query(value = "SELECT B.item FROM Booking B WHERE B.item.id = ?1 AND B.status = 'APPROVED' AND B.end <?2")
    List<Item> getItemsWasCompleteBookingByUser(Integer itemId, LocalDateTime dateTime);

}