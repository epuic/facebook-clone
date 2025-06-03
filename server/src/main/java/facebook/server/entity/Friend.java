package facebook.server.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "friend")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "friend_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id_1")
    @JsonProperty("user_1")
    private User user1;

    @OneToOne
    @JoinColumn(name = "user_id_2")
    @JsonProperty("user_2")
    private User user2;

    @Column(name = "status1")
    @Enumerated(EnumType.STRING)
    private FriendshipStatus user1Status = FriendshipStatus.PENDING;

    @Column(name = "status2")
    @Enumerated(EnumType.STRING)
    private FriendshipStatus user2Status = FriendshipStatus.PENDING;

    public enum FriendshipStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}