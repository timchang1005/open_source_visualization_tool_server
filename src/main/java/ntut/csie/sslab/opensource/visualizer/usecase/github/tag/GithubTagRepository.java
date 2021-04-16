package ntut.csie.sslab.opensource.visualizer.usecase.github.tag;

import ntut.csie.sslab.opensource.visualizer.usecase.common.Repository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;

import java.time.Instant;
import java.util.List;

public interface GithubTagRepository extends Repository<GithubTagDTO, String> {
    List<GithubTagDTO> findByRepoId(String repoId);
    List<GithubTagDTO> findSince(String repoId, Instant sinceTime);
}
