# Plugin Signing Quick Start Guide

## For Local Development

### Build with Signing
```bash
source .env && ./gradlew buildPlugin
```

Output: `build/distributions/matching-colored-brackets-0.0.1.zip`

### Verify Signing
```bash
# Check if plugin JAR is signed
./gradlew verifyPluginSignature
```

---

## For GitHub Actions (CI/CD)

### 1. Add GitHub Secrets

Go to: **Settings → Secrets and variables → Actions**

#### Copy the base64-encoded values:

```bash
# From project root
cat .signing/plugin-signing.crt.base64    # Copy this value
cat .signing/plugin-signing.key.base64    # Copy this value
```

#### Create secrets:

| Secret Name | Value |
|---|---|
| `CERTIFICATE_CHAIN` | Paste from `.signing/plugin-signing.crt.base64` |
| `PRIVATE_KEY` | Paste from `.signing/plugin-signing.key.base64` |
| `PRIVATE_KEY_PASSWORD` | Leave empty (or omit) |
| `PUBLISH_TOKEN` | (Optional) Get from https://plugins.jetbrains.com |

### 2. Workflow Automatically Signs

The `build.yml` workflow will automatically:
1. Download the plugin
2. Sign it with the certificate and key
3. Create a release artifact

---

## Certificate Information

- **Type**: Self-signed RSA 4096-bit
- **Subject**: matching-colored-brackets
- **Organization**: jpeggdev
- **Valid Until**: 2029
- **Password**: None

---

## Troubleshooting

### Build fails with "CERTIFICATE_CHAIN not found"
- **Local**: Run `source .env` before building
- **CI/CD**: Verify GitHub secret is set correctly

### "Invalid certificate" error
- Ensure base64 string is complete (no truncation)
- Check file starts with: `-----BEGIN CERTIFICATE-----`

### Plugin signature verification fails
- Ensure `PRIVATE_KEY` matches `CERTIFICATE_CHAIN`
- Regenerate both if out of sync

---

## Regenerating Certificates

If needed (lost, compromised, or expired):

```bash
cd .signing

# Generate new certificate and key
openssl req -new -x509 -newkey rsa:4096 \
  -keyout plugin-signing.key \
  -out plugin-signing.crt \
  -days 1825 -nodes \
  -subj "/CN=matching-colored-brackets/O=jpeggdev/C=US"

# Encode for environment variables
base64 -w0 plugin-signing.crt > plugin-signing.crt.base64
base64 -w0 plugin-signing.key > plugin-signing.key.base64

# Update .env and GitHub secrets with new values
```

---

## See Also

- `PLUGIN_SIGNING_SETUP.md` - Detailed setup guide
- `build.gradle.kts` - Gradle signing configuration
- `.env.example` - Environment variables template
