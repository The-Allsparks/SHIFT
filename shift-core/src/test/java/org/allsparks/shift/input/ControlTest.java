package org.allsparks.shift.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ControlTest {
    @Test
    void aliasesResolveToCanonicalFaceButtons() {
        assertSame(Control.A, Control.parse("SOUTH"));
        assertSame(Control.A, Control.parse("cross"));
        assertSame(Control.B, Control.parse("EAST"));
        assertSame(Control.B, Control.parse("circle"));
        assertSame(Control.X, Control.parse("WEST"));
        assertSame(Control.X, Control.parse("square"));
        assertSame(Control.Y, Control.parse("NORTH"));
        assertSame(Control.Y, Control.parse("triangle"));
        assertSame(Control.BACK, Control.parse("share"));
        assertSame(Control.BACK, Control.parse("create"));
        assertSame(Control.START, Control.parse("options"));
        assertSame(Control.GUIDE, Control.parse("ps"));
        assertSame(Control.TOUCHPAD_FINGER_1, Control.parse("finger1"));
        assertSame(Control.TOUCHPAD_FINGER_2, Control.parse("touchpad-2"));
        assertSame(Control.LEFT_TRIGGER, Control.parse("LT"));
        assertSame(Control.LEFT_TRIGGER_PRESSED, Control.parse("LTPRESSED"));
        assertSame(Control.LEFT_TRIGGER_PRESSED, Control.parse("lt-click"));
        assertSame(Control.RIGHT_TRIGGER_PRESSED, Control.parse("RTPRESSED"));
        assertSame(Control.RIGHT_TRIGGER_PRESSED, Control.parse("right_trigger_button"));
    }

    @Test
    void unknownControlThrows() {
        assertThrows(IllegalArgumentException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                Control.parse("left_pedal");
            }
        });
    }

    @Test
    void snapshotIgnoresAliasDuplication() {
        InputSnapshot snapshot = InputSnapshot.builder()
                .setDigital(Control.parse("CIRCLE"), true)
                .build();
        assertTrue(snapshot.digital(Control.B));
        assertTrue(snapshot.digital(Control.parse("EAST")));
        assertEquals(1.0, snapshot.analog(Control.B), 0.0);
    }
}
