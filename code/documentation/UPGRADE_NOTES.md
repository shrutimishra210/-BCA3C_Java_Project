# SwapUni upgrade notes (colour theme, product photos, responsive grids)

## 1. Colour theme
All six palette colours live in `gui/UiTheme.java` (`CHERRY_BURGUNDY`, `CHARCOAL_OLIVE`, `OLIVE`,
`TERRACOTTA`, `WARM_CREAM`, `WHITE`). Every other shade (borders, hover tints, muted text, status
colours) is derived from them with `UiTheme.mix(...)`. The old constant names (`BG`, `ACCENT`, `PEACH`...)
were kept so no screen had to change. Warning / error colours keep their own hues on purpose.

## 2. Product photos
* `POST /api/listings/upload-image` (multipart, needs login) -> `{ "imageUrl": "/uploads/listings/<uuid>.jpg" }`
* The URL is sent as `imageUrl` in the normal create / update listing request and stored in `listings.image_url`
  (Hibernate `ddl-auto=update` adds the column; existing rows stay NULL = placeholder).
* On update, a missing `imageUrl` means "keep the current photo".
* JPG/JPEG/PNG/WEBP/GIF, max 5 MB. The server checks the file's real bytes, not just its name.
* Files are saved in `<home>/SwapUni/uploads` (override with the `UPLOAD_DIR` environment variable) and
  served at `/uploads/**`. Every upload gets a random name, so one product can never overwrite another's photo.
* `webp-imageio` was added to `build.gradle` because the JDK cannot decode WEBP by itself.

## 3. Grids
`CardGrid` now re-measures itself when the column count changes (no clipped/blank rows after resizing).
The Dashboard "Recent Marketplace Items" and My Listings now use it too, so cards sit side by side.

## 4. SwapYuni design tokens, resizable windows, login mini-game
* `UiTheme.java` now uses the SwapYuni palette (burgundy #68252F, slate #2D3231, sage #788277, forest #3C5042,
  terracotta #C3775E, parchment #EAE4DA, eggshell #F8F4EE ...). Big page titles use a serif face.
* Login and Sign Up were fixed-size windows; they are now resizable (maximize works) and keep the form centred.
  If you maximize the login window, the main window opens maximized too.
* The left panel of Login / Sign Up is a small game, "Pop the Swap": click the floating items to collect swaps,
  build streaks, catch the golden item (+5), or click the logo to spin it and release a swarm.
