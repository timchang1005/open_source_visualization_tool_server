package ntut.csie.sslab.opensource.visualizer.usecase.github.commit;

import ntut.csie.sslab.opensource.visualizer.usecase.common.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GithubCommitRepository extends Repository<GithubCommitDTO, String> {
    List<GithubCommitDTO> findByRepoId(String repoId);
    List<GithubCommitDTO> findSince(String repoId, Instant sinceTime);
    Optional<GithubCommitDTO> findLatest(String repoId);
}
