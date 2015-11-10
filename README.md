# CucumberJelly

This is an atempt to provide a module to provide a way to Test a Netbeans Platform with Cucumber.

It will also provide some 'Standard Jelly Steps' to help ensure that we get easy testing.

~~This is an extension of https://github.com/NickCharsley/MavenNetBeansPlatformCucumberBBD which uses this.~~
This is really totally isolated now, with an example usage project now at https://github.com/NickCharsley/CRUDyCucumber/tree/AboutExample.

This is my first experiment with both Maven and Netbeans Platform so I may have done something terible with either
coding style or dependencies or choice of package etc.

If you think its criminal please either help by forking and correcting or leave a *nice* issue and I'll see what can be done.

This is now also hosted on Sonatype's OSSRH with releases making it into the Central Repository.
it can be included as a dependency (should have testing scope!!) as follows
```
.
.
.
<dependency>
    <groupId>uk.co.oldnicksoftware</groupId>
    <artifactId>CucumberJelly</artifactId>
    <version>0.11</version>
    <scope>test</scope>
</dependency>
.
.
.
```

