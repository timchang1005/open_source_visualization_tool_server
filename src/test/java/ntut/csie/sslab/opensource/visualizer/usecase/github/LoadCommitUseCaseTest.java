package ntut.csie.sslab.opensource.visualizer.usecase.github;

import ntut.csie.sslab.opensource.visualizer.AbstractUseCaseTest;
import ntut.csie.sslab.opensource.visualizer.adapter.common.UseCaseOutput;
import ntut.csie.sslab.opensource.visualizer.usecase.common.ExitCode;
import ntut.csie.sslab.opensource.visualizer.usecase.common.Output;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitInput;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitUseCase;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadCommitUseCaseTest extends AbstractUseCaseTest {

    @Test
    public void load_all_commit_from_github() {
        String repoOwner = "timchang1005";
        String repoName = "repo_for_testing";

        LoadCommitUseCase loadCommitUseCase = newLoadCommitUseCase();
        LoadCommitInput input = loadCommitUseCase.newInput();
        Output output = new UseCaseOutput();

        input.setRepoOwner(repoOwner);
        input.setRepoName(repoName);
//        input.setSinceTime(Instant.parse("2021-02-25T03:35:00.00Z"));
//        this would only read two commit
        input.setSinceTime(Instant.EPOCH);
        input.setAccessToken(getGithubApiToken());

        loadCommitUseCase.execute(input, output);

        assertEquals(ExitCode.SUCCESS, output.getExitCode());

        List<GithubCommitDTO> commitDTOs = githubCommitRepository.findBy(repoOwner, repoName);
        assertEquals(3, commitDTOs.size());
    }
}
