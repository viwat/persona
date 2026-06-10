package com.example.persona.location.model;

import lombok.Builder;
import lombok.Value;

/**
 * A call-to-action shown on a location card (FR-03: Action Label / Action URL).
 * For example a "Open Account" button linking to an onboarding flow.
 */
@Value
@Builder
public class ActionLink {
    String label;
    String url;
}
