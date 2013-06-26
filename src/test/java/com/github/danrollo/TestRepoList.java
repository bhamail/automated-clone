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
    private static final String PROP_TEST_USER = "automated-clone.test.user";
    private static String testUser;

    private static final String PROP_TEST_PWD = "automated-clone.test.password";
    private static String testPwd;

    private static void getTestProperties() throws IOException {
        if (testUser == null) {
            // first try to read system properties what should be set via .m2/settings.xml
            /*
             @todo Add your repository username and password to .m2/settings.xml like so:

            <settings>
            ...
                <profiles>
                    <profile>
                        <id>automated-clone.test.profile</id>

                        <activation>
                            <activeByDefault>false</activeByDefault>
                        </activation>

                        <properties>
                            <automated-clone.test.user>yourUsername</automated-clone.test.user>
                            <automated-clone.test.password>yourPassword</automated-clone.test.password>
                        </properties>

                    </profile>
                </profiles>
            ...
            </settings>
            */

            final String settingsUser =  System.getProperty(PROP_TEST_USER);
            if (settingsUser != null) {
                testUser = settingsUser;
                testPwd = System.getProperty(PROP_TEST_PWD);

            } else {

                // read user/pwd from local source
                // @todo Add your own values to the file:
                // 'src/test/resources/test-login.properties'.
                final String testPropsFilename = "test-login.properties";
                final InputStream is = TestRepoList.class.getClassLoader()
                        .getResourceAsStream(testPropsFilename);
                final Properties testProps = new Properties();
                try {
                    assertNotNull("Failed to load unit test user/password "
                            + "properties from file: '" + testPropsFilename
                            + "'.");
                    testProps.load(is);
                } finally {
                    is.close();
                }
                // sanity check
                testUser = testProps.getProperty(PROP_TEST_USER);
                assertFalse("It appears you did not edit your .m2/settings.xml"
                        + " file, nor 'src/test/resources/"
                        + testPropsFilename + "' to have your repository "
                        + "username and password.",
                        "myuser".equals(testUser));
                testPwd = testProps.getProperty(PROP_TEST_PWD);
            }
        }
    }

    static String getTestUser() throws IOException {
        getTestProperties();
        return testUser;
    }

    static String getTestPwd() throws IOException {
        getTestProperties();
        return testPwd;
    }


    private RepoList repoList;

    @Before
    public void setUp() throws IOException {
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
