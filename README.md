# PeekID

Privacy-first Android app that shows WHO messaged you on WhatsApp — without revealing WHAT they said.

## Why
When previewing incoming notifications on the lock screen or status bar, WhatsApp displays message content that anyone nearby can see. PeekID intercepts WhatsApp notifications, suppresses the original content, and re-posts a sanitized version that only shows the sender's name and "sent you a message".

## How it works
PeekID uses Android's `NotificationListenerService` API:
1. Intercepts incoming WhatsApp notifications.
2. Extracts the sender name from `Notification.EXTRA_TITLE`.
3. Cancels the original notification.
4. Re-posts a sanitized notification showing only the sender name followed by "sent you a message".

## Privacy Guarantee
- No `INTERNET` permission — PeekID cannot send your data anywhere.
- No storage or databases — nothing is saved.
- No root required.

## Build from Source
Open the project in Android Studio (Hedgehog or newer) or Cursor and build with Gradle.

## Permissions Explained
- **Notification Access (`BIND_NOTIFICATION_LISTENER_SERVICE`)**: Required to detect incoming notifications and dismiss them.
- **Ignore Battery Optimizations**: Recommended so Android / OEM battery savers do not terminate the background listener service.

## Known Limitations
- Inline quick reply from notification is not available since the sanitized notification is re-posted without WhatsApp's RemoteInput action. You can still open WhatsApp to reply normally.

## License
MIT
