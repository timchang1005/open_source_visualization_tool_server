package ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest;

import ntut.csie.sslab.opensource.visualizer.usecase.common.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GithubPullRequestRepository extends Repository<GithubPullRequestDTO, String> {
    List<GithubPullRequestDTO> findByRepoId(String repoId);
    Optional<GithubPullRequestDTO> findLatest(String repoId);
    List<GithubPullRequestDTO> findSince(String repoId, Instant sinceTime);
}
