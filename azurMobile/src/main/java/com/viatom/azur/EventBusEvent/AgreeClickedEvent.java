package com.viatom.azur.EventBusEvent;

/**
 * Created by lili on 2018/5/24.
 */

public class AgreeClickedEvent {
    private boolean clicked;
    public AgreeClickedEvent(boolean b) {
        clicked = b;
    }

    public boolean isClicked() {
        return clicked;
    }
}
