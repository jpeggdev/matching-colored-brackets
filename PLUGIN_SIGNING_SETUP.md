# Plugin Signing Configuration Guide

This document explains how to set up plugin signing for the matching-colored-brackets plugin for local development and CI/CD deployment.

## Overview

Plugin signing is required by JetBrains to publish plugins to the JetBrains Marketplace. The signing process uses:
- A self-signed certificate (PEM format)
- A private key (PEM format)
- A password for the private key (optional)

## Local Development Setup

### 1. Certificate and Keys Already Generated

A self-signed certificate and private key have been generated and stored in `.signing/`:

```
.signing/
├── plugin-signing.crt          # Certificate (PEM format)
├── plugin-signing.key          # Private key (PEM format)
├── plugin-signing.crt.base64   # Base64-encoded certificate
└── plugin-signing.key.base64   # Base64-encoded private key
```

### 2. Configure Local Environment

The `.env` file has been created with the base64-encoded credentials:

```bash
# File: .env
CERTIFICATE_CHAIN=<base64-encoded certificate>
PRIVATE_KEY=<base64-encoded private key>
PRIVATE_KEY_PASSWORD=          # Leave empty (no password on key)
PUBLISH_TOKEN=                  # Add your JetBrains token here
```

**Important Security Note:**
- The `.env` file is in `.gitignore` and should NEVER be committed
- For local builds: use the `.env` file
- For CI/CD: use GitHub Actions secrets (see below)

### 3. Build Locally with Signing

To build the plugin locally with signing enabled:

```bash
# Load environment variables from .env
set -o allexport
source .env
set +o allexport

# Build the signed plugin
./gradlew buildPlugin
```

Or in one command:
```bash
source .env && ./gradlew buildPlugin
```

The signed plugin will be in: `build/distributions/matching-colored-brackets-0.0.1.jar`

## GitHub Actions CI/CD Setup

### 1. Set GitHub Actions Secrets

You need to configure the following secrets in your GitHub repository:

**Go to: Settings → Secrets and variables → Actions**

#### Add the following secrets:

1. **CERTIFICATE_CHAIN**
   - Value: Contents of `.signing/plugin-signing.crt.base64`
   - Paste the entire base64 string (single line)

2. **PRIVATE_KEY**
   - Value: Contents of `.signing/plugin-signing.key.base64`
   - Paste the entire base64 string (single line)

3. **PRIVATE_KEY_PASSWORD**
   - Value: (leave empty)
   - Our key has no password, so this can be empty or omitted

4. **PUBLISH_TOKEN**
   - Value: Your JetBrains Marketplace publish token
   - Obtain from: https://plugins.jetbrains.com/oauth/authorize
   - This is optional for testing CI (required only for publishing)

### 2. How to Get the Base64 Values

If you need to re-generate these values:

```bash
# From the project root
base64 -w0 .signing/plugin-signing.crt > /tmp/cert.base64
base64 -w0 .signing/plugin-signing.key > /tmp/key.base64

# View the values to copy to GitHub
cat /tmp/cert.base64
cat /tmp/key.base64
```

### 3. Verify Secrets are Configured

GitHub Actions will automatically use these secrets when running the build workflow:

```yaml
# From build.yml
signing {
    certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
    privateKey = providers.environmentVariable("PRIVATE_KEY")
    password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
}
```

The workflow will have access to these environment variables during the build process.

## Publishing to JetBrains Marketplace

### 1. Get a Publish Token

1. Visit: https://plugins.jetbrains.com/
2. Sign in with your account (or create one)
3. Go to Account → Tokens
4. Create a new token with "Publish" permissions
5. Copy the token

### 2. Add PUBLISH_TOKEN Secret

1. Go to GitHub repo settings → Secrets and variables → Actions
2. Create a new secret: `PUBLISH_TOKEN`
3. Paste your token value

### 3. Release and Publish

When you create a GitHub release:

1. The `build.yml` workflow creates a draft release with the signed plugin
2. When you publish the release, the `release.yml` workflow triggers
3. It publishes the plugin to JetBrains Marketplace using the `PUBLISH_TOKEN`

## Regenerating Certificates (if needed)

If you need to regenerate the certificate and key:

```bash
cd .signing

# Generate new self-signed certificate and private key (5 years validity)
openssl req -new -x509 -newkey rsa:4096 -keyout plugin-signing.key \
    -out plugin-signing.crt -days 1825 -nodes \
    -subj "/CN=matching-colored-brackets/O=jpeggdev/C=US"

# Generate base64-encoded versions
base64 -w0 plugin-signing.crt > plugin-signing.crt.base64
base64 -w0 plugin-signing.key > plugin-signing.key.base64

# Update .env file with new base64 values
```

**Important:** If you regenerate certificates, you MUST:
1. Update GitHub Actions secrets with new values
2. Update the `.env` file locally
3. All previous releases will use the old certificate (this is fine)
4. Future releases will use the new certificate

## Troubleshooting

### Build fails with "CERTIFICATE_CHAIN not found"

This means the environment variable is not set. Make sure:
- For local builds: `source .env` is executed before running gradle
- For CI/CD: The `CERTIFICATE_CHAIN` secret is properly configured in GitHub

### "Invalid certificate" error

Make sure:
- The base64 string is correctly encoded (run `base64 -w0` without piping through other tools)
- The certificate file is valid PEM format (starts with `-----BEGIN CERTIFICATE-----`)

### Plugin won't sign

Check that:
- `PRIVATE_KEY` is also set and properly base64-encoded
- `PRIVATE_KEY_PASSWORD` is empty (unless you set a password on the key)
- All environment variables are available to the build process

## Security Considerations

### Local Development
- The `.env` file contains sensitive credentials
- It is added to `.gitignore` and should NEVER be committed
- If accidentally exposed, regenerate the certificate

### CI/CD (GitHub Actions)
- Secrets are encrypted by GitHub
- They are only decrypted during workflow runs
- Each workflow run logs are masked to hide secret values
- Only workflows in the main branch can access secrets

### Best Practices
1. **Rotate certificates periodically** - regenerate every 1-2 years
2. **Limit access** - only authorized team members should have GitHub secret access
3. **Monitor usage** - check GitHub Actions logs for unexpected builds
4. **Use different tokens** - don't reuse the same publish token across projects

## Files and Directories

```
matching-colored-brackets/
├── .signing/                          # Certificate and key files (not in git)
│   ├── plugin-signing.crt
│   ├── plugin-signing.key
│   ├── plugin-signing.crt.base64
│   └── plugin-signing.key.base64
├── .env                               # Local environment (not in git)
├── .env.example                       # Example environment template
├── PLUGIN_SIGNING_SETUP.md           # This file
├── build.gradle.kts                   # Signing configuration
└── .github/workflows/
    ├── build.yml                      # Builds and signs the plugin
    └── release.yml                    # Publishes to marketplace
```

## References

- [JetBrains Plugin Signing Documentation](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html)
- [JetBrains Marketplace Publishing](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html)
- [IntelliJ Platform Gradle Plugin](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-signing.html)
- [GitHub Actions Secrets](https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions)
