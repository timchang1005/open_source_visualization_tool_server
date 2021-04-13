package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.release;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubReleaseRepositoryPeer extends CrudRepository<GithubReleaseData, String> {
    List<GithubReleaseData> findByRepoId(String repoId);
}
