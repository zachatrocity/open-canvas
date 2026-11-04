# OpenCanvas

A focused native Android drawing canvas inspired by the fluid workspace and floating tool palette of Apple Freeform. It intentionally excludes shapes, sticky notes, and canvas text: every canvas object is a freehand stroke.

## Included

- Persistent drawing library with create, duplicate, rename, and delete actions
- Pannable dot-grid workspace with zoom controls
- Smooth pen strokes and translucent marker strokes
- Six drawing colors
- Stroke eraser
- Stroke selection and movement
- Undo and redo history
- Responsive portrait and landscape layouts
- Drawing-only welcome sketch on first launch

## Install

Download the signed APK from the [latest GitHub release](https://github.com/zachatrocity/open-canvas/releases/latest).

### Obtainium

1. Open Obtainium and choose **Add App**.
2. Enter `https://github.com/zachatrocity/open-canvas` as the app source URL.
3. Keep **GitHub** as the source and add the app.

Obtainium follows stable GitHub releases and selects the attached `OpenCanvas-vX.Y.Z.apk`. Release version codes are derived from semantic tags, so updates install in order.

## Gestures

1. Choose **Pen** or **Marker**, select a color, then drag to draw.
2. Choose **Move** and drag a stroke to reposition it.
3. With **Move** selected, drag empty canvas space to pan.
4. Choose **Eraser**, then scrub over strokes to remove them.
5. Use the `+` and `−` controls at the right edge to zoom.

## Build

Requires JDK 17 and Android SDK 35.

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Release builds are created from tags matching `vMAJOR.MINOR.PATCH`. The GitHub Actions workflow runs unit tests, signs the APK using repository secrets, verifies the signature, and attaches it to the matching GitHub release.

The project targets Android 15 and supports Android 8.0 and newer.
