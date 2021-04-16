package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.tag;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface GithubTagRepositoryPeer extends CrudRepository<GithubTagData, String> {
    List<GithubTagData> findByRepoId(String repoId);
    List<GithubTagData> findByRepoIdAndCreatedAtAfter(String repoId, Instant sinceTime);
}
