package com.commafeed.backend.dao.datamigrationtoggles;

public class MigrationToggles {

    private static boolean isForkLiftOn = false;
    private static boolean isConsistencyCheckerOn = false;
    private static boolean isShadowWritesOn = false;
    private static boolean isShadowReadsOn = false;
    private static boolean isReadAndWriteOn = false;
    private static boolean isLongTermConsistencyOn = false;

    public static boolean isForkLiftOn() {
        return isForkLiftOn;
    }

    public static boolean isConsistencyCheckerOn() {
        return isConsistencyCheckerOn;
    }

    public static boolean isShadowWritesOn() {
        return isShadowWritesOn;
    }

    public static boolean isShadowReadsOn() {
        return isShadowReadsOn;
    }

    public static boolean isReadAndWriteOn() {
        return isReadAndWriteOn;
    }

    public static boolean isLongTermConsistencyOn() {
        return isLongTermConsistencyOn;
    }

    private static void turnAllTogglesOff() {
        isForkLiftOn = false;
        isConsistencyCheckerOn = false;
        isShadowWritesOn = false;
        isShadowReadsOn = false;
        isReadAndWriteOn = false;
        isLongTermConsistencyOn = false;
    }

    public static void turnForkLiftOn() {
        turnAllTogglesOff();
        isForkLiftOn = true;
    }

    public static void turnConsistencyCheckerOn() {
        turnForkLiftOn();
        isConsistencyCheckerOn = true;
    }

    public static void turnShadowWritesOn() {
        turnConsistencyCheckerOn();
        isShadowWritesOn = true;
    }

    public static void turnShadowReadsOn() {
        turnShadowWritesOn();
        isShadowReadsOn = true;
    }

    public static void turnReadAndWriteOn() {
        turnShadowReadsOn();
        isReadAndWriteOn = true;
    }

    public static void turnLongTermConsistencyOn() {
        turnReadAndWriteOn();
        isLongTermConsistencyOn = true;
    }
}
