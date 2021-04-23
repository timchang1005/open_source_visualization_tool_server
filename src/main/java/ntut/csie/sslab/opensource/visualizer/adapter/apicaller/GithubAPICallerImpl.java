package ntut.csie.sslab.opensource.visualizer.adapter.apicaller;

import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.issue.GithubIssueDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.pullrequest.GithubPullRequestDTO;
import ntut.csie.sslab.opensource.visualizer.usecase.github.tag.GithubTagDTO;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

@Service
public class GithubAPICallerImpl implements GithubAPICaller {

    private static Instant retryAfter = Instant.EPOCH;
    private static WebClient webClient;

    @Autowired
    public GithubAPICallerImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                            .exchangeStrategies(ExchangeStrategies.builder()
                                .codecs(configurer ->
                                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                                )
                                .build())
                            .baseUrl("https://api.github.com")
                        .build();
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
                            String user = commitJSON.getJSONObject("committer").isNull("user") && commitJSON.getJSONObject("author").isNull("user") ?
                                    null :
                                    commitJSON.getJSONObject(
                                            commitJSON.getJSONObject("committer").isNull("user") ? "author" : "committer"
                                    ).getJSONObject("user").getString("login");
                            GithubCommitDTO commitDTO = new GithubCommitDTO(
                                    commitJSON.getString("oid"),
                                    repoId,
                                    user,
                                    Instant.parse(commitJSON.getString("committedDate")),
                                    commitJSON.getInt("additions"),
                                    commitJSON.getInt("deletions")
                            );

                            synchronized (lock) {
                                commitDTOs.add(commitDTO);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e.getCause());
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
                            sinceTime,
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
                } catch (Exception e) {
                    throw new RuntimeException(e.getCause());
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

    private List<GithubPullRequestDTO> getOpenPullRequests(String repoId, String repoOwner, String repoName, Instant sinceTime, String accessToken) throws JSONException, InterruptedException {
        String cursor = "";
        boolean hasNextPage = true;
        List<GithubPullRequestDTO> pullRequestsFromGithub = new ArrayList<>();
        while (hasNextPage) {
            JSONObject pullRequestsCompleteJSON = GithubAPIV4Caller.getOpenPullRequestsWithPagination(
                    repoOwner,
                    repoName,
                    cursor,
                    accessToken
            );
            JSONArray pullRequestsJSON = pullRequestsCompleteJSON.getJSONObject("data")
                    .getJSONObject("repository")
                    .getJSONObject("pullRequests")
                    .getJSONArray("edges");
            for (int i = 0; i < pullRequestsJSON.length(); i++) {
                JSONObject pullRequestJSON = pullRequestsJSON.getJSONObject(i).getJSONObject("node");
                String updatedAt = pullRequestJSON.getString("updatedAt");
                if (Instant.parse(updatedAt).isBefore(sinceTime)) {
                    hasNextPage = false;
                    break;
                }
                String id = pullRequestJSON.getString("id");
                int number = pullRequestJSON.getInt("number");
                String createdAt = pullRequestJSON.getString("createdAt");
                List<String> reviewers = new ArrayList<>();
                JSONArray requestedReviewersJSON = pullRequestJSON.getJSONObject("reviewRequests").getJSONArray("nodes");
                JSONArray reviewersJSON = pullRequestJSON.getJSONObject("reviews").getJSONArray("nodes");
                for (int j = 0; j < requestedReviewersJSON.length(); j++) {
                    reviewers.add(requestedReviewersJSON.getJSONObject(j).getJSONObject("requestedReviewer").getString("login"));
                }
                for (int j = 0; j < reviewersJSON.length(); j++) {
                    if (!reviewersJSON.getJSONObject(j).isNull("author")) {
                        reviewers.add(reviewersJSON.getJSONObject(j).getJSONObject("author").getString("login"));
                    }
                }

                GithubPullRequestDTO pullRequest = new GithubPullRequestDTO(
                        id,
                        repoId,
                        number,
                        "OPEN",
                        Instant.parse(createdAt),
                        Instant.parse(updatedAt),
                        null,
                        reviewers
                );

                pullRequestsFromGithub.add(pullRequest);
            }
            JSONObject pageInfo = pullRequestsCompleteJSON.getJSONObject("data")
                    .getJSONObject("repository")
                    .getJSONObject("pullRequests")
                    .getJSONObject("pageInfo");
            cursor = pageInfo.getString("endCursor");
            hasNextPage = hasNextPage && pageInfo.getBoolean("hasNextPage");
        }
        return pullRequestsFromGithub;
    }

    private List<GithubPullRequestDTO> getClosedPullRequests(String repoId, String repoOwner, String repoName, Instant sinceTime, String accessToken) throws JSONException, InterruptedException {
        final Object lock = new Object();
        List<GithubPullRequestDTO> pullRequestsFromGithub = new ArrayList<>();
        List<String> cursors = new ArrayList<>();
        cursors.add("");
        cursors.addAll(GithubAPIV4Caller.getClosedPullRequestsCursors(repoOwner, repoName, sinceTime, accessToken));
        List<Thread> closedPullRequestsLoaders = cursors.stream().map(cursor -> new Thread(() -> {
            List<GithubPullRequestDTO> pullRequestsReady = new ArrayList<>();
            try {
                JSONObject pullRequestsCompleteJSON = GithubAPIV4Caller.getClosedPullRequestsWithPagination(
                        repoOwner,
                        repoName,
                        cursor,
                        accessToken
                );
                JSONArray pullRequestsJSON = pullRequestsCompleteJSON.getJSONObject("data")
                        .getJSONObject("repository")
                        .getJSONObject("pullRequests")
                        .getJSONArray("nodes");
                for (int i = 0; i < pullRequestsJSON.length(); i++) {
                    JSONObject pullRequestJSON = pullRequestsJSON.getJSONObject(i);
                    String updatedAt = pullRequestJSON.getString("updatedAt");
                    if (Instant.parse(updatedAt).isBefore(sinceTime)) {
                        break;
                    }
                    String id = pullRequestJSON.getString("id");
                    int number = pullRequestJSON.getInt("number");
                    String createdAt = pullRequestJSON.getString("createdAt");
                    String closedAt = pullRequestJSON.getString("closedAt");
                    List<String> reviewers = new ArrayList<>();
                    JSONArray requestedReviewersJSON = pullRequestJSON.getJSONObject("reviewRequests").getJSONArray("nodes");
                    JSONArray reviewersJSON = pullRequestJSON.getJSONObject("reviews").getJSONArray("nodes");
                    for (int j = 0; j < requestedReviewersJSON.length(); j++) {
                        reviewers.add(requestedReviewersJSON.getJSONObject(j).getJSONObject("requestedReviewer").getString("login"));
                    }
                    for (int j = 0; j < reviewersJSON.length(); j++) {
                        if (!reviewersJSON.getJSONObject(j).isNull("author")) {
                            reviewers.add(reviewersJSON.getJSONObject(j).getJSONObject("author").getString("login"));
                        }
                    }
                    JSONArray closerJSON = pullRequestJSON.getJSONObject("timelineItems").getJSONArray("nodes");
                    if (closerJSON.length() != 0) {
                        if (!closerJSON.getJSONObject(0).isNull("actor")) {
                            reviewers.add(closerJSON.getJSONObject(0).getJSONObject("actor").getString("login"));
                        }
                    }

                    GithubPullRequestDTO pullRequest = new GithubPullRequestDTO(
                            id,
                            repoId,
                            number,
                            "CLOSED",
                            Instant.parse(createdAt),
                            Instant.parse(updatedAt),
                            Instant.parse(closedAt),
                            reviewers.stream().distinct().collect(Collectors.toList())
                    );

                    pullRequestsReady.add(pullRequest);
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getCause());
            }
            synchronized (lock) {
                pullRequestsFromGithub.addAll(pullRequestsReady);
            }
        })).collect(Collectors.toList());

        for (Thread thread : closedPullRequestsLoaders) {
            thread.start();
        }

        for (Thread thread : closedPullRequestsLoaders) {
            thread.join();
        }

        return pullRequestsFromGithub;
    }

    private List<GithubPullRequestDTO> getMergedPullRequests(String repoId, String repoOwner, String repoName, Instant sinceTime, String accessToken) throws JSONException, InterruptedException {
        final Object lock = new Object();
        List<GithubPullRequestDTO> pullRequestsFromGithub = new ArrayList<>();
        List<String> cursors = new ArrayList<>();
        cursors.add("");
        cursors.addAll(GithubAPIV4Caller.getMergedPullRequestsCursors(repoOwner, repoName, sinceTime, accessToken));
        List<Thread> closedPullRequestsLoaders = cursors.stream().map(cursor -> new Thread(() -> {
            List<GithubPullRequestDTO> pullRequestsReady = new ArrayList<>();
            try {
                JSONObject pullRequestsCompleteJSON = GithubAPIV4Caller.getMergedPullRequestsWithPagination(
                        repoOwner,
                        repoName,
                        cursor,
                        accessToken
                );
                JSONArray pullRequestsJSON = pullRequestsCompleteJSON.getJSONObject("data")
                        .getJSONObject("repository")
                        .getJSONObject("pullRequests")
                        .getJSONArray("nodes");
                for (int i = 0; i < pullRequestsJSON.length(); i++) {
                    JSONObject pullRequestJSON = pullRequestsJSON.getJSONObject(i);
                    String updatedAt = pullRequestJSON.getString("updatedAt");
                    if (Instant.parse(updatedAt).isBefore(sinceTime)) {
                        break;
                    }
                    String id = pullRequestJSON.getString("id");
                    int number = pullRequestJSON.getInt("number");
                    String createdAt = pullRequestJSON.getString("createdAt");
                    String closedAt = pullRequestJSON.getString("closedAt");
                    List<String> reviewers = new ArrayList<>();
                    JSONArray requestedReviewersJSON = pullRequestJSON.getJSONObject("reviewRequests").getJSONArray("nodes");
                    JSONArray reviewersJSON = pullRequestJSON.getJSONObject("reviews").getJSONArray("nodes");
                    for (int j = 0; j < requestedReviewersJSON.length(); j++) {
                        reviewers.add(requestedReviewersJSON.getJSONObject(j).getJSONObject("requestedReviewer").getString("login"));
                    }
                    for (int j = 0; j < reviewersJSON.length(); j++) {
                        if (!reviewersJSON.getJSONObject(j).isNull("author")) {
                            reviewers.add(reviewersJSON.getJSONObject(j).getJSONObject("author").getString("login"));
                        }
                    }
                    JSONArray closerJSON = pullRequestJSON.getJSONObject("timelineItems").getJSONArray("nodes");
                    if (closerJSON.length() != 0) {
                        if (!closerJSON.getJSONObject(0).isNull("actor")) {
                            reviewers.add(closerJSON.getJSONObject(0).getJSONObject("actor").getString("login"));
                        }
                    }

                    GithubPullRequestDTO pullRequest = new GithubPullRequestDTO(
                            id,
                            repoId,
                            number,
                            "MERGED",
                            Instant.parse(createdAt),
                            Instant.parse(updatedAt),
                            Instant.parse(closedAt),
                            reviewers.stream().distinct().collect(Collectors.toList())
                    );

                    pullRequestsReady.add(pullRequest);
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getCause());
            }
            synchronized (lock) {
                pullRequestsFromGithub.addAll(pullRequestsReady);
            }
        })).collect(Collectors.toList());

        for (Thread thread : closedPullRequestsLoaders) {
            thread.start();
        }

        for (Thread thread : closedPullRequestsLoaders) {
            thread.join();
        }

        return pullRequestsFromGithub;
    }

    @Override
    public List<GithubPullRequestDTO> getPullRequests(String repoId, String repoOwner, String repoName, Instant sinceTime, String accessToken) throws InterruptedException {
        final Object lock = new Object();
        List<GithubPullRequestDTO> result = new ArrayList<>();
        Thread getOpenPullRequestsThread = new Thread(() -> {
            List<GithubPullRequestDTO> pullRequests = new ArrayList<>();
            try {
                pullRequests = getOpenPullRequests(repoId, repoOwner, repoName, sinceTime, accessToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
            synchronized (lock) {
                result.addAll(pullRequests);
            }
        });
        Thread getClosedPullRequestThread = new Thread(() -> {
            List<GithubPullRequestDTO> pullRequests = new ArrayList<>();
            try {
                pullRequests = getClosedPullRequests(repoId, repoOwner, repoName, sinceTime, accessToken);
            } catch (JSONException | InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lock) {
                result.addAll(pullRequests);
            }
        });
        Thread getMergedPullRequestThread = new Thread(() -> {
            List<GithubPullRequestDTO> pullRequests = new ArrayList<>();
            try {
                pullRequests = getMergedPullRequests(repoId, repoOwner, repoName, sinceTime, accessToken);
            } catch (JSONException | InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lock) {
                result.addAll(pullRequests);
            }
        });

        getOpenPullRequestsThread.start();
        getClosedPullRequestThread.start();
        getMergedPullRequestThread.start();

        getOpenPullRequestsThread.join();
        getClosedPullRequestThread.join();
        getMergedPullRequestThread.join();
        return result;
    }

    @Override
    public List<GithubTagDTO> getTags(String repoId, String repoOwner, String repoName, String accessToken) throws JSONException, InterruptedException {
        String cursor = "";
        boolean hasNextPage = true;
        List<GithubTagDTO> tagsFromGithub = new ArrayList<>();
        while (hasNextPage) {
            JSONObject tagsCompleteJSON = GithubAPIV4Caller.getTagsInfoWithPagination(
                    repoOwner,
                    repoName,
                    cursor,
                    accessToken
            );
            JSONArray tagsJSON = tagsCompleteJSON.getJSONObject("data")
                    .getJSONObject("repository")
                    .getJSONObject("refs")
                    .getJSONArray("edges");
            for (int i = 0; i < tagsJSON.length(); i++) {
                JSONObject tagJSON = tagsJSON.getJSONObject(i).getJSONObject("node");
                String tagName = tagJSON.getString("name");
                String oid = tagJSON.getJSONObject("target").getString("oid");
                String tagger;
                String createdAt;
                if (tagJSON.getJSONObject("target").length() == 3) {
                    tagger = tagJSON.getJSONObject("target").getJSONObject("committer").isNull("user") ?
                            null :
                            tagJSON.getJSONObject("target").getJSONObject("committer").getJSONObject("user").getString("login");
                    createdAt = tagJSON.getJSONObject("target").getString("committedDate");
                } else {
                    tagger = tagJSON.getJSONObject("target").getJSONObject("tagger").isNull("user") ?
                            null :
                            tagJSON.getJSONObject("target").getJSONObject("tagger").getJSONObject("user").getString("login");
                    createdAt = tagJSON.getJSONObject("target").getJSONObject("tagger").getString("date");
                }
                GithubTagDTO tag = new GithubTagDTO(
                        oid,
                        repoId,
                        tagName,
                        tagger,
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(createdAt, Instant::from)
                );
                tagsFromGithub.add(tag);
            }
            JSONObject pageInfo = tagsCompleteJSON.getJSONObject("data")
                    .getJSONObject("repository")
                    .getJSONObject("refs")
                    .getJSONObject("pageInfo");
            cursor = pageInfo.getString("endCursor");
            hasNextPage = pageInfo.getBoolean("hasNextPage");
        }
        return tagsFromGithub;
    }

    private static class GithubAPIV4Caller {
        public static JSONObject getCommitTotalCountAndStartCursor(String repoOwner, String repoName, Instant sinceTime, String accessToken) throws JSONException, InterruptedException {
            String responseString;
            boolean firstRun = true;
            do {
                if (firstRun) {
                    firstRun = false;
                } else {
                    Thread.sleep(2000);
                }
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
                responseString = webClient.post()
                        .uri("/graphql")
                        .body(BodyInserters.fromObject(graphQL))
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                        .block();
            } while (responseString.contains("abuse"));
            return new JSONObject(responseString);
        }

        public static JSONObject getCommitsInfoWithCursor(String repoOwner, String repoName, int last, String cursor, String accessToken) throws JSONException, InterruptedException {
            String responseString;
            boolean firstRun = true;
            do {
                if (firstRun) {
                    firstRun = false;
                } else {
                    Thread.sleep(2000);
                }
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
                                "author {\n" +
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
                responseString = webClient.post()
                        .uri("/graphql")
                        .body(BodyInserters.fromObject(map))
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                        .block();
            } while (responseString.contains("abuse"));
            return new JSONObject(responseString);
        }

        public static JSONObject getTagsInfoWithPagination(String repoOwner, String repoName, String cursor, String accessToken) throws JSONException, InterruptedException {
            String responseString;
            boolean firstRun = true;
            do {
                if (firstRun) {
                    firstRun = false;
                } else {
                    Thread.sleep(2000);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("query",
                        "{repository(owner: \"" + repoOwner + "\", name:\"" + repoName + "\") {\n" +
                                "refs(refPrefix: \"refs/tags/\", first: 100, after: \"" + cursor + "\") {\n" +
                                "pageInfo {\n" +
                                "hasNextPage\n" +
                                "endCursor\n" +
                                "}\n" +
                                "edges {" +
                                "cursor\n" +
                                "node {\n" +
                                "name\n" +
                                "target {\n" +
                                "... on Tag {\n" +
                                "tagger {\n" +
                                "date\n" +
                                "user {\n" +
                                "login\n" +
                                "}\n" +
                                "}\n" +
                                "oid\n" +
                                "}\n" +
                                "... on Commit {\n" +
                                "committedDate\n" +
                                "committer {\n" +
                                "user {\n" +
                                "login\n" +
                                "}\n" +
                                "}\n" +
                                "oid\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}}");
                responseString = webClient.post()
                        .uri("/graphql")
                        .body(BodyInserters.fromObject(map))
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                        .block();
            } while (responseString.contains("abuse"));
            return new JSONObject(responseString);
        }

        public static JSONObject getOpenPullRequestsWithPagination(String repoOwner, String repoName, String cursor, String accessToken) throws JSONException, InterruptedException {
            String responseString;
            boolean firstRun = true;
            do {
                if (firstRun) {
                    firstRun = false;
                } else {
                    Thread.sleep(2000);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("query",
                        "{repository(owner: \"" + repoOwner + "\", name:\"" + repoName + "\") {\n" +
                                "pullRequests(states: OPEN, orderBy: {field: UPDATED_AT, direction: DESC}, first: 100" + (cursor.equals("") ? cursor : ", after: \"" + cursor + "\"") + ") {\n" +
                                "pageInfo {\n" +
                                "hasNextPage\n" +
                                "endCursor\n" +
                                "}\n" +
                                "edges {" +
                                "node {\n" +
                                "id\n" +
                                "number\n" +
                                "createdAt\n" +
                                "updatedAt\n" +
                                "closedAt\n" +
                                "reviewRequests(first: 100) {\n" +
                                "nodes {\n" +
                                "requestedReviewer {\n" +
                                "... on User {\n" +
                                "login\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "reviews(first: 100) {\n" +
                                "nodes {\n" +
                                "author {\n" +
                                "login" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}}");
                responseString = webClient.post()
                        .uri("/graphql")
                        .body(BodyInserters.fromObject(map))
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                        .block();
            } while (responseString.contains("abuse"));
            return new JSONObject(responseString);
        }

        public static JSONObject getClosedPullRequestsWithPagination(String repoOwner, String repoName, String cursor, String accessToken) throws JSONException, InterruptedException {
            String responseString;
            boolean firstRun = true;
            do {
                if (firstRun) {
                    firstRun = false;
                } else {
                    Thread.sleep(2000);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("query",
                        "{repository(owner: \"" + repoOwner + "\", name:\"" + repoName + "\") {\n" +
                                "pullRequests(states: CLOSED, orderBy: {field: UPDATED_AT, direction: DESC}, first: 100" + (cursor.equals("") ? cursor : ", after: \"" + cursor + "\"") + ") {\n" +
                                "pageInfo {\n" +
                                "hasNextPage\n" +
                                "endCursor\n" +
                                "}\n" +
                                "nodes {\n" +
                                "id\n" +
                                "number\n" +
                                "createdAt\n" +
                                "updatedAt\n" +
                                "closedAt\n" +
                                "reviewRequests(first: 100) {\n" +
                                "nodes {\n" +
                                "requestedReviewer {\n" +
                                "... on User {\n" +
                                "login\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "reviews(first: 100) {\n" +
                                "nodes {\n" +
                                "author {\n" +
                                "login" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "timelineItems(itemTypes: CLOSED_EVENT, first: 10) {\n" +
                                "nodes {\n" +
                                "... on ClosedEvent {\n" +
                                "actor {\n" +
                                "login" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}}");
                responseString = webClient.post()
                        .uri("/graphql")
                        .body(BodyInserters.fromObject(map))
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                        .block();
            } while (responseString.contains("abuse"));;
            return new JSONObject(responseString);
        }

        public static List<String> getClosedPullRequestsCursors(String repoOwner, String repoName, Instant sinceTime, String accessToken) throws JSONException, InterruptedException {
            String cursor = "";
            boolean hasNextPage = true;
            List<String> cursors = new ArrayList<>();
            while (hasNextPage) {
                Map<String, Object> map = new HashMap<>();
                map.put("query",
                        "{repository(owner: \"" + repoOwner + "\", name:\"" + repoName + "\") {\n" +
                            "pullRequests(states: CLOSED, orderBy: {field: UPDATED_AT, direction: DESC}, first: 100" + (cursor.equals("") ? cursor : ", after: \"" + cursor + "\"") + ") {\n" +
                                "pageInfo {\n" +
                                    "hasNextPage\n" +
                                    "endCursor\n" +
                                "}\n" +
                                "nodes {\n" +
                                    "updatedAt\n" +
                                "}\n" +
                            "}\n" +
                        "}}");
                String responseString = webClient.post()
                        .uri("/graphql")
                        .body(BodyInserters.fromObject(map))
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                        .block();
                if (responseString.contains("abuse")) {
                    Thread.sleep(2000);
                    continue;
                }
                JSONObject responseJSON = new JSONObject(responseString);
                JSONObject pageInfo = responseJSON.getJSONObject("data")
                        .getJSONObject("repository")
                        .getJSONObject("pullRequests")
                        .getJSONObject("pageInfo");
                JSONArray pullRequests = responseJSON.getJSONObject("data")
                        .getJSONObject("repository")
                        .getJSONObject("pullRequests")
                        .getJSONArray("nodes");
                hasNextPage = pageInfo.getBoolean("hasNextPage");
                for (int i = 0; i < pullRequests.length(); i++) {
                    String updatedAt = pullRequests.getJSONObject(i).getString("updatedAt");
                    if (Instant.parse(updatedAt).isBefore(sinceTime)) {
                        hasNextPage = false;
                        break;
                    }
                }
                if (hasNextPage) {
                    cursor = pageInfo.getString("endCursor");
                    cursors.add(cursor);
                }
            }
            return cursors;
        }

        public static List<String> getMergedPullRequestsCursors(String repoOwner, String repoName, Instant sinceTime, String accessToken) throws JSONException, InterruptedException {
            String cursor = "";
            boolean hasNextPage = true;
            List<String> cursors = new ArrayList<>();
            while (hasNextPage) {
                Map<String, Object> map = new HashMap<>();
                map.put("query",
                        "{repository(owner: \"" + repoOwner + "\", name:\"" + repoName + "\") {\n" +
                            "pullRequests(states: MERGED, orderBy: {field: UPDATED_AT, direction: DESC}, first: 100" + (cursor.equals("") ? cursor : ", after: \"" + cursor + "\"") + ") {\n" +
                                "pageInfo {\n" +
                                    "hasNextPage\n" +
                                    "endCursor\n" +
                                "}\n" +
                                "nodes {\n" +
                                    "updatedAt\n" +
                                "}\n" +
                            "}\n" +
                        "}}");
                String responseString = webClient.post()
                        .uri("/graphql")
                        .body(BodyInserters.fromObject(map))
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                        .block();
                if (responseString.contains("abuse")) {
                    Thread.sleep(2000);
                    continue;
                }
                JSONObject responseJSON = new JSONObject(responseString);
                JSONObject pageInfo = responseJSON.getJSONObject("data")
                        .getJSONObject("repository")
                        .getJSONObject("pullRequests")
                        .getJSONObject("pageInfo");
                JSONArray pullRequests = responseJSON.getJSONObject("data")
                        .getJSONObject("repository")
                        .getJSONObject("pullRequests")
                        .getJSONArray("nodes");
                hasNextPage = pageInfo.getBoolean("hasNextPage");
                for (int i = 0; i < pullRequests.length(); i++) {
                    String updatedAt = pullRequests.getJSONObject(i).getString("updatedAt");
                    if (Instant.parse(updatedAt).isBefore(sinceTime)) {
                        hasNextPage = false;
                        break;
                    }
                }
                if (hasNextPage) {
                    cursor = pageInfo.getString("endCursor");
                    cursors.add(cursor);
                }
            }
            return cursors;
        }

        public static JSONObject getMergedPullRequestsWithPagination(String repoOwner, String repoName, String cursor, String accessToken) throws JSONException, InterruptedException {
            String responseString;
            boolean firstRun = true;
            do {
                if (firstRun) {
                    firstRun = false;
                } else {
                    Thread.sleep(2000);
                }
                Map<String, Object> map = new HashMap<>();
                map.put("query",
                        "{repository(owner: \"" + repoOwner + "\", name:\"" + repoName + "\") {\n" +
                                "pullRequests(states: MERGED, orderBy: {field: UPDATED_AT, direction: DESC}, first: 100" + (cursor.equals("") ? cursor : ", after: \"" + cursor + "\"") + ") {\n" +
                                "pageInfo {\n" +
                                "hasNextPage\n" +
                                "endCursor\n" +
                                "}\n" +
                                "nodes {\n" +
                                "id\n" +
                                "number\n" +
                                "createdAt\n" +
                                "updatedAt\n" +
                                "closedAt\n" +
                                "reviewRequests(first: 100) {\n" +
                                "nodes {\n" +
                                "requestedReviewer {\n" +
                                "... on User {\n" +
                                "login\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "reviews(first: 100) {\n" +
                                "nodes {\n" +
                                "author {\n" +
                                "login" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "timelineItems(itemTypes: CLOSED_EVENT, first: 10) {\n" +
                                "nodes {\n" +
                                "... on ClosedEvent {\n" +
                                "actor {\n" +
                                "login" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}\n" +
                                "}}");
                responseString = webClient.post()
                        .uri("/graphql")
                        .body(BodyInserters.fromObject(map))
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                        .block();
            } while (responseString.contains("abuse"));
            return new JSONObject(responseString);
        }
    }

    private static class GithubAPIV3Caller {
        public static int getIssueAndPullRequestTotalCount(String repoOwner, String repoName, Instant sinceTime, String accessToken) {
            AtomicBoolean needToWait = new AtomicBoolean(false);
            String totalCount;
            do {
                totalCount = webClient.get()
                        .uri(String.format("/repos/%s/%s/issues?per_page=1&state=all&since=%s", repoOwner, repoName, sinceTime.toString()))
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                        .exchangeToMono(clientResponse -> {
                            needToWait.set(!clientResponse.headers().header("Retry-After").isEmpty());

                            List<String> links = clientResponse.headers().header("Link");
                            if (links.isEmpty()) {
                                return Mono.just("1");
                            } else {
                                String lastPage = Arrays.stream(links.get(0).split(", ")).filter(x -> x.contains("last")).findAny().get();
                                return Mono.just(StringUtils.substringBetween(lastPage, "&page=", ">;"));
                            }
                        })
                        .block();
            } while (needToWait.get());
            return parseInt(totalCount);
        }

        public static JSONArray getIssuesAndPullRequestsInfoWithPagination(String repoOwner, String repoName, Instant sinceTime, int page, String accessToken) throws JSONException, InterruptedException {
            String responseString;
            boolean firstRun = true;
            do {
                if (firstRun) {
                    firstRun = false;
                } else {
                    Thread.sleep(2000);
                }
                responseString = webClient.get()
                        .uri(String.format("/repos/%s/%s/issues?per_page=100&state=all&since=%s&page=%d", repoOwner, repoName, sinceTime.toString(), page))
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                        .block();
            } while (responseString.contains("abuse"));
            return new JSONArray(responseString);
        }
    }
}
