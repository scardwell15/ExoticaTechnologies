package exoticatechnologies.ui.java;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ButtonData {
    @Getter private final UIComponentAPI button;
    @Getter private final Object data;
    private final List<ButtonListener> listeners = new ArrayList<>();
    @Getter @Setter private boolean enabled = true;

    @Getter @Setter
    private boolean wasChecked = false;
    @Getter @Setter
    private boolean wasHighlighted = false;

    public boolean isButtonAPI() {
        return button instanceof ButtonAPI;
    }

    public boolean isChecked(InputEventAPI event) {
        //button API consumes mouse-down events before they are sent to the panel
        if (isButtonAPI()) {
            ButtonAPI buttonApi = (ButtonAPI) button;
            if (enabled && buttonApi.isEnabled()) {
                return buttonApi.isChecked() || (event.isLMBDownEvent() && button.getPosition().containsEvent(event));
            }
            return false;
        }
        if (enabled) {
            if (event.isLMBDownEvent()) {
                return button.getPosition().containsEvent(event);
            }
        }
        return false;
    }

    public boolean isHighlighted(InputEventAPI event) {
        //button API consumes mouse-down events before they are sent to the panel
        if (isButtonAPI()) {
            ButtonAPI buttonApi = (ButtonAPI) button;
            return buttonApi.isHighlighted() || (buttonApi.getPosition().containsEvent(event) && event.isMouseEvent());
        }
        return button.getPosition().containsEvent(event);
    }

    public boolean isChecked(List<InputEventAPI> events) {
        for (InputEventAPI event : events) {
            if (isChecked(event)) {
                return true;
            }
        }
        return false;
    }

    public boolean isHighlighted(List<InputEventAPI> events) {
        for (InputEventAPI event : events) {
            if (isHighlighted(event)) {
                return true;
            }
        }
        return false;
    }

    public void checkListeners(List<InputEventAPI> events) {
        boolean checkChanged = false;
        boolean highlightChanged = false;

        if (isChecked(events)) {
            if (!wasChecked) {
                wasChecked = true;
            } else {
                wasChecked = false;
            }
            checkChanged = true;
        }

        boolean isHighlighted = isHighlighted(events);
        if (isHighlighted && !wasHighlighted) {
            wasHighlighted = true;
            highlightChanged = true;
        } else if (!isHighlighted && wasHighlighted && (!isButtonAPI() || !Mouse.isButtonDown(0))) {
            wasHighlighted = false;
            highlightChanged = true;
        }

        if (checkChanged) {
            if (wasChecked) {
                callChecked();
            } else {
                callUnchecked();
            }
        }

        if (highlightChanged) {
            if (wasHighlighted) {
                callHighlighted();
            } else {
                callUnhighlighted();
            }
        }
    }

    public void callChecked() {
        for (ButtonListener listener : listeners) {
            listener.checked(this);
        }
    }

    public void callUnchecked() {
        for (ButtonListener listener : listeners) {
            listener.unchecked(this);
        }
    }

    public void callHighlighted() {
        for (ButtonListener listener : listeners) {
            listener.highlighted(this);
        }
    }

    public void callUnhighlighted() {
        for (ButtonListener listener : listeners) {
            listener.unhighlighted(this);
        }
    }

    public void addListener(ButtonListener listener) {
        if (listeners.contains(listener)) return;
        listeners.add(listener);
    }
}
