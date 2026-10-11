package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.entity.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    Optional<ItemRequest> findByOwner_IdAndDescriptionIgnoreCase(Long ownerId, String description);

    List<ItemRequest> findAllByOwner_IdOrderByCreatedDesc(Long ownerId);

    List<ItemRequest> findAllByOwner_IdNotOrderByCreatedDesc(Long userId);
}
