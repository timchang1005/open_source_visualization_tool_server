package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GithubRepoRepositoryPeer extends CrudRepository<GithubRepoData, String> {
    Optional<GithubRepoData> findByOwnerAndName(String owner, String name);
}
