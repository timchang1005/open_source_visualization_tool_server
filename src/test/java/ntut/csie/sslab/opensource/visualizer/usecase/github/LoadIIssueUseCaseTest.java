package ntut.csie.sslab.opensource.visualizer.usecase.github;

import ntut.csie.sslab.opensource.visualizer.AbstractUseCaseTest;
import ntut.csie.sslab.opensource.visualizer.adapter.common.UseCaseOutput;
import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.load.LoadIssueInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.load.LoadIssueUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadIIssueUseCaseTest extends AbstractUseCaseTest {

    @Test
    public void load_all_issues_from_github() {
        String repoOwner = "timchang1005";
        String repoName = "repo_for_testing";

        LoadIssueUseCase loadIssueUseCase = newLoadIssueUseCase();
        LoadIssueInput input = loadIssueUseCase.newInput();
        Output output = new UseCaseOutput();

        input.setRepoOwner(repoOwner);
        input.setRepoName(repoName);
        input.setAccessToken(getGithubApiToken());

        loadIssueUseCase.execute(input, output);

        assertEquals(ExitCode.SUCCESS, output.getExitCode());

        GithubRepoDTO repo = githubRepoRepository.findByOwnerAndName(repoOwner, repoName).get();
        List<GithubIssueDTO> issueDTOs = githubIssueRepository.findByRepoId(repo.getId());
        assertEquals(3, issueDTOs.size());
    }
}
