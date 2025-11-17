# CI/CD Documentation

This document describes the Continuous Integration and Continuous Deployment pipeline for the SourceSync SDK UI Android project.

## Overview

**Status**: ✅ Fully Implemented

The project uses GitHub Actions for automated building, testing, and publishing to Maven Central. The CI/CD pipeline is triggered on:

- **Push to tags** matching pattern `v*` (e.g., `v1.0.0`)
- **Manual workflow dispatch** with custom version input

## Pipeline Architecture

```
┌─────────────────┐
│  Git Tag Push   │
│  or Manual Run  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Get Version    │
│  Extract from   │
│  tag or input   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Checkout Code  │
│  Setup JDK 21   │
│  Setup Android  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Build & Publish │
│  Maven Central  │
│  with Signing   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Create GitHub   │
│    Release      │
└─────────────────┘
```

## Workflow Configuration

### File Location
```
.github/workflows/sdk-ui.yaml
```

### Workflow Name
```yaml
name: Release and Publish
```

### Trigger Events

#### 1. Automatic Trigger (Tag Push)
```yaml
on:
  push:
    tags:
      - 'v*'
```

**Example**: Pushing `v1.2.3` automatically triggers build and publish

#### 2. Manual Trigger (Workflow Dispatch)
```yaml
on:
  workflow_dispatch:
    inputs:
      tag_version:
        description: 'Tag version to build (e.g., v1.0.0)'
        required: true
        type: string
```

**Usage**: Go to Actions tab → Select workflow → Run workflow → Enter version

### Required Permissions
```yaml
permissions:
  contents: write      # Create releases
  packages: write      # Publish packages
  pull-requests: write # Future PR automation
```

## Jobs

### Job 1: Get Version

**Purpose**: Extract version number from git tag or manual input

**Steps**:
1. Determine event type (push or manual)
2. Extract version number (removes `v` prefix)
3. Set as output for downstream jobs

**Output**: `version` (e.g., `1.2.3`)

**Example Logic**:
```bash
if [ "${{ github.event_name }}" = "push" ]; then
  # Extract from tag: refs/tags/v1.2.3 → 1.2.3
  VERSION=${GITHUB_REF#refs/tags/v}
else
  # Use manual input: v1.2.3 → 1.2.3
  VERSION="${{ inputs.tag_version }}"
  VERSION=${VERSION#v}
fi
```

### Job 2: Publish

**Purpose**: Build, sign, and publish the SDK to Maven Central

**Dependencies**: Requires `get-version` job completion

**Environment**:
- **Runner**: `ubuntu-latest`
- **JDK**: Zulu 21
- **Android SDK**: Latest via `android-actions/setup-android@v3`

**Steps**:

#### 1. Checkout Code
```yaml
- uses: actions/checkout@v4
  with:
    ref: ${{ github.event_name == 'workflow_dispatch' && 
             format('v{0}', needs.get-version.outputs.version) || 
             github.ref }}
```

Checks out the specific tag version for building.

#### 2. Setup Build Environment
```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    distribution: 'zulu'
    java-version: 21

- name: Setup Android SDK
  uses: android-actions/setup-android@v3
```

#### 3. Make Gradle Wrapper Executable
```yaml
- name: Make Gradle wrapper executable
  run: chmod +x ./gradlew
```

#### 4. Build and Publish
```bash
./gradlew :sourcesync-sdk-ui:publishAndReleaseToMavenCentral \
  --stacktrace \
  -Pversion=$BUILD_VERSION
```

This single command:
- Builds the SDK library
- Signs the artifacts with GPG
- Publishes to Maven Central staging
- Automatically releases from staging to Central

#### 5. Create GitHub Release
```yaml
- uses: softprops/action-gh-release@v1
  with:
    tag_name: v${{ needs.get-version.outputs.version }}
    name: Release v${{ needs.get-version.outputs.version }}
    draft: false
    prerelease: false
```

Creates a GitHub release with the version tag.

## Required Secrets

The following secrets must be configured in GitHub repository settings:

### Maven Central Credentials

| Secret Name | Description | How to Obtain |
|------------|-------------|---------------|
| `MAVEN_USERNAME` | Sonatype OSSRH username | [Sonatype Account](https://issues.sonatype.org) |
| `MAVEN_PASSWORD` | Sonatype OSSRH password | From Sonatype account |

### Code Signing Credentials

| Secret Name | Description | Format |
|------------|-------------|--------|
| `SIGNING_KEY_ID` | GPG key ID (last 8 chars) | `12345678` |
| `SIGNING_PASSWORD` | GPG key passphrase | Plain text |
| `GPG_KEY_CONTENTS` | ASCII-armored GPG private key | Multi-line base64 |

#### Generating GPG Key

```bash
# Generate new GPG key
gpg --gen-key

# List keys (note the key ID)
gpg --list-secret-keys --keyid-format=long

# Export private key (ASCII-armored)
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc

# Get key ID (last 8 characters of fingerprint)
gpg --list-keys --keyid-format=short
```

## Release Process

### Automated Release (Recommended)

#### Step 1: Prepare Release
```bash
# Ensure you're on main/master branch
git checkout main
git pull origin main

# Ensure all tests pass
./gradlew test

# Update version if needed (in build files)
# Commit any final changes
git add .
git commit -m "chore: prepare release v1.2.3"
git push
```

#### Step 2: Create and Push Tag
```bash
# Create annotated tag
git tag -a v1.2.3 -m "Release version 1.2.3"

# Push tag to trigger workflow
git push origin v1.2.3
```

#### Step 3: Monitor Workflow
1. Go to [Actions tab](https://github.com/Source-Digital/sourcesync-android-sdk-ui/actions)
2. Watch "Release and Publish" workflow progress
3. Verify each job completes successfully

#### Step 4: Verify Publication
1. Check [Maven Central](https://central.sonatype.com/) (may take 15-30 minutes)
2. Search for: `io.sourcesync:sourcesync-sdk-ui`
3. Verify version is available

### Manual Release

If automated release fails or you need to release from a specific commit:

#### Using Workflow Dispatch

1. Go to **Actions** → **Release and Publish**
2. Click **Run workflow**
3. Enter version (e.g., `v1.2.3` or `1.2.3`)
4. Click **Run workflow**

#### Manual Publishing (Emergency)

If GitHub Actions is unavailable:

```bash
# Set version
export BUILD_VERSION=1.2.3

# Set credentials as environment variables
export ORG_GRADLE_PROJECT_mavenCentralUsername=your_username
export ORG_GRADLE_PROJECT_mavenCentralPassword=your_password
export ORG_GRADLE_PROJECT_signingInMemoryKeyId=12345678
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=your_passphrase
export ORG_GRADLE_PROJECT_signingInMemoryKey="$(cat private-key.asc)"

# Publish
./gradlew :sourcesync-sdk-ui:publishAndReleaseToMavenCentral \
  --stacktrace \
  -Pversion=$BUILD_VERSION
```

## Versioning Strategy

### Version Format
```
MAJOR.MINOR.PATCH
```

Following [Semantic Versioning 2.0.0](https://semver.org/):

- **MAJOR**: Breaking API changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Examples
- `1.0.0` - Initial stable release
- `1.1.0` - New feature added
- `1.1.1` - Bug fix
- `2.0.0` - Breaking changes

### Pre-release Versions
```
1.0.0-alpha.1
1.0.0-beta.1
1.0.0-rc.1
```

Pre-release tags can be pushed but should be marked as pre-release in GitHub.

## Maven Central Publishing

### Publication Coordinates
```gradle
groupId: io.sourcesync
artifactId: sourcesync-sdk-ui
version: {version from tag}
```

### Publishing Plugin
Uses Gradle Maven Publish Plugin with Central Portal publishing.

### Staging Process
The workflow uses `publishAndReleaseToMavenCentral` which:

1. **Uploads** artifacts to Central Portal
2. **Validates** artifacts meet requirements
3. **Signs** with GPG key
4. **Releases** automatically to Maven Central

### Sync Time
- **Central Portal**: Immediate
- **Maven Central Search**: 15-30 minutes
- **Repository Mirrors**: 2-4 hours

## Build Artifacts

### Generated Files
- **AAR**: `sourcesync-sdk-ui-{version}.aar`
- **Sources JAR**: `sourcesync-sdk-ui-{version}-sources.jar`
- **Javadoc JAR**: `sourcesync-sdk-ui-{version}-javadoc.jar`
- **POM**: `sourcesync-sdk-ui-{version}.pom`
- **Signatures**: `.asc` files for each artifact

### Artifact Validation
Before release, artifacts are validated for:
- ✅ Correct POM metadata
- ✅ Source and Javadoc JARs present
- ✅ GPG signatures valid
- ✅ Checksums (MD5, SHA1, SHA256)

## Troubleshooting

### Workflow Fails at Publish Step

**Symptom**: Gradle publish task fails

**Common Causes**:
1. Invalid Maven credentials
2. GPG signing issues
3. Network timeout to Maven Central

**Solution**:
```bash
# Test locally first
./gradlew :sourcesync-sdk-ui:publishToMavenLocal

# Check secrets are set correctly in GitHub
# Verify GPG key has not expired
gpg --list-keys
```

### Version Already Published

**Symptom**: `Version 1.2.3 already exists`

**Solution**: Maven Central doesn't allow re-publishing same version
- Increment version (e.g., `1.2.4`)
- Or use pre-release suffix (e.g., `1.2.3-1`)

### Tag Already Exists

**Symptom**: `tag 'v1.2.3' already exists`

**Solution**:
```bash
# Delete local tag
git tag -d v1.2.3

# Delete remote tag
git push origin :refs/tags/v1.2.3

# Recreate and push
git tag -a v1.2.3 -m "Release version 1.2.3"
git push origin v1.2.3
```

### Release Not Appearing on Maven Central

**Wait Time**: Allow 30 minutes for indexing

**Check Status**:
1. Go to [Central Portal](https://central.sonatype.com/)
2. Login with credentials
3. Check deployment history

## Future Enhancements

```

These can be enabled in future releases.

## Best Practices

1. **Test Before Tagging**: Always run full test suite before creating release tag
2. **Use Annotated Tags**: Include release notes in tag annotation
3. **Monitor Workflows**: Watch Actions tab during releases
4. **Verify Publications**: Check Maven Central after each release
5. **Keep Secrets Updated**: Rotate GPG keys and credentials periodically
6. **Document Changes**: Update CHANGELOG.md before releases

## Related Documentation

- [Setup Guide](setup.md) - Development environment
- [Dependencies](dependencies.md) - Library dependencies
- [Architecture](sourcesync.md) - System design

## Support

For CI/CD issues:
- Check [GitHub Actions](https://github.com/Source-Digital/sourcesync-android-sdk-ui/actions)
- Review [workflow logs](https://github.com/Source-Digital/sourcesync-android-sdk-ui/actions/workflows/sdk-ui.yaml)
- Open [issue](https://github.com/Source-Digital/sourcesync-android-sdk-ui/issues) with workflow run link