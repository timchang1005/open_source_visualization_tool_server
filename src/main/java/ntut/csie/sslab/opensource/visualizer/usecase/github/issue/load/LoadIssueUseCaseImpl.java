package ntut.csie.sslab.opensource.visualizer.usecase.github.issue.load;

import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LoadIssueUseCaseImpl implements LoadIssueUseCase {
    private final GithubAPICaller githubAPICaller;
    private final GithubIssueRepository githubIssueRepository;
    private final GithubRepoRepository githubRepoRepository;

    public LoadIssueUseCaseImpl(GithubAPICaller githubAPICaller, GithubIssueRepository githubIssueRepository, GithubRepoRepository githubRepoRepository) {
        this.githubAPICaller = githubAPICaller;
        this.githubIssueRepository = githubIssueRepository;
        this.githubRepoRepository = githubRepoRepository;
    }

    @Override
    public void execute(LoadIssueInput input, Output output) {
        GithubRepoDTO repo = githubRepoRepository.findByOwnerAndName(input.getRepoOwner(), input.getRepoName()).orElse(null);
        if (repo == null) {
            repo = new GithubRepoDTO(UUID.randomUUID().toString(), input.getRepoOwner(), input.getRepoName());
            githubRepoRepository.save(repo);
        }

        Optional<GithubIssueDTO> latestIssue = githubIssueRepository.findLatest(repo.getId());
        Instant sinceTime = latestIssue.isPresent() ? latestIssue.get().getUpdatedAt() : Instant.EPOCH;

        List<GithubIssueDTO> issueDTOs = null;
        try {
            issueDTOs = githubAPICaller.getIssues(repo.getId(), repo.getOwner(), repo.getName(), sinceTime, input.getAccessToken());
            githubIssueRepository.save(issueDTOs);
            output.setExitCode(ExitCode.SUCCESS);
        } catch (InterruptedException e) {
            output.setExitCode(ExitCode.FAILURE);
        }
    }

    @Override
    public LoadIssueInput newInput() {
        return new LoadIssueInputImpl();
    }

    private class LoadIssueInputImpl implements LoadIssueInput {
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
