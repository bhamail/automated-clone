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
public class RepoList {

    private final String user;
    private final String pwd;

    // so far, this is the only scope needed to list repos
    private String[] scopes = new String[] {"repo"};

    private GitHubClient gitHubClient;
    private RepositoryService repositoryService;

    RepoList(final String user, final String pwd) {
        this.user = user;
        this.pwd = pwd;
    }

    public synchronized GitHubClient getValidatedClient() throws IOException {
        if (gitHubClient == null) {
            final OAuthService oauthService = new OAuthService();

            // Replace with actual login and password
            oauthService.getClient().setCredentials(user, pwd);

            final Authorization auth = new Authorization();
            auth.setScopes(Arrays.asList(scopes));

            final Authorization authValidated = oauthService.createAuthorization(auth);

            gitHubClient = oauthService.getClient();
        }
        return gitHubClient;
    }

    public synchronized RepositoryService getRepositoryService() throws IOException {
        if (repositoryService == null) {
            repositoryService = new RepositoryService(getValidatedClient());
        }
        return repositoryService;
    }


    public List<User> getOrganizations() throws IOException {
        final OrganizationService organizationService = new OrganizationService(getValidatedClient());
        return organizationService.getOrganizations();
    }

    public List<Repository> getReposForOrg(final User org) throws IOException {
        if (org == null) {
            throw new IllegalArgumentException("org parameter must not be null.");
        }

        final RepositoryService service = getRepositoryService();
        return service.getOrgRepositories(org.getLogin());
    }

    public List<Repository> getReposPublic() throws IOException {
        final RepositoryService service = getRepositoryService();
        return service.getRepositories();
    }


    public void doDump() throws IOException {

        int count = 0;

        final List<User> orgs = getOrganizations();
        System.out.println("Total Org count: " + orgs.size());

        for (final User org : orgs) {
            final List<Repository> orgRepos = getReposForOrg(org);
            System.out.println("Org: " + org.getLogin() + ", total org repos: " + orgRepos.size());
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
