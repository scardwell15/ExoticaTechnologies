package exoticatechnologies.ui.java;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExclusiveButtonGroup implements Iterable<ButtonData> {
    private final List<ButtonData> buttons = new ArrayList<>();
    private final List<ButtonListener> listeners = new ArrayList<>();

    public void addButton(ButtonData data) {
        buttons.add(data);
    }

    public void addButton(ButtonAPI button, Object data) {
        addButton(new ButtonData(button, data));
    }

    public void checkListeners(List<InputEventAPI> events) {
        boolean checkChanged = false;
        boolean highlightChanged = false;
        for (ButtonData button : buttons) {
            if (button.isChecked(events)) {
                button.setWasChecked(!button.isWasChecked());
                checkChanged = true;
            }

            boolean isHighlighted = button.isHighlighted(events);
            if (isHighlighted && !button.isWasHighlighted()) {
                button.setWasHighlighted(true);
                highlightChanged = true;
            } else if (!isHighlighted && button.isWasHighlighted() && (!button.isButtonAPI() || Mouse.isButtonDown(0))) {
                boolean isButton = button.isButtonAPI();
                boolean lmbDown = Mouse.isButtonDown(0);
                button.setWasHighlighted(false);
                highlightChanged = true;
            }

            if (checkChanged) {
                for (ButtonData otherButton : buttons) {
                    if (button != otherButton) {
                        otherButton.setWasChecked(false);
                    }
                }
                if (button.isWasChecked()) {
                    callChecked(button);
                } else {
                    callUnchecked(button);
                }
            }

            if (highlightChanged) {
                for (ButtonData otherButton : buttons) {
                    if (button != otherButton) {
                        otherButton.setWasHighlighted(false);
                    }
                }
                if (button.isWasHighlighted()) {
                    callHighlighted(button);
                } else {
                    callUnhighlighted(button);
                }
            }

            if (checkChanged || highlightChanged) break;
        }
    }

    public void addListener(ButtonListener listener) {
        listeners.add(listener);
    }

    public void clear() {
        buttons.clear();
    }

    public void callChecked(ButtonData button) {
        for (ButtonListener listener : listeners) {
            listener.checked(button);
        }
    }

    public void callUnchecked(ButtonData button) {
        for (ButtonListener listener : listeners) {
            listener.unchecked(button);
        }
    }

    public void callHighlighted(ButtonData button) {
        for (ButtonListener listener : listeners) {
            listener.highlighted(button);
        }
    }

    public void callUnhighlighted(ButtonData button) {
        for (ButtonListener listener : listeners) {
            listener.unhighlighted(button);
        }
    }

    @NotNull
    @Override
    public Iterator<ButtonData> iterator() {
        return buttons.iterator();
    }
}
