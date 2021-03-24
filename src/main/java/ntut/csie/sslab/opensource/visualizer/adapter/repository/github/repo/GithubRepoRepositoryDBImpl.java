package ntut.csie.sslab.opensource.visualizer.adapter.repository.github.repo;

import com.google.common.collect.Lists;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GithubRepoRepositoryDBImpl implements GithubRepoRepository {

    private final GithubRepoRepositoryPeer githubRepoRepositoryPeer;

    @Autowired
    public GithubRepoRepositoryDBImpl(GithubRepoRepositoryPeer githubRepoRepositoryPeer) {
        this.githubRepoRepositoryPeer = githubRepoRepositoryPeer;
    }

    @Override
    public List<GithubRepoDTO> findAll() {
        return GithubRepoMapper.transformToDTO(Lists.newArrayList(githubRepoRepositoryPeer.findAll()));
    }

    @Override
    public Optional<GithubRepoDTO> findById(String id) {
        Optional<GithubRepoData> data = githubRepoRepositoryPeer.findById(id);
        return Optional.ofNullable(data.isPresent() ? GithubRepoMapper.transformToDTO(data.get()) : null);
    }

    @Override
    public void save(GithubRepoDTO dto) {
        githubRepoRepositoryPeer.save(GithubRepoMapper.transformToData(dto));
    }

    @Override
    public void save(List<GithubRepoDTO> dtos) {
        githubRepoRepositoryPeer.saveAll(GithubRepoMapper.transformToData(dtos));
    }

    @Override
    public void deleteById(String id) {
        githubRepoRepositoryPeer.deleteById(id);
    }

    @Override
    public Optional<GithubRepoDTO> findByOwnerAndName(String owner, String name) {
        Optional<GithubRepoData> data = githubRepoRepositoryPeer.findByOwnerAndName(owner, name);
        return Optional.ofNullable(data.isPresent() ? GithubRepoMapper.transformToDTO(data.get()) : null);
    }
}
