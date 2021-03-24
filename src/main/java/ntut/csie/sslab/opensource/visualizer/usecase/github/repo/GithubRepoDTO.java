package ntut.csie.sslab.opensource.visualizer.usecase.github.repo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubRepoDTO {
    private String id;
    private String owner;
    private String name;
}
