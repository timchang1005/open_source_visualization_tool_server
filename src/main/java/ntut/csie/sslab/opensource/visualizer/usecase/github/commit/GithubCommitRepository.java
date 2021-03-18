package ntut.csie.sslab.opensource.visualizer.usecase.github.commit;

import ntut.csie.sslab.opensource.visualizer.usecase.common.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GithubCommitRepository extends Repository<GithubCommitDTO, String> {
    List<GithubCommitDTO> findBy(String repoOwner, String repoName);
    List<GithubCommitDTO> findSince(String repoOwner, String repoName, Instant sinceTime);
    Optional<GithubCommitDTO> findLastest(String repoOwner, String repoName);
}
