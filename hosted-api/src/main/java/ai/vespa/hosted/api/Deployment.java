// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package ai.vespa.hosted.api;

import java.nio.file.Path;
import java.util.Optional;

/**
 * A deployment intended for hosted Vespa, containing an application package and some meta data.
 */
public class Deployment {

    private final Optional<String> version;
    private final Path applicationZip;
    private final boolean dryRun;

    private Deployment(Optional<String> version, Path applicationZip, boolean dryRun) {
        this.version = version;
        this.applicationZip = applicationZip;
        this.dryRun = dryRun;
    }

    /** Returns a deployment which will use the provided application package. */
    public static Deployment ofPackage(Path applicationZipFile) {
        return new Deployment(Optional.empty(), applicationZipFile, false);
    }

    /** Returns a copy of this which will have the specified Vespa version on its nodes. */
    public Deployment atVersion(String vespaVersion) {
        return new Deployment(Optional.of(vespaVersion), applicationZip, false);
    }

    /** Returns a copy of this which will do a dry-run deployment. */
    public Deployment withDryRun() {
        return new Deployment(version, applicationZip, true);
    }

    public Optional<String> version() { return version; }
    public Path applicationZip() { return applicationZip; }
    public boolean isDryRun() { return dryRun; }

}
