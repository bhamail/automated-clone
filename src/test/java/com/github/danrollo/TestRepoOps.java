package com.github.danrollo;

import com.google.common.io.Files;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Dan Rollo
 * Date: 6/24/13
 * Time: 9:35 PM
 */
public class TestRepoOps {

    private Properties testProps;
    private RepoOps repoOps;
    private File workDir;

    @Before
    public void setUp() throws IOException {
        testProps = TestRepoList.getTestProperties();

        repoOps = new RepoOps();

        workDir = Files.createTempDir();
    }

    @After
    public void tearDown() throws IOException {
        RepoOps.delete(workDir);
    }


    @Test
    public void testDoClone() throws IOException, GitAPIException, URISyntaxException {

        final RepoList repoList = new RepoList(testProps.getProperty("user"), testProps.getProperty("password"));
        final Repository repo = repoList.getReposPublic().get(0);
        final String repoUrl = repo.getCloneUrl();

        final File repoCloneDir = new File(workDir, repo.getName());
        RepoOps.createDir(repoCloneDir);

        repoOps.doClone(repoUrl, repoCloneDir,
                RepoOps.getCredentials(testProps.getProperty("user"), testProps.getProperty("password")));
    }

    // Do not normally run this, could be considered abusive
    //@Test
    public void testCloneAll() throws IOException, GitAPIException, URISyntaxException {

        final List<String> failures = repoOps.cloneAll(workDir, testProps.getProperty("user"), testProps.getProperty("password"));

        final String message;
        if (failures.size() > 0) {
            message = failures.toString().replaceAll(", ", ", \n");
        } else {
            message = null;
        }

        assertEquals(message, 0, failures.size());
    }
}
