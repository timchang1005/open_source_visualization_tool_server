package ntut.csie.sslab.opensource.visualizer;

import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.load.LoadCommitUseCaseImpl;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueRepository;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.load.LoadIssueUseCase;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.load.LoadIssueUseCaseImpl;
import ntut.csie.sslab.opensource.visualizer.usecase.github.repo.GithubRepoRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = GithubConfig.class)
@TestPropertySource("classpath:test.properties")
@SpringBootTest
public abstract class AbstractUseCaseTest {

    @Autowired
    private GithubConfig githubConfig;

    @Autowired
    private GithubAPICaller githubAPICaller;

    @Autowired
    protected GithubCommitRepository githubCommitRepository;

    @Autowired
    protected GithubIssueRepository githubIssueRepository;

    @Autowired
    protected GithubRepoRepository githubRepoRepository;

    protected String getGithubApiToken() {
        return githubConfig.getApiToken();
    }

    protected LoadCommitUseCase newLoadCommitUseCase() {
        return new LoadCommitUseCaseImpl(githubAPICaller, githubCommitRepository, githubRepoRepository);
    }

    protected LoadIssueUseCase newLoadIssueUseCase() {
        return new LoadIssueUseCaseImpl(githubAPICaller, githubIssueRepository, githubRepoRepository);
    }
}
