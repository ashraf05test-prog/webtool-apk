package com.auto.clicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.os.Bundle;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class AutoClickService extends AccessibilityService {
    public static AutoClickService instance;
    public static List<Rule> rules = new ArrayList<>();

    public static class Rule {
        public String triggerText;
        public String action; // click, type, clear, back
        public String typeText;
        public Rule(String t, String a, String tt) {
            triggerText = t; action = a; typeText = tt;
        }
    }

    @Override
    public void onServiceConnected() {
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (rules.isEmpty()) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        scanAndAct(root);
        root.recycle();
    }

    void scanAndAct(AccessibilityNodeInfo node) {
        if (node == null) return;
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();

        for (Rule rule : rules) {
            String trigger = rule.triggerText.toLowerCase();
            boolean matched = false;

            if (text != null && text.toString().toLowerCase().contains(trigger)) matched = true;
            if (desc != null && desc.toString().toLowerCase().contains(trigger)) matched = true;

            if (matched) {
                if (rule.action.equals("click")) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else if (rule.action.equals("type")) {
                    Bundle args = new Bundle();
                    args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, rule.typeText);
                    node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                } else if (rule.action.equals("clear")) {
                    performGlobalAction(GLOBAL_ACTION_BACK);
                } else if (rule.action.equals("back")) {
                    performGlobalAction(GLOBAL_ACTION_BACK);
                }
                return;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                scanAndAct(child);
                child.recycle();
            }
        }
    }

    public void clickAt(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.StrokeDescription stroke =
            new GestureDescription.StrokeDescription(path, 0, 50);
        GestureDescription gesture = new GestureDescription.Builder()
            .addStroke(stroke).build();
        dispatchGesture(gesture, null, null);
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
