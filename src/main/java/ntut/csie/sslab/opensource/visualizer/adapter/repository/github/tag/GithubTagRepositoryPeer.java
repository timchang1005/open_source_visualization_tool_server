package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.tag;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubTagRepositoryPeer extends CrudRepository<GithubTagData, String> {
    List<GithubTagData> findByRepoId(String repoId);
}