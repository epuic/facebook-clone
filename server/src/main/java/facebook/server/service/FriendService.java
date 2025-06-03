package facebook.server.service;

import facebook.server.entity.Friend;
import facebook.server.entity.User;
import facebook.server.repository.FriendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FriendService extends AbstractService<Friend, FriendRepository> {
    @Autowired
    private FriendRepository friendRepository;
    @Autowired
    private UserService userService;

    public Friend createOrUpdateFriendship(Long otherUserId) throws Exception {
        Long currentUserId = userService.getUserIdFromJWT();
        Optional<Friend> existingFriendship = friendRepository.findFriendByUser1_IdAndUser2_Id(currentUserId, otherUserId);

        Friend friendship = existingFriendship.orElseGet(() -> {
            // Create a new friendship if it doesn't exist
            Friend newFriendship = new Friend();
            newFriendship.setUser1(userService.getRepository().findById(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Current user not found")));
            newFriendship.setUser2(userService.getRepository().findById(otherUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Other user not found")));
            return newFriendship;
        });

        if(currentUserId.equals(friendship.getUser1().getId())) {
            friendship.setUser1Status(Friend.FriendshipStatus.ACCEPTED);
        }else{
            friendship.setUser2Status(Friend.FriendshipStatus.ACCEPTED);
        }
        return friendRepository.save(friendship);
    }

    public List<User> getFriendsMutual(){
        Long currentUserId = userService.getUserIdFromJWT();
        List<Friend> friends = friendRepository.findFriendshipsByUserId(currentUserId);
        friends = friends.stream()
                .filter(friend -> friend.getUser1Status() == Friend.FriendshipStatus.ACCEPTED &&
                        friend.getUser2Status() == Friend.FriendshipStatus.ACCEPTED)
                .toList();

        return friends.stream().map(friend -> friend.getUser1().getId().equals(currentUserId)
                ? friend.getUser2() : friend.getUser1()).toList();
    }

    public List<User> getFriendsPending(){
        Long currentUserId = userService.getUserIdFromJWT();
        List<Friend> friends = friendRepository.findFriendshipsByUserId(currentUserId);
        friends = friends.stream()
                .filter(friend -> friend.getUser1().getId().equals(currentUserId)
                        ? friend.getUser1Status() == Friend.FriendshipStatus.ACCEPTED &&
                        friend.getUser2Status() == Friend.FriendshipStatus.PENDING
                        : friend.getUser2Status() == Friend.FriendshipStatus.ACCEPTED &&
                        friend.getUser1Status() == Friend.FriendshipStatus.PENDING)
                .toList();

        return friends.stream().map(friend -> friend.getUser1().getId().equals(currentUserId)
                ? friend.getUser2() : friend.getUser1()).toList();
    }

    public List<User> getRequestFriends(){
        Long currentUserId = userService.getUserIdFromJWT();
        List<Friend> friends = friendRepository.findFriendshipsByUserId(currentUserId);
        friends = friends.stream()
                .filter(friend -> friend.getUser1().getId().equals(currentUserId)
                        ? friend.getUser1Status() == Friend.FriendshipStatus.PENDING &&
                        friend.getUser2Status() == Friend.FriendshipStatus.ACCEPTED
                        : friend.getUser2Status() == Friend.FriendshipStatus.PENDING &&
                        friend.getUser1Status() == Friend.FriendshipStatus.ACCEPTED)
                .toList();

        return friends.stream().map(friend -> friend.getUser1().getId().equals(currentUserId)
                ? friend.getUser2() : friend.getUser1()).toList();
    }

    public void rejectFriendship(Long otherUserId) throws Exception {
        Long currentUserId = userService.getUserIdFromJWT();
        Optional<Friend> existingFriendship = friendRepository.findFriendByUser1_IdAndUser2_Id(currentUserId, otherUserId);

        if (existingFriendship.isPresent()) {
            Friend friendship = existingFriendship.get();

            boolean isUser1 = currentUserId.equals(friendship.getUser1().getId());
            friendship.setUser1Status(isUser1 ? Friend.FriendshipStatus.REJECTED : Friend.FriendshipStatus.PENDING);
            friendship.setUser2Status(isUser1 ? Friend.FriendshipStatus.PENDING : Friend.FriendshipStatus.REJECTED);
            friendRepository.save(friendship);
        } else {
            throw new Exception("Friendship not found");
        }
    }

}
