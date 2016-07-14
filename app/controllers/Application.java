package controllers;

import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.GitHubApiService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Application extends Controller {
    private GitHubApiService gitHubApiService;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public Application(GitHubApiService gitHubApiService, HttpExecutionContext httpExecutionContext) {
        this.gitHubApiService = gitHubApiService;
        this.httpExecutionContext = httpExecutionContext;
    }


    public CompletionStage<Result> healthcheck() {
        return CompletableFuture.supplyAsync(() -> ok("Ready for requests."));
    }


    public CompletionStage<Result> forks(Integer count, String hash, Integer page) {
        if (null != hash && !hash.isEmpty() && gitHubApiService.isHashExpired(hash)) {
            return CompletableFuture.supplyAsync(() -> redirect(request().path()), httpExecutionContext.current());
        }

        return gitHubApiService.getTopReposByForks(
                count,
                "/view/top/" + count + "/forks",
                hash,
                page
        ).thenApply(Results::ok);
    }

    public CompletionStage<Result> lastUpdated(Integer count, String hash, Integer page) {
        if (null != hash && !hash.isEmpty() && gitHubApiService.isHashExpired(hash)) {
            return CompletableFuture.supplyAsync(() -> redirect(request().path()), httpExecutionContext.current());
        }

        return gitHubApiService.getTopReposByLastUpdated(
                count,
                "/view/top/" + count + "/last_updated",
                hash,
                page
        ).thenApply(Results::ok);
    }

    public CompletionStage<Result> openIssues(Integer count, String hash, Integer page) {
        if (null != hash && !hash.isEmpty() && gitHubApiService.isHashExpired(hash)) {
            return CompletableFuture.supplyAsync(() -> redirect(request().path()), httpExecutionContext.current());
        }

        return gitHubApiService.getTopReposByOpenIssues(
                count,
                "/view/top/" + count + "/open_issues",
                hash,
                page
        ).thenApply(Results::ok);
    }

    public CompletionStage<Result> stars(Integer count, String hash, Integer page) {
        if (null != hash && !hash.isEmpty() && gitHubApiService.isHashExpired(hash)) {
            return CompletableFuture.supplyAsync(() -> redirect(request().path()), httpExecutionContext.current());
        }

        return gitHubApiService.getTopReposByStars(
                count,
                "/view/top/" + count + "/stars",
                hash,
                page
        ).thenApply(Results::ok);
    }

    public CompletionStage<Result> watchers(Integer count, String hash, Integer page) {
        if (null != hash && !hash.isEmpty() && gitHubApiService.isHashExpired(hash)) {
            return CompletableFuture.supplyAsync(() -> redirect(request().path()), httpExecutionContext.current());
        }

        return gitHubApiService.getTopReposByWatchers(
                count,
                "/view/top/" + count + "/watchers",
                hash,
                page
        ).thenApply(Results::ok);
    }

    public CompletionStage<Result> passthrough(String path) {
        return gitHubApiService.passthrough(path).thenApply(Results::ok);
    }
}
