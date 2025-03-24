// SegmentProcessorFactory.java
package io.sourcesync.sdk.ui.segments;

import android.view.ViewGroup;
import java.util.HashMap;
import java.util.Map;

import io.sourcesync.sdk.ui.segments.processors.ButtonSegmentProcessor;
import io.sourcesync.sdk.ui.segments.processors.ColumnSegmentProcessor;
import io.sourcesync.sdk.ui.segments.processors.ImageSegmentProcessor;
import io.sourcesync.sdk.ui.segments.processors.RowSegmentProcessor;
import io.sourcesync.sdk.ui.segments.processors.TextSegmentProcessor;

public class SegmentProcessorFactory {
    private final Map<String, SegmentProcessor> processors;
    private final ViewGroup parentContainer;

    public SegmentProcessorFactory(ViewGroup parentContainer) {
        this.parentContainer = parentContainer;
        this.processors = new HashMap<>();
        registerDefaultProcessors();
    }

    private void registerDefaultProcessors() {
        registerProcessor(new TextSegmentProcessor());
        registerProcessor(new ImageSegmentProcessor(parentContainer));
        registerProcessor(new ButtonSegmentProcessor());
        registerProcessor(new RowSegmentProcessor(this, parentContainer));
        registerProcessor(new ColumnSegmentProcessor(this, parentContainer));
    }

    public void registerProcessor(SegmentProcessor processor) {
        processors.put(processor.getSegmentType(), processor);
    }

    public SegmentProcessor getProcessor(String segmentType) {
        return processors.get(segmentType);
    }
}