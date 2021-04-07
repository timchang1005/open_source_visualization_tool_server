package ntut.csie.sslab.opensource.visualizer.usecase.github.issue;

import ntut.csie.sslab.opensource.visualizer.usecase.common.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GithubIssueRepository extends Repository<GithubIssueDTO, String> {
    List<GithubIssueDTO> findByRepoId(String repoId);
    List<GithubIssueDTO> findSince(String repoId, Instant sinceTime);
    Optional<GithubIssueDTO> findLatest(String repoId);
}
