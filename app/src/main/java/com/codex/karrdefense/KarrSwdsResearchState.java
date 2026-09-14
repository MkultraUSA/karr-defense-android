package com.codex.karrdefense;

import android.text.TextUtils;

public class KarrSwdsResearchState {

    public final String matchedKeyword;
    public final int keywordMatchCount;
    public final boolean matchedInName;
    public final boolean matchedInAddress;
    public final boolean matchedInVendorData;

    public KarrSwdsResearchState(String matchedKeyword, int keywordMatchCount,
                                  boolean matchedInName, boolean matchedInAddress, boolean matchedInVendorData) {
        this.matchedKeyword = matchedKeyword;
        this.keywordMatchCount = keywordMatchCount;
        this.matchedInName = matchedInName;
        this.matchedInAddress = matchedInAddress;
        this.matchedInVendorData = matchedInVendorData;
    }
}
