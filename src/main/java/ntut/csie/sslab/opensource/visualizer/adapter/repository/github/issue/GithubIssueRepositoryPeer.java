package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.issue;

import ntut.csie.sslab.opensource.visualizer.adapter.repository.github.commit.GithubCommitData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface GithubIssueRepositoryPeer extends CrudRepository<GithubIssueData, String> {
    List<GithubIssueData> findByRepoId(String repoId);
    List<GithubIssueData> findByRepoIdAndCreatedAtAfter(String repoId, Instant sinceTime);
    Optional<GithubIssueData> findFirstByRepoIdOrderByUpdatedAtDesc(String repoId);
}
