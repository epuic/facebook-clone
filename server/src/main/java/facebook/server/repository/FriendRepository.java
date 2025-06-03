package facebook.server.repository;

import facebook.server.entity.Friend;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends AbstractRepository<Friend> {

    @Query("SELECT f FROM Friend f WHERE (f.user1.id = :user1Id AND f.user2.id = :user2Id) OR (f.user1.id = :user2Id AND f.user2.id = :user1Id)")
    Optional<Friend> findFriendByUser1_IdAndUser2_Id (@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    @Query("SELECT f FROM Friend f WHERE f.user1.id = :userId OR f.user2.id = :userId")
    List<Friend> findFriendshipsByUserId(@Param("userId") Long userId);
}