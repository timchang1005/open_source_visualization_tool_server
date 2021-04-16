package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.tag;

import com.google.common.collect.Lists;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class GithubTagRepositoryDBImpl implements GithubTagRepository {

    private final GithubTagRepositoryPeer githubTagRepositoryPeer;

    @Autowired
    public GithubTagRepositoryDBImpl(GithubTagRepositoryPeer githubTagRepositoryPeer) {
        this.githubTagRepositoryPeer = githubTagRepositoryPeer;
    }

    @Override
    public List<GithubTagDTO> findAll() {
        return GithubTagMapper.transformToDTO(Lists.newArrayList(githubTagRepositoryPeer.findAll()));
    }

    @Override
    public Optional<GithubTagDTO> findById(String id) {
        Optional<GithubTagData> data = githubTagRepositoryPeer.findById(id);
        return Optional.ofNullable(data.isPresent() ? GithubTagMapper.transformToDTO(data.get()) : null);
    }

    @Override
    public void save(GithubTagDTO tagDTO) {
        githubTagRepositoryPeer.save(GithubTagMapper.transformToData(tagDTO));
    }

    @Override
    public void save(List<GithubTagDTO> tagDTOs) {
        githubTagRepositoryPeer.saveAll(GithubTagMapper.transformToData(tagDTOs));
    }

    @Override
    public void deleteById(String id) {
        githubTagRepositoryPeer.deleteById(id);
    }

    @Override
    public List<GithubTagDTO> findByRepoId(String repoId) {
        return GithubTagMapper.transformToDTO(githubTagRepositoryPeer.findByRepoId(repoId));
    }

    @Override
    public List<GithubTagDTO> findSince(String repoId, Instant sinceTime) {
        List<GithubTagData> tags = githubTagRepositoryPeer.findByRepoIdAndCreatedAtAfter(repoId, sinceTime);
        return GithubTagMapper.transformToDTO(tags);
    }
}
