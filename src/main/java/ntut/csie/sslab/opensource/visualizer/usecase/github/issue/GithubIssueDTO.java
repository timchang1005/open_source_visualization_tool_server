package ntut.csie.sslab.opensource.visualizer.usecase.github.issue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubIssueDTO {
    private String id;
    private String repoId;
    private int number;
    private String state;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;
}
