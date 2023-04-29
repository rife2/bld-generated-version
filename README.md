# [Bld](https://rife2.com/bld) Extension to Generate a Project Version Data Class


[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Release](https://flat.badgen.net/maven/v/metadata-url/repo.rife2.com/releases/com/uwyn/rife2/bld-generated-version/maven-metadata.xml?color=blue)](https://repo.rife2.com/#/releases/com/uwyn/rife2/bld-generated-version)
[![Snapshot](https://flat.badgen.net/maven/v/metadata-url/repo.rife2.com/snapshots/com/uwyn/rife2/bld-generated-version/maven-metadata.xml?label=snapshot)](https://repo.rife2.com/#/snapshots/com/uwyn/rife2/bld-generated-version)
[![GitHub CI](https://github.com/rife2/bld-generated-version/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/bld-generated-version/actions/workflows/bld.yml)

To automatically create a generated version class using the default template in your project on compile:
```java
@Override
public void compile() throws Exception {
    genver();
    super.compile();
}

@BuildCommand(summary = "Generates version class")
public void genver() throws Exception {
    new GeneratedVersionOperation()
        .fromProject(this)
        .execute();
}
```

```text
./bld compile
```

- [View Examples](https://github.com/rife2/bld-generated-version/tree/master/examples)

## Version Class Template

This is the default template:

```java
package {{v packageName/}};

import java.util.Date;

public final class {{v className/}} {
    public static final String PROJECT = "{{v project/}}";
    public static final Date BUILD_DATE = new Date({{v epoch/}}L);
    public static final int MAJOR = {{v major/}};
    public static final int MINOR = {{v minor/}};
    public static final int REVISION = {{v revision/}};
    public static final String QUALIFIER = "{{v qualifier/}}";
    public static final String VERSION = "{{v version/}}";
    
    private {{v className/}}() {
        throw new UnsupportedOperationException("Illegal constructor call.");
    }
}
```
## Custom Template
You can specified your own template using some or all of the template value tags, as follows:

```java
@BuildCommand(summary = "Generates MyAppVersion class")
public void genver() throws Exception {
    new GeneratedVersionOperation()
        .fromProject(this)
        .projectName("My App")
        .packageName("com.example.myapp")
        .className("MyAppVersion")
        .classTemplate(new File(workDirectory, "myversion.txt"))
        .execute();
}
```
```java
// myversion.txt

package {{v packageName/}};

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class {{v className/}} {
    public static final String PROJECT = "{{v project/}}";
    public static final LocalDateTime BUILD_DATE = Instant.ofEpochMilli({{v epoch/}}L)
            .atZone(ZoneId.systemDefault()).toLocalDateTime();
    public static final String VERSION = "{{v version/}}";
    
    private {{v className/}}() {
        // no-op
    }
}
```


Please check the [GeneratedVersionOperation documentation](https://rife2.github.io/bld-generated-version/rife/bld/extension/GeneratedVersionOperation.html#method-summary) for all available configuration options.
