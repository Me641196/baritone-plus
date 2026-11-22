# Baritone Plus (NeoForge 1.21.1)

Client-side addon for Baritone 1.11.2: auto-eat, auto-heal, danger avoidance (creepers, low-health flee)
and pathing profiles (`/bplus profile default|speed|safe`). Commands: `/bplus status`,
`/bplus eat|heal|danger on|off`. Config: `config/baritoneplus-client.toml`.

## Easiest build: GitHub does it for you
1. Create a free GitHub repo and upload everything in this folder (drag and drop in the browser).
2. In the repo choose Add file > Create new file, name it `.github/workflows/build.yml`, and paste in the
   contents of that file from this project. Commit.
3. Open the Actions tab, open the latest run, wait for the green check (~3 min), and download the
   `baritoneplus` artifact. Unzip it to get `baritoneplus-1.0.0.jar`.
4. Put the jar in `mods/` next to `baritone-standalone-neoforge-1.11.2.jar` (NeoForge 1.21.1).

## Local build without Gradle
Needs JDK 21 and NeoForge 1.21.1 installed: put `baritone-api-neoforge-1.11.2.jar` in `libs/`,
then run `build.sh` or `build.bat`.

Not compiled or tested by the author. Baritone is automation: check a server's rules before using it there.
