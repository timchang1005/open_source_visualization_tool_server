package ntut.csie.sslab.opensource.visualizer.usecase.github.tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubTagDTO {
    private String id;
    private String repoId;
    private String name;
    private String tagger;
    private Instant createdAt;
}
