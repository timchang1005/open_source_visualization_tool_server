package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.commit;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface GithubCommitRepositoryPeer extends CrudRepository<GithubCommitData, String> {
    List<GithubCommitData> findByRepoId(String repoId);
    List<GithubCommitData> findByRepoIdAndCommittedDateAfter(String repoId, Instant sinceTime);
    Optional<GithubCommitData> findFirstByRepoIdOrderByCommittedDateDesc(String repoId);
}
