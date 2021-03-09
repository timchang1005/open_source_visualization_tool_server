package ntut.csie.sslab.opensource.visualizer.adapter.repository.github;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubCommitData {
    String id;
    String repoOwner;
    String repoName;
    String author;
    Instant committedDate;
    int additions;
    int deletions;
}
