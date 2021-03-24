package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.repo;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubRepoData {

    @Id
    @NotNull
    private String id;

    @NotNull
    private String owner;

    @NotNull
    private String name;
}
