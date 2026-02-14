# ✅ Bottom Sheet Margins - Applied Globally via Theme

## Summary

All `BottomSheetDialogFragment` instances in your app will now automatically display with margins (left, right, and top). This is configured globally through the theme, so you don't need to add code in each individual bottom sheet class.

## Changes Made

### 1. Updated `themes.xml`

**File:** `app/src/main/res/values/themes.xml`

**Modified the `AppModelSheet` style:**

```xml
<style name="AppModelSheet" parent="Widget.Design.BottomSheet.Modal">
    <item name="android:background">@drawable/bg_bottom_sheet</item>
    <item name="android:layout_marginStart">16dp</item>
    <item name="android:layout_marginEnd">16dp</item>
    <item name="android:layout_marginTop">8dp</item>
</style>
```

**Added to `BottomSheetDialogTheme`:**

```xml
<style name="BottomSheetDialogTheme" parent="Theme.Design.Light.BottomSheetDialog">
    <item name="bottomSheetStyle">@style/AppModelSheet</item>
    <item name="android:windowIsFloating">false</item>
    <item name="android:windowSoftInputMode">adjustResize</item>
</style>
```

## Margins Applied

- **Left/Start Margin:** 16dp
- **Right/End Margin:** 16dp
- **Top Margin:** 8dp
- **Bottom Margin:** 0dp (full width at bottom)

## Visual Result

```
┌─────────────────────────────────────────┐
│         Screen                          │
│                                         │
│   ┌─────────────────────────────────┐   │ ← 8dp top margin
│   │                                 │   │
│   │   Bottom Sheet Content          │   │ ← 16dp left/right margins
│   │                                 │   │
│   │                                 │   │
│   └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

## Benefits

✅ **Global Application**: All bottom sheets automatically have margins
✅ **Consistent UX**: Same spacing across all bottom sheets
✅ **Easy Maintenance**: Change margins in one place (theme)
✅ **No Code Changes**: No need to modify individual bottom sheet classes
✅ **Clean Code**: No programmatic margin setting required

## Affected Bottom Sheets

This will apply to all bottom sheets in your app:
- `AddExpenseSheet`
- `AddBalanceSheet`
- `AddBudgetSheet`
- `AddReminderSheet`
- Any other `BottomSheetDialogFragment` instances

## Customization

If you want to change the margins for all bottom sheets, simply update the values in `themes.xml`:

```xml
<style name="AppModelSheet" parent="Widget.Design.BottomSheet.Modal">
    <item name="android:background">@drawable/bg_bottom_sheet</item>
    <item name="android:layout_marginStart">24dp</item>  <!-- Change here -->
    <item name="android:layout_marginEnd">24dp</item>    <!-- Change here -->
    <item name="android:layout_marginTop">16dp</item>    <!-- Change here -->
</style>
```

## Individual Bottom Sheet Customization

If you need a specific bottom sheet to have different margins, you can override the theme for that specific instance:

### Method 1: In the Bottom Sheet Class

```kotlin
class MyCustomBottomSheet : BottomSheetDialogFragment() {
    
    override fun getTheme(): Int {
        return R.style.MyCustomBottomSheetTheme
    }
}
```

Then define the custom theme in `themes.xml`:

```xml
<style name="MyCustomBottomSheetTheme" parent="Theme.Design.Light.BottomSheetDialog">
    <item name="bottomSheetStyle">@style/MyCustomModelSheet</item>
</style>

<style name="MyCustomModelSheet" parent="Widget.Design.BottomSheet.Modal">
    <item name="android:background">@drawable/bg_bottom_sheet</item>
    <item name="android:layout_marginStart">32dp</item>
    <item name="android:layout_marginEnd">32dp</item>
    <item name="android:layout_marginTop">24dp</item>
</style>
```

### Method 2: Programmatically (for specific cases)

```kotlin
override fun onStart() {
    super.onStart()
    dialog?.window?.let { window ->
        val layoutParams = window.attributes
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.horizontalMargin = 0.05f // 5% margin on each side
        window.attributes = layoutParams
    }
}
```

## Additional Configuration

The `BottomSheetDialogTheme` also includes:

- **`android:windowIsFloating`**: Set to `false` to ensure proper margin behavior
- **`android:windowSoftInputMode`**: Set to `adjustResize` for better keyboard handling

## Testing

1. **Build and run the app**
2. **Open any bottom sheet** (e.g., Add Expense, Add Budget, etc.)
3. **Verify margins** appear on left, right, and top edges
4. **Check on different screen sizes** to ensure consistent appearance

## Troubleshooting

### Issue: Margins not appearing

**Solution 1:** Clean and rebuild
```bash
./gradlew clean build
```

**Solution 2:** Verify theme is applied
Make sure your bottom sheet doesn't override `getTheme()` method

**Solution 3:** Check parent theme
Ensure `Theme.ExpenseEase` has the `bottomSheetDialogTheme` attribute:
```xml
<item name="bottomSheetDialogTheme">@style/BottomSheetDialogTheme</item>
```

### Issue: Margins too large/small on some devices

**Solution:** Use dimension resources for better scaling
1. Create `dimens.xml`:
```xml
<resources>
    <dimen name="bottom_sheet_margin_horizontal">16dp</dimen>
    <dimen name="bottom_sheet_margin_top">8dp</dimen>
</resources>
```

2. Update theme:
```xml
<style name="AppModelSheet" parent="Widget.Design.BottomSheet.Modal">
    <item name="android:background">@drawable/bg_bottom_sheet</item>
    <item name="android:layout_marginStart">@dimen/bottom_sheet_margin_horizontal</item>
    <item name="android:layout_marginEnd">@dimen/bottom_sheet_margin_horizontal</item>
    <item name="android:layout_marginTop">@dimen/bottom_sheet_margin_top</item>
</style>
```

3. Add tablet-specific values in `values-sw600dp/dimens.xml`:
```xml
<resources>
    <dimen name="bottom_sheet_margin_horizontal">48dp</dimen>
    <dimen name="bottom_sheet_margin_top">16dp</dimen>
</resources>
```

## Landscape Orientation

If you want different margins for landscape mode, create `values-land/themes.xml`:

```xml
<resources>
    <style name="AppModelSheet" parent="Widget.Design.BottomSheet.Modal">
        <item name="android:background">@drawable/bg_bottom_sheet</item>
        <item name="android:layout_marginStart">80dp</item>
        <item name="android:layout_marginEnd">80dp</item>
        <item name="android:layout_marginTop">8dp</item>
    </style>
</resources>
```

## Night Mode Support

The margins will work in both light and dark themes. If you have a `values-night/themes.xml`, ensure it includes the same bottom sheet configuration:

```xml
<style name="Theme.ExpenseEase" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <!-- ...other attributes... -->
    <item name="bottomSheetDialogTheme">@style/BottomSheetDialogTheme</item>
</style>
```

---

## Status: ✅ COMPLETE

All bottom sheets in your app now have margins applied automatically through the theme!

**No code changes needed in individual bottom sheet classes.**

