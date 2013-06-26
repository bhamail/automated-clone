package com.github.danrollo;

import com.google.common.io.Files;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Dan Rollo
 * Date: 6/24/13
 * Time: 9:35 PM
 */
public class TestRepoOps {

    private static CredentialsProvider getTestCredentialProvider() throws IOException {
        return RepoOps.getCredentials(TestRepoList.getTestUser(), TestRepoList.getTestPwd());
    }

    @Rule
    public TestName name = new TestName();

    private RepoOps repoOps;
    private File workDir;

    @Before
    public void setUp() throws IOException {

        repoOps = new RepoOps();

        workDir = Files.createTempDir();
    }

    @After
    public void tearDown() throws IOException {
        RepoOps.delete(workDir);
    }

    private boolean quiet;

    @Test
    public void testDoClone() throws IOException, GitAPIException, URISyntaxException {

        final RepoList repoList = new RepoList(TestRepoList.getTestUser(), TestRepoList.getTestPwd());
        if (repoList.getReposPublic().size() > 0) {
            final Repository repo = repoList.getReposPublic().get(0);
            final String repoUrl = repo.getCloneUrl();

            final File repoCloneDir = new File(workDir, repo.getName());
            RepoOps.createDir(repoCloneDir);

            repoOps.doClone(repoUrl, repoCloneDir, getTestCredentialProvider());
        } else {
            if (!quiet) {
                System.out.println("WARNING **** Skipping: " + name.getMethodName()
                        + "() due to zero public repositories available for user: " + TestRepoList.getTestUser());
            }
        }

    }

    @Test
    public void testDoPull() throws GitAPIException, IOException, URISyntaxException {
        // ensure a repo is available
        quiet = true;
        try {
            testDoClone();
        } finally {
            quiet = false;
        }

        final RepoList repoList = new RepoList(TestRepoList.getTestUser(), TestRepoList.getTestPwd());
        if (repoList.getReposPublic().size() > 0) {
            final Repository repo = repoList.getReposPublic().get(0);
            final File repoCloneDir = new File(workDir, repo.getName());
            assertTrue(repoCloneDir.exists());

            repoOps.doPull(repoCloneDir, getTestCredentialProvider());
        } else {
            System.out.println("WARNING **** Skipping: " + name.getMethodName()
                    + "() due to zero public repositories available for user: " + TestRepoList.getTestUser());
        }
    }

    // Do not normally run this, could be considered abusive
    @Test
    public void testCloneAll() throws IOException, GitAPIException, URISyntaxException {

        final List<String> failures = repoOps.cloneAll(workDir, TestRepoList.getTestUser(), TestRepoList.getTestPwd());

        final String message;
        if (failures.size() > 0) {
            message = failures.toString().replaceAll(", ", ", \n");
        } else {
            message = null;
        }

        assertEquals(message, 0, failures.size());
    }
}
