package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.release;

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
public class GithubReleaseData {
    @Id
    @NotNull
    private String id;

    @NotNull
    private String repoId;

    @NotNull
    private String publisher;

    @NotNull
    private String tagName;

    @NotNull
    private Instant publishedAt;
}
