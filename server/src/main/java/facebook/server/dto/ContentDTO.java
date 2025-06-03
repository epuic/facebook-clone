package facebook.server.dto;

import facebook.server.entity.Content;
import facebook.server.entity.Tag;
import lombok.Data;

import java.util.List;

@Data
public class ContentDTO {
    private Content content;
    private List<Tag> tag;
}
