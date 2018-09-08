package com.google.ar.core.examples.java.common.helpers;

/**
 * Created by ISeshagiribabu on 9/8/2018.
 */

import com.google.ar.core.Anchor;
public class GraphHelper {
    int anchorShortCode;
    int locationId;
    Anchor anchor;
    public GraphHelper(int anchorShortCode, int locationId, Anchor anchor) {
        this.anchorShortCode = anchorShortCode;
        this.locationId = locationId;
        this.anchor = anchor;
    }
    public int getAnchorShortCode() {
        return this.anchorShortCode;
    }
    public int getLocationId() {
        return this.locationId;
    }
    public Anchor getAnchor() {
        return this.anchor;
    }
}
