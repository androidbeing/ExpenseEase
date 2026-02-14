# Google Sheets Auto-Sync Implementation

## Overview
This implementation adds automatic synchronization of ExpenseEase data to Google Sheets every 12 hours without creating duplicates.

## Key Features

### 1. **Sheet Naming Convention**
- Sheet name format: `BUDGET_BUDDY_SHEET_2026` (year changes automatically)
- One sheet per year to keep data organized
- Implemented in `SheetUtils.getCurrentYearSheetName()`

### 2. **No Duplicate Data**
- Uses ID-based update/insert logic
- Checks existing data in sheets before writing
- Updates existing rows if ID matches
- Appends new rows only if ID doesn't exist
- Implemented in `appendOrUpdateData()` method

### 3. **12-Hour Auto Sync**
- WorkManager schedules periodic sync every 12 hours
- Only syncs when user is signed in to Google
- Only syncs data created/updated since last sync
- Requires network connectivity
- Runs in background without user interaction

## Files Modified

### 1. **MainActivity.kt**
Added `setupAutoSync()` method that:
- Creates periodic work request for 12-hour intervals
- Sets network connectivity constraint
- Schedules work using WorkManager
- Uses KEEP policy to avoid duplicate schedules

### 2. **SyncWorker.kt** (Completely rewritten)
- Checks Google sign-in status
- Retrieves data modified since last sync
- Prevents duplicate entries using ID-based updates
- Updates sync timestamp after successful sync
- Implements retry logic on failure

### 3. **BackupFragment.kt**
Updated `exportDataToSheets()` method to:
- Use ID-based update logic
- Prevent duplicates when syncing
- Include ID column in all sheets
- Update existing rows instead of appending duplicates

### 4. **SheetsServiceHelper.kt**
- Already configured correctly
- No changes needed

### 5. **gradle files**
Added dependencies:
- WorkManager Runtime KTX
- Hilt Work integration
- Hilt Work Compiler

## Sheet Structure

Each sheet contains these tabs:

### Expenses Sheet
| ID | Date | Type | Amount | Notes | CreatedAt | UpdatedAt |
|----|------|------|--------|-------|-----------|-----------|

### Budgets Sheet
| ID | Type | Amount | MonthYear | CreatedAt | UpdatedAt |
|----|------|--------|-----------|-----------|-----------|

### Wallet Sheet
| ID | Balance | AddedAmount | Notes | CreatedAt | UpdatedAt |
|----|---------|-------------|-------|-----------|-----------|

## How It Works

### Manual Sync (BackupFragment)
1. User clicks "Sync" button
2. Checks if user is signed in
3. If not, initiates Google sign-in
4. Retrieves data modified since last sync
5. Creates sheet if doesn't exist (BUDGET_BUDDY_SHEET_2026)
6. For each data row:
   - Reads existing sheet data
   - Checks if ID exists
   - Updates if exists, appends if new
7. Updates last sync timestamp
8. Shows success/failure message

### Auto Sync (SyncWorker)
1. Triggered every 12 hours automatically
2. Checks if user is signed in (skips if not)
3. Retrieves data modified since last sync
4. If no new data, skips sync
5. Creates/uses existing sheet
6. Performs ID-based updates (no duplicates)
7. Updates sync timestamp
8. Retries on failure

## Duplicate Prevention Logic

```kotlin
private suspend fun appendOrUpdateData(
    spreadsheetId: String,
    sheetName: String,
    newData: List<List<Any>>,
    idColumnIndex: Int
) {
    // 1. Read all existing data
    val existingData = sheetsServiceHelper.readData(...)
    
    // 2. Build map of ID -> row number
    val existingIds = mutableMapOf<Any, Int>()
    existingData.forEachIndexed { index, row ->
        if (index > 0) { // Skip header
            val id = row.getOrNull(idColumnIndex)
            if (id != null) {
                existingIds[id] = index + 1
            }
        }
    }
    
    // 3. For each new row:
    newData.forEach { row ->
        val id = row.getOrNull(idColumnIndex)
        val existingRowNumber = existingIds[id]
        
        if (existingRowNumber != null) {
            // UPDATE existing row
            sheetsServiceHelper.writeData(...)
        } else {
            // APPEND new row
            sheetsServiceHelper.writeData(...)
        }
    }
}
```

## Testing

### Test Manual Sync
1. Sign in to Google account in Backup screen
2. Add some expenses/budgets/wallet entries
3. Click "Sync" button
4. Verify data appears in Google Sheet
5. Modify existing data and sync again
6. Verify updates appear (no duplicates)

### Test Auto Sync
1. Ensure user is signed in
2. Add new data
3. Wait 12 hours OR trigger manually using WorkManager testing
4. Check Google Sheet for updated data
5. Verify no duplicates exist

### Manual WorkManager Trigger (for testing)
```kotlin
// In your test code or debug build
WorkManager.getInstance(context)
    .enqueueUniqueWork(
        "AutoSyncWork",
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<SyncWorker>().build()
    )
```

## Sync Indicators

### Last Sync Time
- Displayed in Backup Fragment
- Shows relative time (e.g., "2 hours ago")
- Updated after each successful sync

### Sync Status
- "Not Synced" - Never synced
- Timestamp - Last successful sync
- Progress bar during active sync

## Network Requirements

- Auto-sync only runs when connected to network
- Uses WorkManager network constraint
- Gracefully handles offline scenarios

## Error Handling

### Auto Sync Errors
- Logged to Logcat
- Automatically retries on failure
- Continues on next scheduled interval

### Manual Sync Errors
- Shows toast message to user
- Logs detailed error information
- Allows user to retry immediately

## Benefits

1. **No Duplicates**: ID-based logic prevents duplicate entries
2. **Efficient**: Only syncs changed data since last sync
3. **Automatic**: Runs every 12 hours without user action
4. **Battery Friendly**: Uses WorkManager scheduling
5. **Network Aware**: Only syncs when connected
6. **Year-Based Organization**: New sheet each year

## Future Enhancements

- Add pull/restore from sheets
- Conflict resolution for simultaneous edits
- Multiple device sync support
- Sync frequency customization
- Manual sync trigger from notification

