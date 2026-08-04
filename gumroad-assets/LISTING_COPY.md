# Gumroad Listing Copy — PocketVDR Free

## Title
PocketVDR Free — Personal Voyage Data Recorder

## Short description (one line, shows in search/cards)
Logs your voyage's position, speed, and heading offline — free, full history for 30 days.

## Price
$0

## Long description

**A personal voyage data recorder for your phone — no extra hardware, works with zero signal.**

PocketVDR gives small-craft sailors and boaters the same core idea as the certified VDR/S-VDR
equipment required on larger vessels, shrunk down to something that runs on the phone already in
your pocket. It continuously logs your position, speed, heading, and altitude throughout a
voyage, purely for your own record — reviewing what happened before an incident, proving a track
for insurance, checking performance after a passage, or just having a real answer instead of
relying on memory.

**Important: this is not a type-approved VDR or S-VDR under SOLAS, has no legal evidentiary
standing, and is a personal record-keeping tool only.**

**Zero internet dependency for logging, period.** The real environment this is built for is total
zero signal on deck, and often poor signal even below. PocketVDR logs using nothing but your
phone's own GPS chip and local storage — no cloud round-trip, nothing that can fail because the
boat is out of range.

**What's free:**
- Full logging — position, UTC time, speed, heading, altitude, and satellites used, at a
  configurable 5-60s interval
- Runs as a real foreground service — keeps working with the screen locked, survives Doze/battery
  optimization once you grant the exemption
- Live telemetry while logging: fix count, elapsed voyage time, satellites used, current position
- A big "Mark Incident" button — instantly tag a near-miss, weather event, or mechanical issue
  with a timestamp and optional note, findable later without scrubbing through hours of track
- Distinct voyage sessions with a list of past voyages you can reopen or permanently delete
- A review screen: track plotted on a simple offline canvas, a timeline scrubber, and a
  tap-to-jump list of your marked events
- Positions shown mariner-style — `xx.xxxxx N/S, yyy.yyyyy E/W` — not raw signed decimals
- Export any voyage (or just a selected time range) as GPX, plain NMEA 0183, or a plain-text log
  readable in Notepad/WordPad — GPX/NMEA open in OpenCPN, Google Earth, or ECDIS-class
  chartplotters; the text log is for handing to an insurer or surveyor with no special software
- Free for 30 days from first launch, with unlimited history review and export. After that,
  logging keeps working forever unlimited — only review/export narrows to the last 24 hours (or
  your current voyage). See PocketVDR Pro for unlimited history, permanently.

**Requirements:** Android 8.0 (Oreo) or newer. Installs via sideload (this isn't distributed
through Google Play) — your phone will need "install from unknown sources" enabled, which is
expected for anything not from a store.

**Source code:** fully open, MIT licensed —
[github.com/Trozovka/pocketvdr-lite](https://github.com/Trozovka/pocketvdr-lite)

## Screenshots to upload (in `gumroad-assets/`, in this order)
1. `01_free_main_idle.png` — main screen, idle, disclaimer and status visible
2. `02_free_main_logging.png` — logging active, the big Flag button
3. `04_free_review.png` — review screen with a real track, flag marker, and timeline

## File to upload
The signed release APK once built — `app-release.apk`, renamed to something like
`PocketVDR-Free-v1.0.0.apk` for clarity in the downloads folder.
