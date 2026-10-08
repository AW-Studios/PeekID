# PeekID

> Know who messaged you. Never what they said.

PeekID is a lightweight, open-source Android app that intercepts WhatsApp notifications and replaces message content with **"sent you a message"** — so you see the sender's name, nothing else.

No internet. No storage. No root. Just privacy.

---

## The Problem

When a WhatsApp message arrives, your notification bar shows this:

```
Ali
"bro where are you?"
```

Anyone glancing at your phone can read it. PeekID fixes that.

---

## What You See Instead

```
Ali
sent you a message
```

The real message is still waiting inside WhatsApp. PeekID just keeps it off your screen.

---

## How It Works

PeekID registers as a `NotificationListenerService` — a standard Android system API that requires no root and no special hardware.

When a WhatsApp notification arrives:

1. PeekID intercepts it before it appears on screen
2. Extracts only the sender name (`Notification.EXTRA_TITLE`)
3. Cancels the original notification (message content gone)
4. Posts a clean replacement: `"<Sender> sent you a message"`
5. Leaves WhatsApp's silent drawer notification untouched so you still have the badge

Calls, voice notes, media playback, and all other apps are completely ignored.

---

## What PeekID Will Never Touch

| Notification Type | Behavior |
|---|---|
| Incoming voice / video call | Completely ignored — rings normally |
| Active call bar | Completely ignored |
| Voice note playback controls | Completely ignored |
| Any app other than WhatsApp | Completely ignored |
| WhatsApp group summary | Left alone in drawer |

---

## Privacy Guarantee

- **No `INTERNET` permission** — the OS blocks PeekID from sending anything anywhere
- **No database or file storage** — nothing is ever written to disk
- **No analytics, no tracking, no accounts**
- **No root required**
- **Open source** — read every line of code yourself

---

## Selective Whitelist

You can whitelist specific contacts whose messages show normally.

Go to PeekID → add a contact name → their notifications come through unfiltered. Everyone else stays private.

---

## Known Limitation

Inline quick-reply is not available on PeekID's sanitized notification. Tap the notification to open WhatsApp and reply normally.

---

## Installation

### Option A — Build from source

```bash
git clone https://github.com/abdulwalidal/PeekID.git
cd PeekID
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Option B — Download APK

Grab the latest APK from [Releases](https://github.com/abdulwalidal/PeekID/releases).

---

## Setup After Installing

1. Open PeekID → tap **Grant Notification Access** → enable PeekID in the list
2. Tap **Allow** when prompted for battery optimisation exemption
3. **Samsung only**: Settings → Device Care → Battery → Background usage limits → Never sleeping apps → add PeekID

---

## Requirements

- Android 6.0 (API 23) or higher
- WhatsApp or WhatsApp Business installed

---

## Built With

- Kotlin
- Android `NotificationListenerService` API
- Material Design 3
- Zero third-party libraries

---

## License

MIT © Abdul
