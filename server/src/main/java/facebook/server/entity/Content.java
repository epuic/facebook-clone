package facebook.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "content")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "content_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = true)
    private String title;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "type", nullable = false)
    private boolean typeContent;

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "content_tags",
            joinColumns = @JoinColumn(name = "content_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties("contents")
    private List<Tag> tags = new ArrayList<>();

    private String urlPhoto;

    @Column(name = "nr_comments", nullable = false)
    private int nrComments;

    @Column(name = "nr_votes", nullable = false)
    private int nrVotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "parent_content_id", nullable = true)
    private Long parentContentId;

    public void addVote(Vote vote) {
        if (votes == null) {
            votes = new ArrayList<>();
        }
        votes.add(vote);
    }
    public void removeVote(Vote vote) {
        if (votes != null) {
            votes.remove(vote);
        }
    }
}