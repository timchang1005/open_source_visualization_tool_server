package ntut.csie.sslab.opensource.visualizer.adapter.controller.github;

import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.adapter.common.UseCaseOutput;
import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubCommitInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitUseCaseImpl;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path="/api/v1")
public class GithubCommitController {
    private final GithubAPICaller githubAPICaller;
    private final GithubCommitRepository githubCommitRepository;
    private final GithubRepoRepository githubRepoRepository;
    private final LoadCommitUseCase loadCommitUseCase;

    @Autowired
    public GithubCommitController(GithubAPICaller githubAPICaller, GithubCommitRepository githubCommitRepository, GithubRepoRepository githubRepoRepository) {
        this.githubAPICaller = githubAPICaller;
        this.githubCommitRepository = githubCommitRepository;
        this.githubRepoRepository = githubRepoRepository;
        this.loadCommitUseCase = new LoadCommitUseCaseImpl(githubAPICaller, githubCommitRepository, githubRepoRepository);
    }

    @GetMapping("/commits")
    public List<GithubCommitInfo> getCommits(@RequestParam String repoOwner,
                                             @RequestParam String repoName,
                                             @RequestParam(defaultValue = "1970-01-01T00:00:00Z") Instant sinceTime) {
        Optional<GithubRepoDTO> repo = githubRepoRepository.findByOwnerAndName(repoOwner, repoName);
        if(repo.isPresent()) {
            List<GithubCommitDTO> commits = githubCommitRepository.findSince(repo.get().getId(), sinceTime);
            return GithubCommitInfo.fromDTO(commits, repoOwner, repoName);
        }
        return new ArrayList<>();
    }

    @PostMapping("/commits")
    public Output loadCommits(@RequestBody LoadCommitRequest request) {
        LoadCommitInput input = loadCommitUseCase.newInput();
        Output output = new UseCaseOutput();

        input.setRepoOwner(request.getRepoOwner());
        input.setRepoName(request.getRepoName());
        input.setAccessToken(request.getAccessToken());

        loadCommitUseCase.execute(input, output);

        return output;
    }

    @Data
    private static class LoadCommitRequest {
        private String repoOwner;
        private String repoName;
        private String accessToken;
    }
}
