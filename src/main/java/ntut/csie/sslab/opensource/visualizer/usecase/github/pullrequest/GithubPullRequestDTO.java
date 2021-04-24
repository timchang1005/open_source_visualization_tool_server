package ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubPullRequestDTO {
    private String id;
    private String repoId;
    private int number;
    private String state;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;
}
