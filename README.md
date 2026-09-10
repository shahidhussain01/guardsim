# GuardSim (Free Tier)

An Android call-screening app. Two things it does, both fully offline:

1. **Blocklist** — reject calls from numbers you've added.
2. **Silence unknown callers** (optional) — calls from numbers not in your
   contacts ring silently instead of loudly, without fully blocking them.

No AI, no server, no account, no internet permission needed for this version
— that comes later, once this base is live and working.

## How to build the APK for free (no Android Studio, no powerful PC)

1. Create a free account at github.com if you don't have one.
2. Click **New repository**. Name it `guardsim` and make sure it's set to
   **Public** (this is what makes the build minutes free and unlimited).
   Don't add a README/gitignore when creating it — leave it empty.
3. Open the new empty repo, click **Add file > Upload files**, then drag
   this whole `GuardSim` folder (all of it, including the hidden
   `.github` folder) into the upload box. Commit the files.
4. Go to the **Actions** tab of your repo. A workflow called "Build APK"
   should already be running (it starts automatically on upload). If it
   isn't running, click it and press **Run workflow**.
5. Wait for the green checkmark (a few minutes). Click into that run, and
   under **Artifacts** at the bottom you'll find `guardsim-debug-apk` —
   download it, it's a zip containing `app-debug.apk`.
6. Copy that `.apk` file to your phone (via USB, WhatsApp to yourself,
   Google Drive, anything) and tap it to install. You may need to allow
   "install unknown apps" for whichever app you used to open it — Android
   will prompt you for this the first time.
7. Open GuardSim, tap **Enable Call Screening**, and approve the system
   permission screen that appears.

That's it — zero cost, no laptop build tools required.
