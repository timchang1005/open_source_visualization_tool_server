package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.release;

import com.google.common.collect.Lists;
import ntut.csie.sslab.opensource.visualizer.usecase.github.release.GithubReleaseDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.release.GithubReleaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GithubReleaseRepositoryDBImpl implements GithubReleaseRepository {
    private final GithubReleaseRepositoryPeer githubReleaseRepositoryPeer;


    @Autowired
    public GithubReleaseRepositoryDBImpl(GithubReleaseRepositoryPeer githubReleaseRepositoryPeer) {
        this.githubReleaseRepositoryPeer = githubReleaseRepositoryPeer;
    }

    @Override
    public List<GithubReleaseDTO> findAll() {
        return GithubReleaseMapper.transformToDTO(Lists.newArrayList(githubReleaseRepositoryPeer.findAll()));
    }

    @Override
    public Optional<GithubReleaseDTO> findById(String id) {
        Optional<GithubReleaseData> data = githubReleaseRepositoryPeer.findById(id);
        return Optional.ofNullable(data.isPresent() ? GithubReleaseMapper.transformToDTO(data.get()) : null);
    }

    @Override
    public void save(GithubReleaseDTO dto) {
        githubReleaseRepositoryPeer.save(GithubReleaseMapper.transformToData(dto));
    }

    @Override
    public void save(List<GithubReleaseDTO> dtos) {
        githubReleaseRepositoryPeer.saveAll(GithubReleaseMapper.transformToData(dtos));
    }

    @Override
    public void deleteById(String id) {
        githubReleaseRepositoryPeer.deleteById(id);
    }

    @Override
    public List<GithubReleaseDTO> findByRepoId(String repoId) {
        return GithubReleaseMapper.transformToDTO(githubReleaseRepositoryPeer.findByRepoId(repoId));
    }

    @Override
    public Optional<GithubReleaseDTO> findLatest(String repoId) {
        return Optional.empty();
    }
}
