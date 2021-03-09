package ntut.csie.sslab.opensource.visualizer.entity.model.commit;

import lombok.Data;

import java.time.Instant;

@Data
public class Commit {
    String id;
    String repoName;
    String repoOwner;
    String author;
    Instant committedDate;
    int additions;
    int deletions;
}
