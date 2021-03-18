package ntut.csie.sslab.opensource.visualizer.adapter.repository.github;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface GithubCommitRepositoryPeer extends CrudRepository<GithubCommitData, String> {
    List<GithubCommitData> findByRepoOwnerAndRepoName(String repoOwner, String repoName);
    List<GithubCommitData> findByRepoOwnerAndRepoNameAndCommittedDateAfter(String repoOwner, String repoName, Instant sinceTime);
    Optional<GithubCommitData> findFirstByRepoOwnerAndRepoNameOrderByCommittedDateDesc(String repoOwner, String repoName);
}
