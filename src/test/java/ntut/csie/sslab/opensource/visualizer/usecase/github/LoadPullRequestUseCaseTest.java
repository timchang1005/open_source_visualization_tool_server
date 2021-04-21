package ntut.csie.sslab.opensource.visualizer.usecase.github;

import ntut.csie.sslab.opensource.visualizer.AbstractUseCaseTest;
import ntut.csie.sslab.opensource.visualizer.adapter.common.UseCaseOutput;
import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.GithubPullRequestDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.load.LoadPullRequestInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.load.LoadPullRequestUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadPullRequestUseCaseTest extends AbstractUseCaseTest {

    @Test
    public void load_all_pull_requests_from_github() {
        String repoOwner = "timchang1005";
        String repoName = "repo_for_testing";

        LoadPullRequestUseCase loadPullRequestUseCase = newLoadPullRequestUseCase();
        LoadPullRequestInput input = loadPullRequestUseCase.newInput();
        Output output = new UseCaseOutput();

        input.setRepoOwner(repoOwner);
        input.setRepoName(repoName);
        input.setAccessToken(getGithubApiToken());

        loadPullRequestUseCase.execute(input, output);

        assertEquals(ExitCode.SUCCESS, output.getExitCode());

        GithubRepoDTO repo = githubRepoRepository.findByOwnerAndName(repoOwner, repoName).get();
        List<GithubPullRequestDTO> pullRequestDTOs = githubPullRequestRepository.findByRepoId(repo.getId());
        assertEquals(1, pullRequestDTOs.size());
    }
}
