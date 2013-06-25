package com.github.danrollo;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author Dan Rollo
 * Date: 6/24/13
 * Time: 8:39 PM
 */
public class RepoOps {

    static CredentialsProvider getCredentials(final String user, final String pwd) {
        return new UsernamePasswordCredentialsProvider(user, pwd);
    }

    /**
     * Use 1. init, 2. pull to avoid writing credentials or token in plain text in git files
     * See: bottom of page: https://github.com/blog/1270-easier-builds-and-deployments-using-git-over-https-and-oauth
     *
     * mkdir foo
     * cd foo
     * git init
     * git pull https://<token>@github.com/username/bar.git
     *
     *
     * @param repoURL
     * @param repoBasedir
     * @throws IOException
     * @throws GitAPIException
     */
    boolean doClone(final String repoURL, final File repoBasedir, final CredentialsProvider credentialsProvider) throws IOException, GitAPIException, URISyntaxException {


        //final Repository repo = new FileRepository(repoBasedir);


        final InitCommand initCommand = Git.init();
        initCommand.setDirectory(repoBasedir);
        initCommand.setBare(false);

        final Git git = initCommand.call();

        final StoredConfig storedConfig = git.getRepository().getConfig();
        // setup remote
        final RemoteConfig remoteConfig = new RemoteConfig(storedConfig, "origin");
        final URIish uri = new URIish(repoURL);
        remoteConfig.addURI(uri);

        final RefSpec refSpec = new RefSpec("refs/heads/master");
        remoteConfig.addFetchRefSpec(refSpec);

        remoteConfig.update(storedConfig);

        // need 'merge' setting, otherwise fails with: org.eclipse.jgit.api.errors.InvalidConfigurationException: No value for key branch.master.merge found in configuration
        storedConfig.setString("branch", "master", "merge", refSpec.getSource());

        storedConfig.save();


        final PullCommand pullCommand = git.pull();

        pullCommand.setCredentialsProvider(credentialsProvider);

        final ProgressMonitor progressMonitor = new ProgressMonitor() {
            final boolean isVerbose = false;

            @Override
            public void start(int totalTasks) {
                if (isVerbose) {
                    System.out.println("start: totalTasks:" + totalTasks);
                }
            }

            @Override
            public void beginTask(String title, int totalWork) {
                if (isVerbose) {
                    System.out.println(title + ": totalWork:" + totalWork);
                } else {
                    //System.out.print(title + "; ");
                }
            }

            @Override
            public void update(int completed) {
                if (isVerbose) {
                    System.out.println("completed:" + completed);
                }
            }

            @Override
            public void endTask() {
                if (isVerbose) {
                    System.out.println("task done");
                }
            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        };
        pullCommand.setProgressMonitor(progressMonitor);
        final PullResult pullResult = pullCommand.call();
        return pullResult.isSuccessful();
    }

    public void cloneAll(final File workDir, final String user, final String pwd) throws IOException, GitAPIException, URISyntaxException {
        createDir(workDir);

        final RepoList repoList = new RepoList(user, pwd);

        int count = 0;

        final List<User> orgs = repoList.getOrganizations();
        System.out.println("Total Org count: " + orgs.size());

        final CredentialsProvider credentialsProvider = getCredentials(user, pwd);

        for (final User org : orgs) {
            final List<Repository> orgRepos = repoList.getReposForOrg(org);
            final File baseDirOrg = new File(workDir, org.getLogin());
            createDir(baseDirOrg);
            System.out.println("Org: " + org.getLogin() + ", total org repos: " + orgRepos.size());


            for (final Repository repo : orgRepos) {
                System.out.println(repo.getCloneUrl());

                doClone(repo.getCloneUrl(), baseDirOrg, credentialsProvider);

                count++;
            }
        }


        final List<Repository> reposPublic = repoList.getReposPublic();
        final File baseDirPublic = new File(workDir, "public");
        createDir(baseDirPublic);
        System.out.println("Public repos: " + reposPublic.size());
        for (Repository repo : reposPublic) {
            System.out.println(repo.getCloneUrl());

            doClone(repo.getCloneUrl(), baseDirPublic, credentialsProvider);
            count++;
        }

        System.out.println("Total Repo count: " + count);

    }

    private void createDir(File dir) throws IOException {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("Error creating directory: " + dir.getCanonicalPath());
            } else {
                System.out.println("Created directory: " + dir.getCanonicalPath());
            }
        }
        if (!dir.isDirectory()) {
            throw new IllegalStateException("Should be a directory: " + dir.getCanonicalPath());
        }
    }

}
