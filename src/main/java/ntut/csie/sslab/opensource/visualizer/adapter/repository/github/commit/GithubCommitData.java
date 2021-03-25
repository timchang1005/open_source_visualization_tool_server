package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.commit;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubCommitData {

    @Id
    @NotNull
    private String id;

    @NotNull
    private String repoId;

    private String committer;

    @NotNull
    private Instant committedDate;

    @NotNull
    private int additions;

    @NotNull
    private int deletions;
}
