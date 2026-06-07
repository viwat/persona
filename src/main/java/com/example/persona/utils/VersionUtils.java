package com.example.persona.utils;

import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;

/**
 * Utility class for semantic version comparison. Supports major.minor.patch
 * format (e.g., "1.0.0", "2.1.3").
 */
public class VersionUtils {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");

    /**
     * Parse semantic version string to Version object.
     *
     * @param version
     *            version string (e.g., "1.0.0")
     * @return Version object
     * @throws IllegalArgumentException
     *             if version format is invalid
     */
    public static Version parse(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }

        version = version.trim();
        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw new IllegalArgumentException("Invalid version format: " + version + ". Expected: major.minor.patch");
        }

        String[] parts = version.split("\\.");
        return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    /**
     * Compare two semantic versions.
     *
     * @param v1
     *            first version
     * @param v2
     *            second version
     * @return negative if v1 < v2, zero if v1 == v2, positive if v1 > v2
     */
    public static int compare(String v1, String v2) {
        Version version1 = parse(v1);
        Version version2 = parse(v2);
        return version1.compareTo(version2);
    }

    /**
     * Check if version1 is less than version2.
     */
    public static boolean isLessThan(String v1, String v2) {
        return compare(v1, v2) < 0;
    }

    /**
     * Check if version1 is less than or equal to version2.
     */
    public static boolean isLessThanOrEqual(String v1, String v2) {
        return compare(v1, v2) <= 0;
    }

    /**
     * Check if version1 is greater than version2.
     */
    public static boolean isGreaterThan(String v1, String v2) {
        return compare(v1, v2) > 0;
    }

    /**
     * Check if version1 is greater than or equal to version2.
     */
    public static boolean isGreaterThanOrEqual(String v1, String v2) {
        return compare(v1, v2) >= 0;
    }

    /**
     * Check if an API is deprecated before a given version.
     *
     * @param deprecatedVersion
     *            the version when API was deprecated (can be empty)
     * @param targetVersion
     *            the version to check against
     * @return true if deprecated version is before target version
     */
    public static boolean isDeprecatedBefore(String deprecatedVersion, String targetVersion) {
        if (deprecatedVersion == null || deprecatedVersion.trim().isEmpty()) {
            return false; // Not deprecated
        }
        return isLessThan(deprecatedVersion.trim(), targetVersion);
    }

    /**
     * Version class for semantic versioning.
     */
    public record Version(int major, int minor, int patch) implements Comparable<Version> {

        @Override
        public int compareTo(Version other) {
            if (this.major != other.major) {
                return Integer.compare(this.major, other.major);
            }
            if (this.minor != other.minor) {
                return Integer.compare(this.minor, other.minor);
            }
            return Integer.compare(this.patch, other.patch);
        }

        @Override
        public @NonNull String toString() {
            return major + "." + minor + "." + patch;
        }
    }
}
