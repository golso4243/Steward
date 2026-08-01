package com.swornhero.steward.module.warning.model;

public record WarningSummary(
        int activeWarnings,
        int activePoints,
        int lifetimeWarnings,
        WarningRecommendation recommendation
) {

    public WarningSummary {
        activeWarnings =
                Math.max(
                        0,
                        activeWarnings
                );

        activePoints =
                Math.max(
                        0,
                        activePoints
                );

        lifetimeWarnings =
                Math.max(
                        0,
                        lifetimeWarnings
                );

        recommendation =
                recommendation != null
                        ? recommendation
                        : WarningRecommendation.NONE;
    }

    public boolean hasActiveWarnings() {
        return activeWarnings > 0;
    }
}