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
    private String repoId;
    private String committer;
    private Instant committedDate;
    private int additions;
    private int deletions;
}
