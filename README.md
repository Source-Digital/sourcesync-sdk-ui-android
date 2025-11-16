# SourceSync SDK UI Android

A lightweight Android library for rendering interactive content overlays using DivKit's flexible JSON-based rendering system.

## Overview

SourceSync SDK UI Android provides production-ready UI components for displaying interactive content in your Android applications. The library offers:

- **Unified content display** with flexible rendering modes
- **DivKit-powered rendering** for flexible JSON-based layouts
- **Configurable positioning** with alignment options
- **Smart interaction handling** with outside-click detection
- **Efficient resource management** and memory cleanup
- **Custom URL handling** for deep links and external actions

## Installation

### Maven Central (Recommended)

Add the dependency to your app's `build.gradle`:

```gradle
dependencies {
    implementation 'io.sourcesync.sdk:sourcesync-sdk-ui-android:0.0.1'
}
```

### JitPack Alternative

Add JitPack repository to your root `build.gradle`:

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Then add the dependency:

```gradle
dependencies {
    implementation 'io.sourcesync.sdk:sourcesync-sdk-ui-android:0.0.1'
}
```

## Quick Start

### Basic Integration

```java
public class MainActivity extends AppCompatActivity {
    private ActivationView activationView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Get your container
        RelativeLayout container = findViewById(R.id.content_container);
        
        // Show content
        showContent(container);
    }
    
    private void showContent(RelativeLayout container) {
        try {
            // Create JSON data for your content
            JSONObject contentData = new JSONObject();
            contentData.put("card", createCardJson());
            
            // Configure the view
            ActivationConfig config = new ActivationConfig.Builder(this)
                .setPositionAlignment(new Alignment(
                    ActivationHorizontalAlignment.CENTER,
                    ActivationVerticalAlignment.CENTER
                ))
                .build();
            
            // Create and display the view
            activationView = ActivationView.createFromJson(
                this, 
                contentData, 
                config
            );
            
            container.addView(
                activationView, 
                activationView.getViewLayoutParams()
            );
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to show content", e);
        }
    }
    
    private JSONObject createCardJson() throws JSONException {
        // Create your DivKit card structure
        JSONObject card = new JSONObject();
        // Add your card configuration
        return card;
    }
    
    @Override
    protected void onDestroy() {
        if (activationView != null) {
            activationView.cleanup();
        }
        super.onDestroy();
    }
}
```

## Core Components

### ActivationView

The unified component for displaying both preview and detail modes.

```java
// Create from JSON
ActivationView view = ActivationView.createFromJson(
    context,
    jsonData,
    config
);

// Create from DivData
ActivationView view = ActivationView.createFromDivData(
    context,
    divData,
    config
);

// Clean up resources
view.cleanup();
```

### ActivationConfig

Configuration builder for activation behavior and appearance.

```java
ActivationConfig config = new ActivationConfig.Builder(context)
    // Set click handler for preview
    .setClickHandler(v -> showDetails())
    
    // Handle URL actions (links, deep links)
    .setUrlActionHandler(() -> trackAction())
    
    // Handle details close button
    .setDetailsCloseHandler(() -> hideDetails())
    
    // Handle outside clicks (dismiss)
    .setOutsideClickHandler(() -> hideDetails())
    
    // Set positioning
    .setPositionAlignment(new Alignment(
        ActivationHorizontalAlignment.RIGHT,
        ActivationVerticalAlignment.BOTTOM
    ))
    
    // Enable/disable visual error indicators
    .setVisualErrorsEnabled(false)
    
    .build();
```

## Positioning & Alignment

### Horizontal Alignment

```java
ActivationHorizontalAlignment.LEFT    // Align to left edge
ActivationHorizontalAlignment.CENTER  // Center horizontally
ActivationHorizontalAlignment.RIGHT   // Align to right edge
```

### Vertical Alignment

```java
ActivationVerticalAlignment.TOP       // Align to top edge
ActivationVerticalAlignment.CENTER    // Center vertically
ActivationVerticalAlignment.BOTTOM    // Align to bottom edge
```

### Example: Bottom-Right Positioning

```java
Alignment alignment = new Alignment(
    ActivationHorizontalAlignment.RIGHT,
    ActivationVerticalAlignment.BOTTOM
);

ActivationConfig config = new ActivationConfig.Builder(context)
    .setPositionAlignment(alignment)
    .build();
```

## JSON Template Structure

### Preview Template

```json
{
  "card": {
    "log_id": "preview_card",
    "states": [
      {
        "state_id": 0,
        "div": {
          "type": "container",
          "items": [
            {
              "type": "text",
              "text": "Tap to learn more",
              "font_size": 16,
              "text_color": "#FFFFFF"
            }
          ]
        }
      }
    ]
  }
}
```

### Details Template

```json
{
  "card": {
    "log_id": "details_card",
    "states": [
      {
        "state_id": 0,
        "div": {
          "type": "container",
          "orientation": "vertical",
          "items": [
            {
              "type": "image",
              "image_url": "https://example.com/image.jpg",
              "width": {
                "type": "match_parent"
              }
            },
            {
              "type": "text",
              "text": "Detailed content here",
              "font_size": 18
            },
            {
              "type": "text",
              "text": "Close",
              "actions": [
                {
                  "log_id": "close_action",
                  "url": "sourcesync://close"
                }
              ]
            }
          ]
        }
      }
    ]
  }
}
```

## Advanced Features

### Custom URL Handling

The SDK automatically handles various URL schemes:

- `sourcesync://close` - Closes details view
- `http://` / `https://` - Opens in browser
- `mailto:` - Opens email client
- `tel:` - Opens phone dialer
- `sms:` - Opens messaging app
- Custom schemes - Triggers URL action handler

### Outside Click Detection

Automatically detects clicks outside the activation view while preserving video control functionality:

```java
ActivationConfig config = new ActivationConfig.Builder(context)
    .setOutsideClickHandler(() -> {
        // User clicked outside - dismiss details
        hideActivationDetails();
    })
    .build();
```

The SDK intelligently ignores clicks in the bottom 100dp area to preserve video controls.

### Resource Management

Always clean up resources to prevent memory leaks:

```java
@Override
protected void onDestroy() {
    // Clean up activation view
    if (activationView != null) {
        activationView.cleanup();
    }
    
    super.onDestroy();
}
```

## Complete Example

Here's a complete example showing preview-to-details flow:

```java
public class ContentActivity extends AppCompatActivity {
    private static final String TAG = "ContentActivity";
    private ActivationView currentView;
    private RelativeLayout container;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        
        container = findViewById(R.id.content_container);
        
        // Show preview after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showPreview();
        }, 2000);
    }
    
    private void showPreview() {
        try {
            JSONObject previewJson = loadJsonFromAssets("preview_template.json");
            
            ActivationConfig config = new ActivationConfig.Builder(this)
                .setClickHandler(v -> {
                    Log.d(TAG, "Preview clicked");
                    showDetails();
                })
                .setPositionAlignment(new Alignment(
                    ActivationHorizontalAlignment.RIGHT,
                    ActivationVerticalAlignment.BOTTOM
                ))
                .build();
            
            currentView = ActivationView.createFromJson(
                this,
                previewJson,
                config
            );
            
            container.addView(
                currentView,
                currentView.getViewLayoutParams()
            );
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing preview", e);
        }
    }
    
    private void showDetails() {
        try {
            // Clean up preview
            if (currentView != null) {
                currentView.cleanup();
                container.removeView(currentView);
            }
            
            JSONObject detailJson = loadJsonFromAssets("detail_template.json");
            
            ActivationConfig config = new ActivationConfig.Builder(this)
                .setDetailsCloseHandler(this::hideContent)
                .setOutsideClickHandler(this::hideContent)
                .setUrlActionHandler(() -> {
                    Log.d(TAG, "URL action triggered");
                })
                .setPositionAlignment(new Alignment(
                    ActivationHorizontalAlignment.CENTER,
                    ActivationVerticalAlignment.CENTER
                ))
                .build();
            
            currentView = ActivationView.createFromJson(
                this,
                detailJson,
                config
            );
            
            container.addView(
                currentView,
                currentView.getViewLayoutParams()
            );
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing details", e);
        }
    }
    
    private void hideContent() {
        if (currentView != null) {
            currentView.cleanup();
            container.removeView(currentView);
            currentView = null;
        }
    }
    
    private JSONObject loadJsonFromAssets(String fileName) throws Exception {
        InputStream is = getAssets().open(fileName);
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        return new JSONObject(new String(buffer, StandardCharsets.UTF_8));
    }
    
    @Override
    protected void onDestroy() {
        hideContent();
        super.onDestroy();
    }
}
```

## Troubleshooting

### Content Not Showing

1. **Verify JSON structure**
   - Ensure `card` and `states` objects are present
   - Check for valid DivKit syntax
   - Validate JSON with a JSON validator

2. **Check container setup**
   ```java
   RelativeLayout container = findViewById(R.id.content_container);
   if (container == null) {
       Log.e(TAG, "Container not found");
   }
   ```

3. **Enable visual errors for debugging**
   ```java
   ActivationConfig config = new ActivationConfig.Builder(context)
       .setVisualErrorsEnabled(true)  // Shows DivKit errors
       .build();
   ```

### Memory Leaks

Always call `cleanup()` on ActivationView instances:

```java
@Override
protected void onDetachedFromWindow() {
    if (activationView != null) {
        activationView.cleanup();
        activationView = null;
    }
    super.onDetachedFromWindow();
}
```

### Layout Issues

Verify parent container supports RelativeLayout.LayoutParams:

```xml
<RelativeLayout
    android:id="@+id/content_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Your content and ActivationView will be added here -->
    
</RelativeLayout>
```

## ProGuard Configuration

If using ProGuard, the library includes consumer rules automatically. For manual configuration:

```proguard
-keep class io.sourcesync.sdk.ui.** { *; }
-keep class com.yandex.div.** { *; }
```

## Requirements

- **Minimum SDK**: Android 5.0 (API 21)
- **Target SDK**: Android 14 (API 34)
- **Language**: Java 8+ or Kotlin 1.9+

## Dependencies

The SDK automatically includes:

- DivKit rendering engine
- Picasso image loading

## Demo Applications

Explore the demo apps for complete integration examples:

- **demo-mobile**: Mobile phone implementation
- **demo-tv**: Android TV implementation

Run demos with:

```bash
./gradlew demo-mobile:installDebug
./gradlew demo-tv:installDebug
```

## Migration Guide

### From Legacy Components

If migrating from separate Preview/Details components:

**Before:**
```java
ActivationPreview preview = new ActivationPreview(context);
ActivationDetails details = new ActivationDetails(context);
```

**After:**
```java
// Single unified component for both preview and details
ActivationView view = ActivationView.createFromJson(
    context, jsonData, config
);
```

## Best Practices

1. **Always clean up resources** - Call `cleanup()` in onDestroy/onDetachedFromWindow
2. **Handle exceptions** - Wrap ActivationView creation in try-catch blocks
3. **Test positioning** - Verify alignment on different screen sizes and orientations
4. **Cache templates** - Load JSON templates once and reuse to improve performance
5. **Use appropriate handlers** - Configure handlers based on your use case (preview vs details)
6. **Validate JSON** - Test your DivKit JSON templates before deploying

## Support & Documentation

- **Issues**: [GitHub Issues](https://github.com/Source-Digital/sourcesync-android-sdk-ui/issues)
- **Documentation**: [docs/](./docs/)
- **Architecture**: [docs/sourcesync_architecture.md](./docs/sourcesync_architecture.md)

## License

Copyright © 2025 Source Digital, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.