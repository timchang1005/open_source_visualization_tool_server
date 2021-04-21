package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.pullrequest;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubPullRequestRepositoryPeer extends CrudRepository<GithubPullRequestData, String> {
    List<GithubPullRequestData> findByRepoId(String repoId);
    Optional<GithubPullRequestData> findFirstByRepoIdOrderByUpdatedAtDesc(String repoId);
}
