package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.tag;

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
public class GithubTagData {

    @Id
    @NotNull
    private String id;

    @NotNull
    private String repoId;

    @NotNull
    private String name;

    @NotNull
    private String tagger;

    @NotNull
    private Instant createdAt;
}
