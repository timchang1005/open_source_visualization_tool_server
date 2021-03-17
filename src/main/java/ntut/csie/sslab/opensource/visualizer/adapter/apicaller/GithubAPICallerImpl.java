package ntut.csie.sslab.opensource.visualizer.adapter.apicaller;

import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;
import ntut.csie.sslab.opensource.visualizer.usecase.apicaller.GithubAPICaller;
import ntut.csie.sslab.opensource.visualizer.usecase.github.commit.GithubCommitDTO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GithubAPICallerImpl implements GithubAPICaller {

    private final WebClient webClient;

    @Autowired
    public GithubAPICallerImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.github.com").build();
    }

    @Override
    public GithubUserInfo getUserInfo(String userId, String accessToken) {
        return webClient.get()
                .uri(userId==null ? "/user" : String.format("/users/%s", userId))
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(accessToken);
                })
                .retrieve()
                .bodyToMono(GithubUserInfo.class)
                .block();
    }

    @Override
    public List<GithubCommitDTO> getCommits(String repoOwner, String repoName, Instant sinceTime, String accessToken) {
        Map<String, Object> graphQL = new HashMap<>();
        graphQL.put("query", "{repository(owner: \"" + repoOwner + "\", name:\"" + repoName + "\") {" +
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
        String responseString = this.webClient.post()
                .uri("/graphql")
                .body(BodyInserters.fromObject(graphQL))
                .headers(httpHeaders -> { httpHeaders.setBearerAuth(accessToken); })
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                .block();
        try {
            List<GithubCommitDTO> commitDTOs = new ArrayList<>();
            JSONObject responseInJSON = new JSONObject(responseString);
            JSONObject paginationInfo = responseInJSON.getJSONObject("data")
                    .getJSONObject("repository")
                    .getJSONObject("defaultBranchRef")
                    .getJSONObject("target")
                    .getJSONObject("history");
            int totalCount = paginationInfo.getInt("totalCount");
            if(totalCount != 0) {
                String cursor = paginationInfo.getJSONObject("pageInfo").getString("startCursor").split(" ")[0];
                for (int i = totalCount; i > 0; i-=100) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("query",
                            "{repository(owner: \"" + repoOwner + "\", name:\"" + repoName + "\") {\n" +
                                "defaultBranchRef {\n" +
                                    "target {\n" +
                                        "... on Commit {\n" +
                                            "history (last: " + Math.min(i, 100) + ", before: \"" + String.format("%s %d", cursor, i) +"\") {\n" +
                                                "nodes {\n" +
                                                    "oid\n" +
                                                    "committedDate\n" +
                                                    "additions\n" +
                                                    "deletions\n" +
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
                    String responseInString = this.webClient.post()
                            .uri("/graphql")
                            .body(BodyInserters.fromObject(map))
                            .headers(httpHeaders -> {
                                httpHeaders.setBearerAuth(accessToken);
                            })
                            .exchangeToMono(clientResponse ->
                                    clientResponse.bodyToMono(String.class))
                            .block();

                    JSONArray commitsJSON = (new JSONObject(responseInString)).getJSONObject("data")
                            .getJSONObject("repository")
                            .getJSONObject("defaultBranchRef")
                            .getJSONObject("target")
                            .getJSONObject("history")
                            .getJSONArray("nodes");
                    if (commitsJSON != null) {
                        for (int j = 0; j < commitsJSON.length(); j++) {
                            JSONObject commitJSON = commitsJSON.getJSONObject(j);
                            GithubCommitDTO data = new GithubCommitDTO(
                                    commitJSON.getString("oid"),
                                    repoOwner,
                                    repoName,
                                    commitJSON.getJSONObject("author").getJSONObject("user").getString("login"),
                                    Instant.parse(commitJSON.getString("committedDate")),
                                    commitJSON.getInt("additions"),
                                    commitJSON.getInt("deletions")
                            );
                            commitDTOs.add(data);
                        }
                    }
                }
            }
            return commitDTOs;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
