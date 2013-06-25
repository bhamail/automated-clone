package com.github.danrollo;

import com.google.common.io.Files;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * @author Dan Rollo
 * Date: 6/24/13
 * Time: 9:35 PM
 */
public class TestRepoOps {

    private static void delete(final File f) throws IOException {
        if (f.isDirectory()) {
            for (final File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

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
        delete(workDir);
    }


    @Test
    public void testDoClone() throws IOException, GitAPIException, URISyntaxException {

        final RepoList repoList = new RepoList(testProps.getProperty("user"), testProps.getProperty("password"));
        final String repoUrl = repoList.getReposPublic().get(0).getCloneUrl();

        assertTrue(repoOps.doClone(repoUrl, workDir, RepoOps.getCredentials(testProps.getProperty("user"), testProps.getProperty("password"))));
    }
}
