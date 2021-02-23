package ntut.csie.sslab.opensource.visualizer.adapter.apicaller;

import ntut.csie.sslab.opensource.visualizer.adapter.presenter.GithubUserInfo;

public interface GithubAPICaller {
    /**
     * Get user info from github
     *
     * @param userId  <code>null</code> for getting the caller's user info. Otherwise, get the info of user with userId given.
     */
    GithubUserInfo getUserInfo(String userId, String accessToken);
}
