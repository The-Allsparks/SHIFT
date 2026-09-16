package org.allsparks.shift.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AnalogTransformTest {
    @Test
    void deadbandRescalesRemainingRange() {
        AnalogTransform transform = AnalogTransform.builder().deadband(0.2).build();
        assertEquals(0.0, transform.apply(0.1), 1e-9);
        assertEquals(0.0, transform.apply(0.2), 1e-9);
        assertEquals(0.375, transform.apply(0.5), 1e-9);
        assertEquals(1.0, transform.apply(1.0), 1e-9);
        assertEquals(-0.375, transform.apply(-0.5), 1e-9);
    }

    @Test
    void invertHappensBeforeDeadband() {
        AnalogTransform transform =
                AnalogTransform.builder().deadband(0.1).invert(true).build();
        assertEquals(1.0, transform.apply(-1.0), 1e-9);
        assertEquals(-1.0, transform.apply(1.0), 1e-9);
    }

    @Test
    void scaleAndExponentAndClamp() {
        AnalogTransform transform = AnalogTransform.builder()
                .exponent(2.0)
                .scale(0.5)
                .min(-0.4)
                .max(0.4)
                .build();
        assertEquals(0.125, transform.apply(0.5), 1e-9);
        assertEquals(0.4, transform.apply(1.0), 1e-9);
        assertEquals(-0.4, transform.apply(-1.0), 1e-9);
    }

    @Test
    void thresholdTreatsAnalogAsDigital() {
        AnalogTransform transform = AnalogTransform.builder().threshold(0.7).build();
        assertFalse(transform.asDigital(0.69));
        assertTrue(transform.asDigital(0.7));
    }

    @Test
    void invalidParametersFailAtConstruction() {
        assertThrows(IllegalArgumentException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                AnalogTransform.builder().deadband(1.0).build();
            }
        });
        assertThrows(IllegalArgumentException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                AnalogTransform.builder().exponent(0.0).build();
            }
        });
    }
}
