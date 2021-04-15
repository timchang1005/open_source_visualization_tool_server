package ntut.csie.sslab.opensource.visualizer.usecase.github.tag;

import ntut.csie.sslab.opensource.visualizer.usecase.common.Repository;

import java.util.List;

public interface GithubTagRepository extends Repository<GithubTagDTO, String> {
    List<GithubTagDTO> findByRepoId(String repoId);
}
