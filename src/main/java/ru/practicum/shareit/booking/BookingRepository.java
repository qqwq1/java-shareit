package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.entity.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBooker_IdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBooker_IdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime time);

    List<Booking> findAllByBooker_IdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime time);

    List<Booking> findAllByBooker_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
            Long bookerId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Booking> findAllByItem_Owner_IdOrderByStartDesc(Long ownerId);

    List<Booking> findAllByItem_Owner_IdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    List<Booking> findAllByItem_Owner_IdAndEndIsBeforeOrderByStartDesc(Long ownerId, LocalDateTime time);

    List<Booking> findAllByItem_Owner_IdAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime time);

    List<Booking> findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
            Long ownerId,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Booking> findFirstByItem_IdAndStatusAndEndIsBeforeOrderByEndDesc(
            Long itemId,
            BookingStatus status,
            LocalDateTime time
    );

    @Query("""
            select b
            from Booking b
            where b.item.id in :itemIds
              and b.status = :status
              and b.end < :time
              and not exists (
                  select b2.id
                  from Booking b2
                  where b2.item.id = b.item.id
                    and b2.status = :status
                    and b2.end < :time
                    and (
                        b2.end > b.end
                        or (b2.end = b.end and b2.id > b.id)
                    )
              )
            order by b.item.id
            """)
    List<Booking> findLastBookingsByItemIds(
            @Param("itemIds") List<Long> itemIds,
            @Param("status") BookingStatus status,
            @Param("time") LocalDateTime time
    );

    @Query("""
            select b
            from Booking b
            where b.item.id in :itemIds
              and b.status = :status
              and b.start > :time
              and not exists (
                  select b2.id
                  from Booking b2
                  where b2.item.id = b.item.id
                    and b2.status = :status
                    and b2.start > :time
                    and (
                        b2.start < b.start
                        or (b2.start = b.start and b2.id < b.id)
                    )
              )
            order by b.item.id
            """)
    List<Booking> findNextBookingsByItemIds(
            @Param("itemIds") List<Long> itemIds,
            @Param("status") BookingStatus status,
            @Param("time") LocalDateTime time
    );

    Optional<Booking> findFirstByItem_IdAndStatusAndStartIsAfterOrderByStartAsc(
            Long itemId,
            BookingStatus status,
            LocalDateTime time
    );

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndIsBefore(
            Long itemId,
            Long bookerId,
            BookingStatus status,
            LocalDateTime time
    );

}
