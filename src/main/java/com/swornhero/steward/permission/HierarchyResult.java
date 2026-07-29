package com.swornhero.steward.permission;

public record HierarchyResult(
        boolean allowed,
        int actorWeight,
        int targetWeight,
        String denialMessage
) {

    public static HierarchyResult allowed(
            int actorWeight,
            int targetWeight
    ) {
        return new HierarchyResult(
                true,
                actorWeight,
                targetWeight,
                ""
        );
    }

    public static HierarchyResult denied(
            int actorWeight,
            int targetWeight,
            String denialMessage
    ) {
        return new HierarchyResult(
                false,
                actorWeight,
                targetWeight,
                denialMessage
        );
    }
}