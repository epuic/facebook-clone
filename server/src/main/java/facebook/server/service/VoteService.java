package facebook.server.service;

import facebook.server.entity.Content;
import facebook.server.entity.User;
import facebook.server.entity.Vote;
import facebook.server.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
public class VoteService extends AbstractService<Vote, VoteRepository>{
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private ContentService contentService;
    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Vote save(Vote vote) throws Exception {
        if (vote.getType() == null  || vote.getContent() == null) {
            throw new IllegalArgumentException("Vote type or content cannot be null");
        }

        Content content = contentService.getRepository().findById(vote.getContent().getId()).get();
        User user = userService.getUserFromJWT();
        if (content.getId() == null) {
            throw new IllegalArgumentException("Content ID cannot be null");
        }
        if(content.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User cannot vote on their own content");
        }

        // Check if user already voted
        Optional<Vote> existingVote = voteRepository.findByUserIdAndContentId(user.getId(), vote.getContent().getId());
        
        // If voting the same way, remove the vote
        if (existingVote.isPresent() && existingVote.get().getType().equals(vote.getType())) {
            deleteById(existingVote.get().getId());
            return null;
        }

        // If voting differently, remove old vote first
        if (existingVote.isPresent()) {
            deleteById(existingVote.get().getId());
        }

        // Add new vote
        vote.setUser(user);

        // Update vote count and score
        if (vote.getType().equals("UPVOTE")) {
            content.setNrVotes(content.getNrVotes() + 1);
            User contentUser = content.getUser();
            float score = content.isTypeContent() ? 2.5f : 5f;
            userService.updateScore(contentUser, score);
        } else if (vote.getType().equals("DOWNVOTE")) {
            content.setNrVotes(content.getNrVotes() - 1);
            User contentUser = content.getUser();
            float score = content.isTypeContent() ? -1.5f : -2.5f;
            userService.updateScore(contentUser, score);
            if(content.isTypeContent() == false){
                userService.updateScore(user, -1.5f);
            }
        }

        content.addVote(vote);
        vote.setContent(content);
        contentService.getRepository().save(content);
        return vote;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vote not found"));

        Content content = contentService.getRepository().findById(vote.getContent().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Content not found"));

        User user = userService.getUserFromJWT();
        if(!Objects.equals(user.getId(), vote.getUser().getId())) {
            throw new SecurityException("User ID does not match the JWT token");
        }

        // Update vote count and score
        if(vote.getType().equals("UPVOTE")) {
            content.setNrVotes(content.getNrVotes() - 1);
            User contentUser = content.getUser();
            float score = content.isTypeContent() ? -2.5f : -5f;
            userService.updateScore(contentUser, score);
        } else if (vote.getType().equals("DOWNVOTE")) {
            content.setNrVotes(content.getNrVotes() + 1);
            User contentUser = content.getUser();
            float score = content.isTypeContent() ? 1.5f : 2.5f;
            userService.updateScore(contentUser, score);
        }

        content.removeVote(vote);
        contentService.getRepository().save(content);
        super.deleteById(id);
    }
}
