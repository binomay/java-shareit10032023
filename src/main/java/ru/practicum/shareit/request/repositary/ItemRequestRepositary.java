package ru.practicum.shareit.request.repositary;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRequestRepositary extends PagingAndSortingRepository<ItemRequest, Integer> {

    List<ItemRequest> findAllByRequesterOrderById(User user);

    @Query(value = "SELECT IR FROM ItemRequest IR WHERE IR.requester.id <> ?1")
    List<ItemRequest> findAll(Integer userId, Pageable pageable);
}
