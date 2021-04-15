package ntut.csie.sslab.opensource.visualizer.adapter.controller.github;

import lombok.Data;
import ntut.csie.sslab.opensource.visualizer.adapter.common.UseCaseOutput;
import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubTagInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitUseCaseImpl;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path="/api/v1")
public class GithubTagController {
    private final GithubAPICaller githubAPICaller;
    private final GithubCommitRepository githubCommitRepository;
    private final GithubRepoRepository githubRepoRepository;
    private final LoadCommitUseCase loadCommitUseCase;

    @Autowired
    public GithubTagController(GithubAPICaller githubAPICaller, GithubCommitRepository githubCommitRepository, GithubRepoRepository githubRepoRepository) {
        this.githubAPICaller = githubAPICaller;
        this.githubCommitRepository = githubCommitRepository;
        this.githubRepoRepository = githubRepoRepository;
        this.loadCommitUseCase = new LoadCommitUseCaseImpl(githubAPICaller, githubCommitRepository, githubRepoRepository);
    }

    @GetMapping("/tags")
    public List<GithubTagInfo> getTags(@RequestParam String repoOwner,
                                       @RequestParam String repoName,
                                       @RequestParam(defaultValue = "1970-01-01T00:00:00Z") Instant sinceTime,
                                       @RequestBody GetTagsRequest request) throws InterruptedException {
        LoadCommitInput input = loadCommitUseCase.newInput();
        Output output = new UseCaseOutput();

        input.setRepoOwner(repoOwner);
        input.setRepoName(repoName);
        input.setAccessToken(request.getAccessToken());

        loadCommitUseCase.execute(input, output);

        List<GithubTagInfo> tagInfos = new ArrayList<>();
        if (output.getExitCode().equals(ExitCode.SUCCESS)) {
            GithubRepoDTO repo = githubRepoRepository.findByOwnerAndName(repoOwner, repoName).get();
            List<GithubTagDTO> tags = githubAPICaller.getTags(repo.getId(), repoOwner, repoName, request.getAccessToken());
            List<GithubCommitDTO> commits = githubCommitRepository.findByRepoId(repo.getId());
            tagInfos.addAll(GithubTagInfo.fromDTO(tags, repoOwner, repoName));
        }
        return tagInfos;
    }

    @Data
    private static class GetTagsRequest {
        private String accessToken;
    }
}
