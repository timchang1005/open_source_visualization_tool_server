package ntut.csie.sslab.opensource.visualizer.usecase.github.repo;

import ntut.csie.sslab.opensource.visualizer.usecase.common.Repository;

import java.util.Optional;

public interface GithubRepoRepository extends Repository<GithubRepoDTO, String> {
    Optional<GithubRepoDTO> findByOwnerAndName(String owner, String name);
}

