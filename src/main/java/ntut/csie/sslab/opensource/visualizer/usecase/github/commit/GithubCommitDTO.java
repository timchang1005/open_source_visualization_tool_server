package ntut.csie.sslab.opensource.visualizer.usecase.github.commit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubCommitDTO {
    private String id;
    private String repoOwner;
    private String repoName;
    private String author;
    private Instant committedDate;
    private int additions;
    private int deletions;
}
