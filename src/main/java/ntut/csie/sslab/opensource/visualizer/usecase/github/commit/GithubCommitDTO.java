package ntut.csie.sslab.opensource.visualizer.usecase.github.commit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubCommitDTO {
    String id;
    String repoOwner;
    String repoName;
    String author;
    Instant committedDate;
    int additions;
    int deletions;
}
