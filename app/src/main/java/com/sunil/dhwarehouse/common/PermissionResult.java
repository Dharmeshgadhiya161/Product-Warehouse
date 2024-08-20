package com.sunil.dhwarehouse.common;

public interface PermissionResult {

    void permissionGranted();

    void permissionDenied();

    void permissionForeverDenied();
}
