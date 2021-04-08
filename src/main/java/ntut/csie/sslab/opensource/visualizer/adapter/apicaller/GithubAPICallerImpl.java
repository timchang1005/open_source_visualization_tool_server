package ntut.csie.sslab.opensource.visualizer.adapter.apicaller;

import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueDTO;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

import static java.lang.Integer.parseInt;

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
        List<Thread> githubCommitLoaders = new ArrayList<>();
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
                            GithubCommitDTO commitDTO = new GithubCommitDTO(
                                    commitJSON.getString("oid"),
                                    repoId,
                                    commitJSON.getJSONObject("committer").isNull("user") ? null : commitJSON.getJSONObject("committer").getJSONObject("user").getString("login"),
                                    Instant.parse(commitJSON.getString("committedDate")),
                                    commitJSON.getInt("additions"),
                                    commitJSON.getInt("deletions")
                            );

                            synchronized (lock) {
                                commitDTOs.add(commitDTO);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            githubCommitLoaders.add(githubCommitLoader);
            githubCommitLoader.start();
        }
        for (Thread thread : githubCommitLoaders) {
            thread.join();
        }
        return commitDTOs;
    }

    @Override
    public List<GithubIssueDTO> getIssues(String repoId, String repoOwner, String repoName, Instant sinceTime, String accessToken) throws InterruptedException {
        int totalCount = GithubAPIV3Caller.getIssueAndPullRequestTotalCount(repoOwner, repoName, sinceTime, accessToken);

        final Object lock = new Object();
        List<GithubIssueDTO> issueDTOs = new ArrayList<>();
        List<Thread> githubIssueLoaders = new ArrayList<>();
        for (int i = 1; i <= (totalCount/100)+1; i++) {
            int finalI = i;
            Thread githubIssueLoader = new Thread(() -> {
                try {
                    JSONArray issuesJSON = GithubAPIV3Caller.getIssuesAndPullRequestsInfoWithPagination(
                            repoOwner,
                            repoName,
                            finalI,
                            accessToken
                    );
                    for (int j = 0; j < issuesJSON.length(); j++) {
                        JSONObject issueJSON = issuesJSON.getJSONObject(j);
                        if (!issueJSON.has("pull_request")) {
                            GithubIssueDTO issueDTO = new GithubIssueDTO(
                                    issueJSON.getString("node_id"),
                                    repoId,
                                    issueJSON.getInt("number"),
                                    issueJSON.getString("state"),
                                    Instant.parse(issueJSON.getString("created_at")),
                                    Instant.parse(issueJSON.getString("updated_at")),
                                    issueJSON.getString("closed_at").equals("null") ? null : Instant.parse(issueJSON.getString("closed_at"))
                            );
                            synchronized (lock) {
                                issueDTOs.add(issueDTO);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            githubIssueLoaders.add(githubIssueLoader);
            githubIssueLoader.start();
        }

        for (Thread thread : githubIssueLoaders) {
            thread.join();
        }

        return issueDTOs;
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
    }

    private static class GithubAPIV3Caller {
        public static int getIssueAndPullRequestTotalCount(String repoOwner, String repoName, Instant sinceTime, String accessToken) {
            String totalCount = webClient.get()
                    .uri(String.format("/repos/%s/%s/issues?per_page=1&state=all&sinceTime=%s", repoOwner, repoName, sinceTime.toString()))
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                    .exchangeToMono(clientResponse -> {
                        List<String> links = clientResponse.headers().header("Link");
                        if (links.isEmpty()) {
                            return Mono.just("1");
                        } else {
                            String lastPage = Arrays.stream(links.get(0).split(", ")).filter(x -> x.contains("last")).findAny().get();
                            return Mono.just(StringUtils.substringBetween(lastPage, "&page=", ">;"));
                        }
                    })
                    .block();
            return parseInt(totalCount);
        }

        public static JSONArray getIssuesAndPullRequestsInfoWithPagination(String repoOwner, String repoName, int page, String accessToken) throws JSONException {
            String responseString = webClient.get()
                    .uri(String.format("/repos/%s/%s/issues?per_page=100&state=all&page=%d", repoOwner, repoName, page))
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                    .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                    .block();
            return new JSONArray(responseString);
        }
    }
}
