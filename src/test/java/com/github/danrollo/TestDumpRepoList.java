package com.github.danrollo;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Dan Rollo
 * Date: 6/24/13
 * Time: 3:30 PM
 */
public class TestDumpRepoList {

    private DumpRepoList dumpRepoList;

    @Before
    public void setUp() {
        dumpRepoList = new DumpRepoList("defunkt", "");
    }

    @Test
    public void testDump() throws IOException {
        dumpRepoList.doDump();
    }

}
