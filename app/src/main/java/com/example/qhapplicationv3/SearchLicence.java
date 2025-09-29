package com.example.qhapplicationv3;

import android.os.Bundle;

public class SearchLicence extends SearchBase {
    @Override protected String getFieldKey() { return "[* Licence number]"; }
    @Override protected String getScreenTitle() { return "Search by Licence Number"; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
