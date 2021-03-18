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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path="/api/v1")
public class GithubCommitController {
    private final GithubAPICaller githubAPICaller;
    private final GithubCommitRepository githubCommitRepository;
    private final LoadCommitUseCase loadCommitUseCase;

    @Autowired
    public GithubCommitController(GithubAPICaller githubAPICaller, GithubCommitRepository githubCommitRepository) {
        this.githubAPICaller = githubAPICaller;
        this.githubCommitRepository = githubCommitRepository;
        this.loadCommitUseCase = new LoadCommitUseCaseImpl(githubAPICaller, githubCommitRepository);
    }

    @GetMapping("/commits")
    public List<GithubCommitInfo> getCommits(@RequestParam String repoOwner,
                                             @RequestParam String repoName,
                                             @RequestParam(defaultValue = "1970-01-01T00:00:00Z") Instant sinceTime) {
        List<GithubCommitDTO> commits =
                githubCommitRepository.findSince(repoOwner, repoName, sinceTime);

        return GithubCommitInfo.fromDTO(commits);
    }

    @PostMapping("/commits")
    public Output loadCommits(@RequestBody LoadCommitRequest request) {
        LoadCommitInput input = loadCommitUseCase.newInput();
        Output output = new UseCaseOutput();
        Optional<GithubCommitDTO> lastCommit = githubCommitRepository.findLastest(request.getRepoOwner(), request.getRepoName());

        input.setRepoOwner(request.getRepoOwner());
        input.setRepoName(request.getRepoName());
        input.setSinceTime(lastCommit.isPresent() ? lastCommit.get().getCommittedDate() : Instant.EPOCH);
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
