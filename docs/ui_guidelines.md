# UI Guidelines

This guide provides comprehensive information on using, customizing, and styling UI components in the SourceSync SDK UI Android library.

## Overview

The SourceSync SDK UI uses **DivKit** (Yandex's declarative UI framework) to render JSON-based layouts. This approach provides:

- **Flexibility**: Define UIs in JSON without code changes
- **Consistency**: Same templates work across platforms (iOS/Android)
- **Dynamic Updates**: Change layouts without app updates
- **Rich Components**: Text, images, buttons, containers, and more

## Core Components

### ActivationView

The primary UI component for displaying content overlays.

#### Component Features

- ✅ DivKit-powered rendering
- ✅ Configurable positioning and alignment
- ✅ Click and touch handling
- ✅ Outside-click detection
- ✅ Automatic resource cleanup
- ✅ Custom URL handling

#### Basic Usage

```java
// Create configuration
ActivationConfig config = new ActivationConfig.Builder(context)
    .setPositionAlignment(new Alignment(
        ActivationHorizontalAlignment.CENTER,
        ActivationVerticalAlignment.CENTER
    ))
    .build();

// Create view from JSON
ActivationView view = ActivationView.createFromJson(
    context,
    jsonData,
    config
);

// Add to container
RelativeLayout container = findViewById(R.id.container);
container.addView(view, view.getViewLayoutParams());
```

#### Component Lifecycle

```
Create → Configure → Display → Interact → Cleanup
   ↓         ↓          ↓          ↓         ↓
 New     Config     addView    onClick   cleanup()
```

Always call `cleanup()` when removing the view to prevent memory leaks.

## Positioning & Layout

### Positioning System

ActivationView uses a **rule-based positioning** system with RelativeLayout parameters.

#### Horizontal Alignment

```java
enum ActivationHorizontalAlignment {
    LEFT,    // Align to parent left
    CENTER,  // Center horizontally
    RIGHT    // Align to parent right
}
```

#### Vertical Alignment

```java
enum ActivationVerticalAlignment {
    TOP,     // Align to parent top
    CENTER,  // Center vertically
    BOTTOM   // Align to parent bottom
}
```

#### Common Positioning Patterns

**Top-Left Corner**
```java
new Alignment(
    ActivationHorizontalAlignment.LEFT,
    ActivationVerticalAlignment.TOP
)
```

**Bottom-Right Corner** (Common for previews)
```java
new Alignment(
    ActivationHorizontalAlignment.RIGHT,
    ActivationVerticalAlignment.BOTTOM
)
```

**Full Center** (Common for details)
```java
new Alignment(
    ActivationHorizontalAlignment.CENTER,
    ActivationVerticalAlignment.CENTER
)
```

**Top-Center**
```java
new Alignment(
    ActivationHorizontalAlignment.CENTER,
    ActivationVerticalAlignment.TOP
)
```

### Container Requirements

ActivationView requires a **RelativeLayout** parent container:

```xml
<RelativeLayout
    android:id="@+id/content_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Your content -->
    <!-- ActivationView will be added programmatically -->
    
</RelativeLayout>
```

**Why RelativeLayout?**
- Supports rule-based positioning
- Allows overlaying content
- Efficient for overlay scenarios
- No nested layout inflation

### Layout Parameters

The SDK automatically calculates appropriate layout parameters:

```java
RelativeLayout.LayoutParams layoutParams = view.getViewLayoutParams();
// Returns configured params with:
// - width: WRAP_CONTENT
// - height: WRAP_CONTENT
// - alignment rules based on config
```

## DivKit Template Structure

### Basic Template Anatomy

```json
{
  "card": {
    "log_id": "unique_card_identifier",
    "states": [
      {
        "state_id": 0,
        "div": {
          "type": "container",
          "orientation": "vertical",
          "items": [
            // Child components
          ]
        }
      }
    ]
  },
  "templates": {
    // Optional reusable templates
  }
}
```

### Required Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `card` | Object | ✅ Yes | Root card container |
| `card.log_id` | String | ✅ Yes | Unique identifier |
| `card.states` | Array | ✅ Yes | State definitions |
| `div` | Object | ✅ Yes | Root component |


**Font Sizes**:
- Small: 12-14
- Medium: 16-18
- Large: 20-24
- Extra Large: 28+


**Image Loading**: Powered by Picasso
- Automatic caching
- Memory efficient
- Placeholder support


## Actions & Interactions

### Click Actions

```json
{
  "type": "text",
  "text": "Click me",
  "actions": [
    {
      "log_id": "click_action",
      "url": "sourcesync://close"
    }
  ]
}
```

### Supported URL Schemes

| Scheme | Description | Example |
|--------|-------------|---------|
| `sourcesync://close` | Close details view | `sourcesync://close` |
| `http://`, `https://` | Open in browser | `https://example.com` |
| `mailto:` | Open email client | `mailto:support@example.com` |
| `tel:` | Open phone dialer | `tel:+1234567890` |
| `sms:` | Open messaging app | `sms:+1234567890` |
| Custom schemes | App deep links | `myapp://screen/detail` |

### Action Handlers

Configure handlers in ActivationConfig:

```java
ActivationConfig config = new ActivationConfig.Builder(context)
    .setClickHandler(v -> {
        // Handle preview tap
        Log.d(TAG, "Preview clicked");
    })
    .setUrlActionHandler(() -> {
        // Handle URL actions (links, deep links)
        Log.d(TAG, "URL action triggered");
    })
    .setDetailsCloseHandler(() -> {
        // Handle close button
        hideDetails();
    })
    .setOutsideClickHandler(() -> {
        // Handle clicks outside view
        hideDetails();
    })
    .build();
```

## Styling Guidelines

### Color System

Use hex color codes for consistency:

```json
{
  "type": "container",
  "background": [
    {
      "type": "solid",
      "color": "#000000"  // Solid black
    }
  ]
}
```

**Alpha Channel Support**:
```json
"color": "#80000000"  // 50% transparent black
```

### Typography

**Font Weights**:
- `light` - 300
- `regular` - 400
- `medium` - 500
- `bold` - 700

**Best Practices**:
- Use 16sp minimum for body text
- Line height = font size × 1.5
- Limit to 2-3 font sizes per template

### Spacing System

Use consistent spacing scale:

```json
{
  "paddings": {
    "left": 16,    // 1x
    "top": 16,
    "right": 16,
    "bottom": 16
  }
}
```

**Recommended Scale**:
- Extra Small: 4dp
- Small: 8dp
- Medium: 16dp
- Large: 24dp
- Extra Large: 32dp


### Best Practices

1. **Provide descriptions** for all images
2. **Use semantic types** (button, header, etc.)
3. **Ensure contrast ratios** meet WCAG standards (4.5:1 minimum)
4. **Support text scaling** by using sp units

## Performance Optimization

### Image Optimization

1. **Use appropriate sizes** - Don't load 4K images for thumbnails
2. **Enable caching** - Picasso handles this automatically
3. **Use placeholders** - Provide base64 previews for faster loading
4. **Lazy load** - Only load images when needed

### Template Optimization

1. **Minimize nesting** - Keep hierarchy shallow (< 5 levels)
2. **Reuse templates** - Define common patterns once
3. **Avoid overdraw** - Don't layer unnecessary backgrounds
4. **Limit items** - Keep lists under 50 items per view

### Memory Management

```java
@Override
protected void onDestroy() {
    // Always cleanup
    if (activationView != null) {
        activationView.cleanup();
    }
    super.onDestroy();
}
```

## Debugging

### Visual Error Indicators

Enable visual errors to see rendering issues:

```java
ActivationConfig config = new ActivationConfig.Builder(context)
    .setVisualErrorsEnabled(true)  // Show red boxes on errors
    .build();
```

### DivKit Logs

Filter Logcat for DivKit messages:

```
tag:Div2View OR tag:DivKit OR tag:DivData
```

### Common Issues

**Text Not Showing**
- Check `text_color` contrasts with background
- Verify `font_size` is appropriate
- Ensure container has proper size

**Image Not Loading**
- Verify URL is accessible
- Check network permissions
- Look for Picasso errors in logs

**Layout Overflow**
- Use `wrap_content` instead of `match_parent`
- Check padding/margin values
- Verify container orientation

## Design Patterns

### Preview-Details Pattern

**Preview**: Compact, bottom-right positioned
```java
new Alignment(
    ActivationHorizontalAlignment.RIGHT,
    ActivationVerticalAlignment.BOTTOM
)
```

**Details**: Full-screen or centered overlay
```java
new Alignment(
    ActivationHorizontalAlignment.CENTER,
    ActivationVerticalAlignment.CENTER
)
```

### Card Pattern

```json
{
  "type": "container",
  "orientation": "vertical",
  "width": {"type": "match_parent"},
  "background": [{"type": "solid", "color": "#FFFFFF"}],
  "border": {
    "corner_radius": 12,
    "stroke": {"color": "#E0E0E0", "width": 1}
  },
  "paddings": {"left": 16, "top": 16, "right": 16, "bottom": 16},
  "items": [
    // Content
  ]
}
```

### List Pattern

```json
{
  "type": "container",
  "orientation": "vertical",
  "items": [
    {
      "type": "separator",
      "delimiter_style": {
        "color": "#E0E0E0"
      }
    },
    // List items
  ]
}
```

## Testing UI Components

### Visual Testing Checklist

- [ ] Test on different screen sizes (phone, tablet)
- [ ] Test both orientations (portrait, landscape)
- [ ] Test with different text scales
- [ ] Test light and dark modes
- [ ] Test with slow network (image loading)
- [ ] Test accessibility with TalkBack

### Example Test Template

```java
@Test
public void testActivationViewDisplay() {
    // Arrange
    JSONObject template = loadTestTemplate();
    ActivationConfig config = new ActivationConfig.Builder(context)
        .setPositionAlignment(new Alignment(
            ActivationHorizontalAlignment.CENTER,
            ActivationVerticalAlignment.CENTER
        ))
        .build();
    
    // Act
    ActivationView view = ActivationView.createFromJson(
        context, template, config
    );
    
    // Assert
    assertNotNull(view);
    assertNotNull(view.getViewLayoutParams());
}
```

## Resources

### DivKit Documentation
- [DivKit GitHub](https://github.com/yandex/divkit)
- [DivKit Playground](https://divkit.tech/playground)
- [Component Reference](https://divkit.tech/doc)

### Related Documentation
- [README](../README.md) - Quick start guide
- [Setup Guide](setup.md) - Development environment
- [Architecture](architecture.md) - System design

## Best Practices Summary

1. ✅ **Always call cleanup()** when removing views
2. ✅ **Use RelativeLayout** as parent container
3. ✅ **Test templates** in DivKit playground first
4. ✅ **Optimize images** for mobile displays
5. ✅ **Provide accessibility** descriptions
6. ✅ **Use consistent spacing** system
7. ✅ **Handle errors gracefully** with visual indicators
8. ✅ **Keep templates simple** - avoid deep nesting
9. ✅ **Cache templates** - load JSON once
10. ✅ **Test on real devices** - emulators can differ