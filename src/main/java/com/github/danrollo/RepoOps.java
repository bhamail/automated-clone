package com.github.danrollo;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
     * @param repoURL Repository CloneUrl
     * @param repoCloneDir The local directory in which to create the clone (should include the repo name)
     * @throws GitAPIException
     */
    void doClone(final String repoURL, final File repoCloneDir,
                    final CredentialsProvider credentialsProvider) throws GitAPIException {


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


        final CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(repoURL);
        cloneCommand.setCredentialsProvider(credentialsProvider);
        cloneCommand.setDirectory(repoCloneDir);
        cloneCommand.setProgressMonitor(progressMonitor);
        //final Git git =
                cloneCommand.call();
    }

    public List<String> cloneAll(final File workDir, final String user, final String pwd) throws IOException {
        createDir(workDir);

        final File userCloneDir = new File(workDir, user);
        createDir(userCloneDir);

        final RepoList repoList = new RepoList(user, pwd);

        int count = 0;

        final List<User> orgs = repoList.getOrganizations();
        System.out.println("Total Org count: " + orgs.size());

        final CredentialsProvider credentialsProvider = getCredentials(user, pwd);

        final List<String> failures = new ArrayList<String>();

        for (final User org : orgs) {
            final List<Repository> orgRepos = repoList.getReposForOrg(org);
            final File baseDirOrg = new File(userCloneDir, org.getLogin());
            createDir(baseDirOrg);
            System.out.println("Org: " + org.getLogin() + ", total org repos: " + orgRepos.size());


            for (final Repository repo : orgRepos) {
                System.out.println(repo.getCloneUrl());

                final File repoCloneDir = new File(baseDirOrg, repo.getName());
                if (repoCloneDir.exists()) {
                    System.out.println("Dir exists, skipping: " + repoCloneDir);
                } else {
                    createDir(repoCloneDir);

                    try {
                        doClone(repo.getCloneUrl(), repoCloneDir, credentialsProvider);
                    } catch (Exception e) {
                        addFailure(failures, repo, repoCloneDir, e);
                    }
                }

                count++;
            }
        }


        final List<Repository> reposPublic = repoList.getReposPublic();
        final File baseDirPublic = new File(userCloneDir, "public");
        createDir(baseDirPublic);
        System.out.println("Public repos: " + reposPublic.size());
        for (Repository repo : reposPublic) {
            System.out.println(repo.getCloneUrl());

            final File repoCloneDir = new File(baseDirPublic, repo.getName());
            if (repoCloneDir.exists()) {
                System.out.println("Dir exists, skipping: " + repoCloneDir);
            } else {
                createDir(repoCloneDir);

                try {
                    doClone(repo.getCloneUrl(), repoCloneDir, credentialsProvider);
                } catch (Exception e) {
                    addFailure(failures, repo, repoCloneDir, e);
                }
            }

            count++;
        }

        System.out.println("Total Repo count: " + count);

        return failures;
    }

    void addFailure(final List<String> failures, final Repository failedRepo, final File repoCloneDir, final Exception cause) throws IOException {
        failures.add(failedRepo.getCloneUrl() + " -- " + repoCloneDir + " -- message: " + cause.getMessage());

        System.out.println("Clone failed!!! Deleting repoCloneDir: " + repoCloneDir + " -- message: " + cause.getMessage());
        // delete cloneDir, so subsequent attempts can succeed
        delete(repoCloneDir);
    }

    static void createDir(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Error creating directory: " + dir.getCanonicalPath());
        }
        if (!dir.isDirectory()) {
            throw new IllegalStateException("Should be a directory: " + dir.getCanonicalPath());
        }
    }

    // @todo replace with JDK 1.7 equivalent?
    static void delete(final File f) throws IOException {
        if (f.isDirectory()) {
            for (final File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }



}
