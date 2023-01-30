package ru.practicum.shareit.item.repositary;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends PagingAndSortingRepository<Item, Integer> {
    Optional<Item> getItemById(Integer itemId);

    List<Item> findAllByOwner(User owner, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT * FROM ITEMS " +
            "WHERE (UPPER(NAME) LIKE '%' ||  ?1 || '%' OR " +
            "UPPER(DESCRIPTION) LIKE '%' ||  ?1 || '%') AND " +
            "IS_AVIALABLE = TRUE")
    List<Item> contextSearch(String context, Pageable pageable);

    @Query(value = "SELECT B.item FROM Booking B WHERE B.item.id = ?1 AND B.status = 'APPROVED' AND B.end <?2")
    List<Item> getItemsWasCompleteBookingByUser(Integer itemId, LocalDateTime dateTime);

    List<Item> findByRequestInOrderById(List<ItemRequest> itemRequestList);

}