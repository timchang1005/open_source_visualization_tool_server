package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.pullrequest;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubPullRequestData {

    @Id
    @NotNull
    private String id;

    @NotNull
    private String repoId;

    @NotNull
    private int number;

    @NotNull
    private String state;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;

    private Instant closedAt;
}
