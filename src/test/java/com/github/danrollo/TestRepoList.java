package com.github.danrollo;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.RequestException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Dan Rollo
 * Date: 6/24/13
 * Time: 3:30 PM
 */
public class TestRepoList {
    static final String PROP_TEST_USER = "automated-clone.test.user";
    static final String PROP_TEST_PWD = "automated-clone.test.password";

    private static Properties testProps;
    private static Properties getTestProperties() throws IOException {
        if (testProps == null) {
            // read user/pwd from local source - @todo Add your own values to the file 'src/test/resources/test-login.properties'.
            final String testPropsFilename = "test-login.properties";
            final InputStream is = TestRepoList.class.getClassLoader().getResourceAsStream(testPropsFilename);
            testProps = new Properties();
            try {
                assertNotNull("Failed to load unit test user/password properties. Be sure you created your own: '" + testPropsFilename + "' file.");
                testProps.load(is);
            } finally {
                is.close();
            }
            // sanity check
            final String testUserName = testProps.getProperty(PROP_TEST_USER);
            assertFalse("It appears you did not edit 'src/test/resources/" + testPropsFilename + "' to have your user/pwd.", "myuser".equals(testUserName));
        }
        return testProps;
    }

    static String getTestUser() throws IOException {
        return getTestProperties().getProperty(PROP_TEST_USER);
    }

    static String getTestPwd() throws IOException {
        return getTestProperties().getProperty(PROP_TEST_PWD);
    }


    private RepoList repoList;

    @Before
    public void setUp() throws IOException {
        final Properties testProps = getTestProperties();

        repoList = new RepoList(getTestUser(), getTestPwd());
    }

    @Test(expected = RequestException.class)
    public void testGetOAuthTokenEmpty() throws IOException {
        new RepoList("", "").getValidatedClient();
    }

    @Test
    public void testGetOAuthToken() throws IOException {
        assertNotNull(repoList.getValidatedClient());
    }


    @Test
    public void testGetOrganizations() throws IOException {
        assertNotNull(repoList.getOrganizations());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetReposForOrgWithNull() throws IOException {
        assertNotNull(repoList.getReposForOrg(null));
    }

    @Test
    public void testGetReposForOrg() throws IOException {
        final List<User> orgs = repoList.getOrganizations();
        for (final User org : orgs) {
            assertNotNull(repoList.getReposForOrg(org));
        }
    }

    @Test
    public void testGetReposPublic() throws IOException {
        assertNotNull(repoList.getReposPublic());
    }

    @Test
    public void testDump() throws IOException {
        repoList.doDump();
    }

}
