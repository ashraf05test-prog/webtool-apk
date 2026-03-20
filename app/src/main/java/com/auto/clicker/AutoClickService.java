package com.auto.clicker;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class AutoClickService extends AccessibilityService {
    public static AutoClickService instance;
    public static List<Rule> rules = new ArrayList<>();
    private static long lastActionTime = 0;
    private static final long DELAY = 1500;

    public static class Rule {
        public String trigger;
        public String action;
        public String typeText;
        public Rule(String t, String a, String tt) {
            trigger = t; action = a; typeText = tt;
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
        h.post(() -> Toast.makeText(getApplicationContext(), "✅ Connected!", Toast.LENGTH_LONG).show());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (rules.isEmpty()) return;
        long now = System.currentTimeMillis();
        if (now - lastActionTime < DELAY) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        boolean acted = scanAndAct(root);
        root.recycle();
        if (acted) lastActionTime = now;
    }

    boolean scanAndAct(AccessibilityNodeInfo node) {
        if (node == null) return false;

        for (Rule rule : rules) {
            String trigger = rule.trigger.toLowerCase().trim();
            CharSequence text = node.getText();
            CharSequence desc = node.getContentDescription();

            boolean matched = false;
            if (text != null && text.toString().toLowerCase().contains(trigger)) matched = true;
            if (desc != null && desc.toString().toLowerCase().contains(trigger)) matched = true;

            if (matched) {
                android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
                if (rule.action.equals("click")) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    h.post(() -> Toast.makeText(getApplicationContext(), "🖱 Clicked: " + trigger, Toast.LENGTH_SHORT).show());
                    return true;
                } else if (rule.action.equals("type")) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Bundle args = new Bundle();
                    args.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        rule.typeText
                    );
                    node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                    h.post(() -> Toast.makeText(getApplicationContext(), "⌨ Typed: " + trigger, Toast.LENGTH_SHORT).show());
                    return true;
                } else if (rule.action.equals("clear")) {
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    h.post(() -> Toast.makeText(getApplicationContext(), "🗑 Cleared!", Toast.LENGTH_SHORT).show());
                    return true;
                } else if (rule.action.equals("back")) {
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    return true;
                }
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                boolean acted = scanAndAct(child);
                child.recycle();
                if (acted) return true;
            }
        }
        return false;
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
