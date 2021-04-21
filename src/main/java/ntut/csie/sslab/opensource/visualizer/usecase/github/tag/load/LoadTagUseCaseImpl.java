package ntut.csie.sslab.opensource.visualizer.usecase.github.tag.load;

import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class LoadTagUseCaseImpl implements LoadTagUseCase {
    private final GithubAPICaller githubAPICaller;
    private final GithubTagRepository githubTagRepository;
    private final GithubRepoRepository githubRepoRepository;

    @Autowired
    public LoadTagUseCaseImpl(GithubAPICaller githubAPICaller, GithubTagRepository githubTagRepository, GithubRepoRepository githubRepoRepository) {
        this.githubAPICaller = githubAPICaller;
        this.githubTagRepository = githubTagRepository;
        this.githubRepoRepository = githubRepoRepository;
    }

    @Override
    public void execute(LoadTagInput input, Output output) {
        GithubRepoDTO repo = githubRepoRepository.findByOwnerAndName(input.getRepoOwner(), input.getRepoName()).get();
        if (repo == null) {
            repo = new GithubRepoDTO(UUID.randomUUID().toString(), input.getRepoOwner(), input.getRepoName());
            githubRepoRepository.save(repo);
        }

        try {
            List<GithubTagDTO> tagDTOs = githubAPICaller.getTags(repo.getId(), repo.getOwner(), repo.getName(), input.getAccessToken());
            githubTagRepository.save(tagDTOs);
            output.setExitCode(ExitCode.SUCCESS);
        } catch (Exception e) {
            output.setExitCode(ExitCode.FAILURE);
            output.setMessage(e.getMessage());
        }
    }

    @Override
    public LoadTagInput newInput() {
        return new LoadTagInputImpl();
    }

    private class LoadTagInputImpl implements LoadTagInput {
        private String repoOwner;
        private String repoName;
        private String accessToken;

        @Override
        public String getRepoOwner() {
            return repoOwner;
        }

        @Override
        public void setRepoOwner(String repoOwner) {
            this.repoOwner = repoOwner;
        }

        @Override
        public String getRepoName() {
            return repoName;
        }

        @Override
        public void setRepoName(String repoName) {
            this.repoName = repoName;
        }

        @Override
        public String getAccessToken() {
            return accessToken;
        }

        @Override
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
