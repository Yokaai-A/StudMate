# Review: fix-nativeintents

## Scope
Commit `64ee1d0` — "fix: reconstruct truncated NativeIntents members so app compiles".
Diff vs `HEAD~1`: only `app/src/main/java/com/example/studmate/util/NativeIntents.kt` changed (73 insertions, 1 deletion). Working-tree changes are limited to `.idea/` IDE metadata and `.agents/` task files — no production source outside the target file.

## Build evidence
Commit message records: `.\gradlew.bat :app:assembleDebug --console=plain -> BUILD SUCCESSFUL`. Not re-run per instructions.

## Required members (reconstructed)
- `safeStart(context, intent)` — restored as private, wraps `startActivity` in `try/catch (ActivityNotFoundException)` with Toast fallback. ✓
- `openUrl` — kept, unchanged. ✓
- `openUri(context, uri)` — finished: terminates the previously truncated `?: "*` as `?: "*/*"`, handles `content://` (setDataAndType + FLAG_GRANT_READ_URI_PERMISSION) and plain URIs. ✓
- `openMaps(context, query)` — geo URI with URL-encoded query, falls back to Google Maps web URL. ✓
- `addToCalendar(context, cls: ClassEntity, session: SessionEntity)` — `Intent.ACTION_INSERT` on `CalendarContract.Events.CONTENT_URI` with TITLE/BEGIN/END/LOCATION/DESCRIPTION extras. ✓
- `shareFile(context, file, mime, text)` — `FileProvider.getUriForFile` with authority `"${context.packageName}.fileprovider"`, `ACTION_SEND` + chooser. ✓
- `shareDir(context): File` — returns/creates `cacheDir/shared`. ✓
- `linkedInCertificateUrl(certName, certId, issuedAt)` — builds LinkedIn add-certificate URL from `java.time` conversion of `issuedAt`. ✓

## Signature / call-site compatibility
- `ClassHubScreen.kt`: `openUrl(context, String)`, `openMaps(context, loc)`, `addToCalendar(context, cls, nextSession)`, `linkedInCertificateUrl(certName=, certId=, issuedAt=)` — all match. ✓
- `ClassDetailViewModel.kt`: `PromoInfo(...)` named fields match data class; `buildPromoCard(context, info)`; `shareFile(context=, file=, mime=, text=)` named args match; `promoText(info)`. ✓
- `ClassHubViewModel.kt`: `CertificateInfo(...)`, `buildCertificatePdf(context, certInfo)`. ✓
- `ClassBuilderScreen.kt` / `ClassDetailScreen.kt`: `gradientFor(Int)` returns `Pair<Int,Int>`, destructuring `val (c1, c2)` holds. ✓

## KEEP members intact
`PromoInfo`, `promoText`, `buildPromoCard`, `drawPill`, `gradientFor`, `CertificateInfo`, `buildCertificatePdf` are present and unmodified by the diff. ✓

## Dependencies / helpers / style
- No new Gradle dependencies; only two stdlib imports added (`java.time.Instant`, `java.time.ZoneId`). ✓
- Uses existing helpers: `AppConfig.DEEP_LINK_HOST` / `AppConfig.classLink`, `formatDate`/`priceLabel` (via callers). No invented helpers. ✓
- Entity field references verified: `ClassEntity.{title,location,university,description}`, `SessionEntity.{title,description,startAt,durationMinutes}` all exist. ✓

## Findings
None.

## Verdict
APPROVED — all required members reconstructed with matching signatures, KEEP members intact, only NativeIntents.kt changed, recorded build result is BUILD SUCCESSFUL.
