package ntut.csie.sslab.opensource.visualizer.adapter.apicaller;

import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueDTO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.tuple.Tuple;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GithubAPICallerImpl implements GithubAPICaller {

    private static WebClient webClient;

    @Autowired
    public GithubAPICallerImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.github.com").build();
    }

    @Override
    public GithubUserInfo getUserInfo(String userId, String accessToken) {
        return webClient.get()
                .uri(userId==null ? "/user" : String.format("/users/%s", userId))
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GithubUserInfo.class)
                .block();
    }

    @Override
    public List<GithubCommitDTO> getCommits(String repoId, String repoOwner, String repoName, Instant sinceTime, String accessToken) throws JSONException, InterruptedException {
        JSONObject totalCountAndStartCursorJSON = GithubAPIV4Caller.getCommitTotalCountAndStartCursor(repoOwner, repoName, sinceTime, accessToken);
        JSONObject paginationInfo = totalCountAndStartCursorJSON.getJSONObject("data")
                .getJSONObject("repository")
                .getJSONObject("defaultBranchRef")
                .getJSONObject("target")
                .getJSONObject("history");

        final Object lock = new Object();
        List<Thread> githubCommitsLoaders = new ArrayList<>();
        List<GithubCommitDTO> commitDTOs = new ArrayList<>();
        String cursor = paginationInfo.getJSONObject("pageInfo").getString("startCursor").split(" ")[0];
        for (int i = paginationInfo.getInt("totalCount"); i > 1; i -= 100) {
            int finalI = i;
            Thread githubCommitLoader = new Thread(() -> {
                try {
                    JSONObject commitsJSON = GithubAPIV4Caller.getCommitsInfoWithCursor(
                            repoOwner,
                            repoName,
                            Math.min(finalI, 100),
                            String.format("%s %d", cursor, finalI),
                            accessToken);
                    JSONArray commits = commitsJSON
                            .getJSONObject("data")
                            .getJSONObject("repository")
                            .getJSONObject("defaultBranchRef")
                            .getJSONObject("target")
                            .getJSONObject("history")
                            .getJSONArray("nodes");
                    if (commits != null) {
                        for (int j = 0; j < commits.length(); j++) {
                            JSONObject commitJSON = commits.getJSONObject(j);
                            GithubCommitDTO data = new GithubCommitDTO(
                                    commitJSON.getString("oid"),
                                    repoId,
                                    commitJSON.getJSONObject("committer").isNull("user") ? null : commitJSON.getJSONObject("committer").getJSONObject("user").getString("login"),
                                    Instant.parse(commitJSON.getString("committedDate")),
                                    commitJSON.getInt("additions"),
                                    commitJSON.getInt("deletions")
                            );

                            synchronized (lock) {
                                commitDTOs.add(data);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            githubCommitsLoaders.add(githubCommitLoader);
            githubCommitLoader.start();
        }
        for (Thread thread : githubCommitsLoaders) {
            thread.join();
        }
        return commitDTOs;
    }

    @Override
    public List<GithubIssueDTO> getAllIssues(String repoId, String repoOwner, String repoName, String accessToken) {
        int totalCount = GithubAPIV4Caller.getIssueAndPullRequestTotalCount(repoOwner, repoName, accessToken);
        System.out.println(totalCount);
        return null;
    }

    @Override
    public List<GithubIssueDTO> getIssuesUpdatedSince(String repoId, String repoOwner, String repoName, Instant sinceTime, String accessToken) {
        return null;
    }

    private static class GithubAPIV4Caller {
        public static JSONObject getCommitTotalCountAndStartCursor(String repoOwner, String repoName, Instant sinceTime, String accessToken) throws JSONException {
            Map<String, Object> graphQL = new HashMap<>();
            graphQL.put("query",
                    "{repository(owner: \"" + repoOwner + "\", name:\"" + repoName + "\") {" +
                        "defaultBranchRef {" +
                            "target {" +
                                "... on Commit {" +
                                    "history (since: \"" + sinceTime.toString() + "\") {" +
                                        "totalCount\n" +
                                        "pageInfo {" +
                                            "startCursor" +
                                        "}" +
                                    "}" +
                                "}" +
                            "}" +
                        "}" +
                    "}}");
            String responseString = webClient.post()
                    .uri("/graphql")
                    .body(BodyInserters.fromObject(graphQL))
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                    .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                    .block();
            return new JSONObject(responseString);
        }

        public static JSONObject getCommitsInfoWithCursor(String repoOwner, String repoName, int last, String cursor, String accessToken) throws JSONException {
            Map<String, Object> map = new HashMap<>();
            map.put("query",
                    "{repository(owner: \"" + repoOwner + "\", name:\"" + repoName + "\") {\n" +
                        "defaultBranchRef {\n" +
                            "target {\n" +
                                "... on Commit {\n" +
                                    "history (last: " + last + ", before: \"" + cursor + "\") {\n" +
                                        "nodes {\n" +
                                            "oid\n" +
                                            "committedDate\n" +
                                            "additions\n" +
                                            "deletions\n" +
                                            "committer {\n" +
                                                "user {\n" +
                                                    "login\n" +
                                                "}\n" +
                                            "}\n" +
                                        "}\n" +
                                    "}\n" +
                                "}\n" +
                            "}\n" +
                        "}\n" +
                    "}}");
            String responseString = webClient.post()
                    .uri("/graphql")
                    .body(BodyInserters.fromObject(map))
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                    .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                    .block();
            return new JSONObject(responseString);
        }

        public static int getIssueAndPullRequestTotalCount(String repoOwner, String repoName, String accessToken) {
            Map<String, Object> graphQL = new HashMap<>();
            graphQL.put("query",
                        "{repository(owner: \"" + repoOwner + "\", name:\"" + repoName + "\") {" +
                            "issues {\n" +
                                "totalCount\n" +
                            "}\n" +
                            "pullRequests {\n" +
                                "totalCount\n" +
                            "}\n" +
                        "}}");
            String responseString = webClient.post()
                    .uri("/graphql")
                    .body(BodyInserters.fromObject(graphQL))
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                    .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                    .block();
            try {
                JSONObject totalCountJSON = new JSONObject(responseString);
                int issueTotalCount = totalCountJSON.getJSONObject("data")
                        .getJSONObject("repository")
                        .getJSONObject("issues")
                        .getInt("totalCount");
                int pullRequestTotalCount = totalCountJSON.getJSONObject("data")
                        .getJSONObject("repository")
                        .getJSONObject("pullRequests")
                        .getInt("totalCount");
                return issueTotalCount + pullRequestTotalCount;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }
}
