package com.github.danrollo;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;

/**
 * Read the repositories available to the user.
 *
 * @author Dan Rollo
 * Date: 6/24/13
 * Time: 3:07 PM
 */
public class DumpRepoList {

    private final String user;
    private final String pwd;

    DumpRepoList(final String user, final String pwd) {
        this.user = user;
        this.pwd = pwd;
    }

    public void doDump() throws IOException {
        RepositoryService service = new RepositoryService();
        for (Repository repo : service.getRepositories(user)) {
            System.out.println(repo.getCloneUrl());
        }
    }
}
