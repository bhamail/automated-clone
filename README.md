Automated Clone project
=======================

Here is the [Maven generated web site](http://bhamail.github.com/automated-clone/) for this project.

Setup
-----

In order to run the unit tests in this project, you must specify your
your repository username and password. You can do this by creating a
new profile in your `.m2/settings.xml` file, like so:

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

If you do not want to alter `.m2/settings.xml`, you can edit the existing
test resource file: `src/test/resources/test-login.properties` to contain your
repository username and password.