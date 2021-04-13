package ntut.csie.sslab.opensource.visualizer.usecase.github.release;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubReleaseDTO {
    private String id;
    private String repoId;
    private String publisher;
    private String tagName;
    private Instant publishedAt;
}
