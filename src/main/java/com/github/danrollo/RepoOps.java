package com.github.danrollo;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
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
public final class RepoOps {

    private static final ProgressMonitor progressMonitor = new ProgressMonitor() {
        private static final boolean IS_VERBOSE = false;

        @Override
        public void start(final int totalTasks) {
            if (IS_VERBOSE) {
                System.out.println("start: totalTasks:" + totalTasks);
            }
        }

        @Override
        public void beginTask(final String title, final int totalWork) {
            if (IS_VERBOSE) {
                System.out.println(title + ": totalWork:" + totalWork);
//                } else {
//                    System.out.print(title + "; ");
            }
        }

        @Override
        public void update(final int completed) {
            if (IS_VERBOSE) {
                System.out.println("completed:" + completed);
            }
        }

        @Override
        public void endTask() {
            if (IS_VERBOSE) {
                System.out.println("task done");
            }
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    };



    /**
     * @param user the repository user name
     * @param pwd the repository password
     * @return a CredentialProvider for the given credentials
     */
    static CredentialsProvider getCredentials(final String user,
                                              final String pwd) {
        return new UsernamePasswordCredentialsProvider(user, pwd);
    }

    /**
     * Clone the given repository.
     *
     * @param repoURL Repository CloneUrl
     * @param repoDir The local directory in which to create the clone
     *                     (should include the repo name)
     * @param credentialsProvider credentialsProvider for the given repository.
     * @throws GitAPIException if an error occurs in a GIT API call.
     */
    void doClone(final String repoURL, final File repoDir,
                    final CredentialsProvider credentialsProvider)
            throws GitAPIException {

        final CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(repoURL);
        cloneCommand.setCredentialsProvider(credentialsProvider);
        cloneCommand.setDirectory(repoDir);
        cloneCommand.setProgressMonitor(progressMonitor);
        //final Git git =
                cloneCommand.call();
    }


    /**
     * @param repoDir The local directory in which to pull
     * @param credentialsProvider credentialsProvider for the given repository.
     * @return true if pull succeeded
     * @throws IOException if an error occurs opening the local repository.
     * @throws GitAPIException if an error occurs in a GIT API call.
     */
    boolean doPull(final File repoDir, final CredentialsProvider credentialsProvider) throws IOException, GitAPIException {
        final Git git = Git.open(repoDir);

        final PullCommand pullCommand = git.pull();
        pullCommand.setProgressMonitor(progressMonitor);
        pullCommand.setCredentialsProvider(credentialsProvider);
        final PullResult pullResult = pullCommand.call();
        return pullResult.isSuccessful();
    }

    /**
     * Only called after a 'pull' fails.
     *
     * @param repoDir The local directory in which to fetch
     * @param credentialsProvider credentialsProvider for the given repository.
     * @throws IOException if an error occurs opening the local repository.
     * @throws GitAPIException if an error occurs in a GIT API call.
     */
    void doFetch(final File repoDir, final CredentialsProvider credentialsProvider) throws IOException, GitAPIException {
        final Git git = Git.open(repoDir);

        final FetchCommand fetchCommand = git.fetch();
        fetchCommand.setProgressMonitor(progressMonitor);
        fetchCommand.setCredentialsProvider(credentialsProvider);
        //final FetchResult fetchResult =
                fetchCommand.call();
    }


    /**
     * @param workDir base directory in which all other folders will be
     *                located.
     * @param user repository user name
     * @param pwd repository password
     * @return a list of error messages, which should be empty if no errors
     * occurred.
     * @throws IOException if an IO error occurs
     */
    public List<String> cloneOrUpdateAll(final File workDir, final String user,
                                         final String pwd) throws IOException {
        createDir(workDir);

        final File userCloneDir = new File(workDir, user);
        createDir(userCloneDir);

        final RepoList repoList = new RepoList(user, pwd);

        int count = 0;

        final List<User> orgs = repoList.getOrganizations();
        System.out.println("Total Org count: " + orgs.size());

        final CredentialsProvider credentialsProvider
                = getCredentials(user, pwd);

        final List<String> failures = new ArrayList<String>();

        for (final User org : orgs) {
            final List<Repository> orgRepos = repoList.getReposForOrg(org);
            final File baseDirOrg = new File(userCloneDir, org.getLogin());
            createDir(baseDirOrg);
            System.out.println("Org: " + org.getLogin()
                    + ", total org repos: " + orgRepos.size());


            for (final Repository repo : orgRepos) {

                final File repoDir = new File(baseDirOrg, repo.getName());
                cloneOrUpdate(credentialsProvider, failures, repo, repoDir);

                count++;
            }
        }


        final List<Repository> reposPublic = repoList.getReposPublic();
        final File baseDirPublic = new File(userCloneDir, "public");
        createDir(baseDirPublic);
        System.out.println("Public repos: " + reposPublic.size());
        for (Repository repo : reposPublic) {

            final File repoDir = new File(baseDirPublic, repo.getName());
            cloneOrUpdate(credentialsProvider, failures, repo, repoDir);

            count++;
        }

        System.out.println("Total Repo count: " + count);

        return failures;
    }

    /**
     * @param credentialsProvider credentialsProvider for the given repo
     * @param failures list of failures so far (will be appended to if needed).
     * @param repo repository to clone or update (pull or fetch).
     * @param repoDir local repository directory
     * @throws IOException if an IO error occurs
     */
    void cloneOrUpdate(final CredentialsProvider credentialsProvider,
                       final List<String> failures, final Repository repo,
                       final File repoDir) throws IOException {

        System.out.print(repo.getCloneUrl());

        if (repoDir.exists()) {
            System.out.print(" -- Pulling to: " + repoDir);
            try {
                if (!doPull(repoDir, credentialsProvider)) {
                    addFailurePull(failures, repo, repoDir, null);
                }
            } catch (Exception e) {

                System.out.print("\n\t\t  WARNING *** " + repo.getName()
                        + " pull failed!, Fetching to: " + repoDir);
                try {
                    doFetch(repoDir, credentialsProvider);
                } catch (Exception e1) {
                    addFailurePull(failures, repo, repoDir, e);
                }

            }
            System.out.println();

        } else {
            System.out.println(" -- Cloning to: " + repoDir);
            createDir(repoDir);

            try {
                doClone(repo.getCloneUrl(), repoDir,
                        credentialsProvider);
            } catch (Exception e) {
                addFailureClone(failures, repo, repoDir, e);
            }
        }
    }

    /**
     * @param failures list of failure messages, to which we will add the given
     *                 error message and info.
     * @param failedRepo the repository on which the failure occurred.
     * @param repoDir the local directory containing the repository on which
     *                the pull failed.
     * @param cause the failure cause
     */
    void addFailurePull(final List<String> failures, final Repository failedRepo,
                         final File repoDir, final Exception cause) {

        final String msgSuffix = (cause != null ? " -- message: " + cause.getMessage() : "");

        failures.add(failedRepo.getCloneUrl() + " -- " + repoDir + msgSuffix);

        System.out.println("Pull failed!!! repoDir: " + repoDir + msgSuffix);
    }

    /**
     * @param failures list of failure messages, to which we will add the given
     *                 error message and info.
     * @param failedRepo the repository on which the failure occurred.
     * @param repoCloneDir the local directory (which will be deleted)
     *                     containing the failed repository clone.
     * @param cause the failure cause
     * @throws IOException if an IO error occurs
     */
    void addFailureClone(final List<String> failures, final Repository failedRepo,
                         final File repoCloneDir, final Exception cause)
            throws IOException {

        failures.add(failedRepo.getCloneUrl() + " -- " + repoCloneDir
                + " -- message: " + cause.getMessage());

        System.out.println("Clone failed!!! Deleting repoCloneDir: "
                + repoCloneDir + " -- message: " + cause.getMessage());

        // delete cloneDir, so subsequent attempts can succeed
        delete(repoCloneDir);
    }

    /**
     * Create all directories need for the given directory on disk.
     * @param dir the dir tree to create.
     * @throws IOException if an IO error occurs creating dirs.
     */
    static void createDir(final File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Error creating directory: "
                    + dir.getCanonicalPath());
        }
        if (!dir.isDirectory()) {
            throw new IllegalStateException("Should be a directory: "
                    + dir.getCanonicalPath());
        }
    }

    /**
     * @param f a directory to recursively delete, or a file to delete.
     * @throws IOException if an IO error occurs.
     */
    // @todo replace with JDK 1.7 equivalent?
    static void delete(final File f) throws IOException {
        if (f.isDirectory()) {
            for (final File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }
}
