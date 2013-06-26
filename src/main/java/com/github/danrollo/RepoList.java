package com.github.danrollo;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Read the repositories available to the user.
 *
 * @author Dan Rollo
 * Date: 6/24/13
 * Time: 3:07 PM
 */
public final class RepoList {

    /**
     * Repository user name.
     */
    private final String user;
    /**
     * Repository password.
     */
    private final String pwd;

    /**
     * Permission scopes need to list repo info.
     * So far, this is the only scope needed to list repos
     */
    private final String[] scopes = new String[] {"repo"};

    /**
     * Client class for interacting with GitHub HTTP/JSON API.
     */
    private GitHubClient gitHubClient;
    /**
     * Repository service class.
     */
    private RepositoryService repositoryService;

    /**
     * @param username repository login user name
     * @param password repository login password
     */
    RepoList(final String username, final String password) {
        this.user = username;
        this.pwd = password;
    }

    /**
     * @return validated Client class for interacting with GitHub HTTP/JSON API.
     * @throws IOException if an IO error occurs
     */
    public synchronized GitHubClient getValidatedClient() throws IOException {
        if (gitHubClient == null) {
            final OAuthService oauthService = new OAuthService();

            // Replace with actual login and password
            oauthService.getClient().setCredentials(user, pwd);

            final Authorization auth = new Authorization();
            auth.setScopes(Arrays.asList(scopes));

            //final Authorization authValidated =
                    oauthService.createAuthorization(auth);

            gitHubClient = oauthService.getClient();
        }
        return gitHubClient;
    }

    /**
     * @return a valid instance of a RepositoryService.
     * @throws IOException if an IO error occurs
     */
    public synchronized RepositoryService getRepositoryService()
            throws IOException {
        if (repositoryService == null) {
            repositoryService = new RepositoryService(getValidatedClient());
        }
        return repositoryService;
    }

    /**
     * @return a list of Organizations to which the logged in user belongs.
     * @throws IOException if an IO error occurs
     */
    public List<User> getOrganizations() throws IOException {
        final OrganizationService organizationService
                = new OrganizationService(getValidatedClient());
        return organizationService.getOrganizations();
    }

    /**
     * @param org the org who's repositories will be returned.
     * @return the list of repositories available to the given Organization.
     * @throws IOException if an IO error occurs
     */
    public List<Repository> getReposForOrg(final User org) throws IOException {
        if (org == null) {
            throw new IllegalArgumentException(
                    "org parameter must not be null.");
        }

        final RepositoryService service = getRepositoryService();
        return service.getOrgRepositories(org.getLogin());
    }

    /**
     * @return the list of public repositories available to the logged in user.
     * @throws IOException if an IO error occurs
     */
    public List<Repository> getReposPublic() throws IOException {
        final RepositoryService service = getRepositoryService();
        return service.getRepositories();
    }


    /**
     * @throws IOException if an IO error occurs
     */
    public void doDump() throws IOException {

        int count = 0;

        final List<User> orgs = getOrganizations();
        System.out.println("Total Org count: " + orgs.size());

        for (final User org : orgs) {
            final List<Repository> orgRepos = getReposForOrg(org);
            System.out.println("Org: " + org.getLogin() + ", total org repos: "
                    + orgRepos.size());
            for (final Repository repo : orgRepos) {
                System.out.println(repo.getCloneUrl());
                count++;
            }
        }


        final List<Repository> reposPublic = getReposPublic();
        System.out.println("Public repos: " + reposPublic.size());
        for (Repository repo : reposPublic) {
            System.out.println(repo.getCloneUrl());
            count++;
        }

        System.out.println("Total Repo count: " + count);
    }

}
