package facebook.server.service;

import facebook.server.dto.ContentDTO;
import facebook.server.entity.Content;
import facebook.server.entity.Tag;
import facebook.server.entity.User;
import facebook.server.repository.ContentRepository;
import facebook.server.utilities.JWTUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContentService extends AbstractService<Content, ContentRepository> {
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private StorageS3Service storageS3Service;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TagService tagService;

    @Override
    @Deprecated
    public Content save(Content entity) {
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken = authHeader.substring(7);

        User user = userService.getUserRepository()
                .findByEmail(jwtUtils.extractUsername(jwtToken))
                .orElseThrow(() -> new RuntimeException("User not found"));

        entity.setUser(user);
        entity.setVotes(new ArrayList<>());
        entity.setNrComments(0);
        entity.setNrVotes(0);
        entity.setTags(new ArrayList<>());

        return repository.save(entity);
    }

    public Content save(ContentDTO entityDTO, MultipartFile photo) throws Exception {
        User user = userService.getUserFromJWT();
        String imageHashed = "";
        // Extract content from DTO
        Content entity = entityDTO.getContent();

        // Check content type (true = post, false = comment)
        boolean isPost = entity.isTypeContent();

        if (!isPost) {
            if (entity.getParentContentId() == null) {
                throw new RuntimeException("Comments must have a parent content specified");
            }
            Content parentContent = repository.findById(entity.getParentContentId())
                    .orElseThrow(() -> new RuntimeException("Parent content not found"));

            parentContent.setNrComments(parentContent.getNrComments() + 1);
            repository.save(parentContent);
        }

        // Set required fields
        entity.setUser(user);
        entity.setVotes(new ArrayList<>());
        entity.setNrComments(0);
        entity.setNrVotes(0);
        entity.setCreatedAt(LocalDateTime.now());

        // Initialize tags if null
        if (entity.getTags() == null) {
            entity.setTags(new ArrayList<>());
        }

        if (!photo.isEmpty()) {
            // Generate unique image name and upload photo
            imageHashed = passwordEncoder.encode(System.currentTimeMillis() +
                    user.getUsername()).replace("/", ".");
            String url = storageS3Service.uploadFile(photo, imageHashed);
            entity.setUrlPhoto(url);
        }

        try {
            Content savedContent = repository.save(entity);

            for (Tag tagItem : entityDTO.getTag()) {
                if (tagItem.getName() != null && !tagItem.getName().isEmpty()) {
                    Tag tag = tagService.findByNameOrCreate(tagItem.getName());

                    // Add bidirectional relationship
                    if (tag.getContents() == null) {
                        tag.setContents(new ArrayList<>());
                    }
                    tag.getContents().add(savedContent);

                    if (!savedContent.getTags().contains(tag)) {
                        savedContent.getTags().add(tag);
                    }
                }
            }

            // Save again to update tags
            savedContent = repository.save(savedContent);
            return savedContent;
        } catch (Exception e) {
            // Delete uploaded file if saving fails
            if(!photo.isEmpty()) {
                storageS3Service.deleteFile(imageHashed);
            }
            throw new RuntimeException("Error while saving content with file: " + e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        Content content = repository.findById(id).get();
        User user = userService.getUserFromJWT();

        if (!content.getUser().getId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
            throw new RuntimeException("You are not allowed to delete this content");
        }
        repository.deleteById(id);
    }

    @Override
    public Content update(Long id, Content entity) {
        User user = userService.getUserFromJWT();

        if (!entity.getUser().getId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
            throw new RuntimeException("You are not allowed to update this content");
        }
        if(repository.findById(id).isEmpty())
            throw new RuntimeException("Content not found");

        Content target = repository.findById(id).get();
        target.setText(entity.getText());
        target.setTitle(entity.getTitle());
        
        // Handle tags properly
        List<Tag> updatedTags = new ArrayList<>();
        for (Tag tagItem : entity.getTags()) {
            if (tagItem.getName() != null && !tagItem.getName().isEmpty()) {
                Tag tag = tagService.findByNameOrCreate(tagItem.getName());
                updatedTags.add(tag);
            }
        }
        target.setTags(updatedTags);
        
        target.setCreatedAt(LocalDateTime.now());
        // Keep the existing photo URL if no new photo is provided
        if (entity.getUrlPhoto() == null) {
            target.setUrlPhoto(target.getUrlPhoto());
        } else {
            target.setUrlPhoto(entity.getUrlPhoto());
        }

        return repository.save(target);
    }

    public List<Content> getAllLimited(int cursor) throws IOException {
        Pageable pageable = PageRequest.of(cursor, 5,
                org.springframework.data.domain.Sort.by("createdAt").descending());
        return repository.findAllByTypeContentIsTrue(pageable).getContent();
    }

    public List<Content> getCommentsForContent(Long parentContentId) {
        return repository.findByParentContentId(parentContentId);
    }
    //comentariu
}
