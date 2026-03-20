package com.auto.clicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
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
    private static final long DELAY = 1500; // 1.5 sec delay between actions

    public static class Rule {
        public String trigger;
        public String action;
        public String typeText;
        public Rule(String t, String a, String tt) {
            trigger = t; action = a; typeText = tt;
        }
    }

    @Override
    public void onServiceConnected() {
        instance = this;
        Toast.makeText(this, "✅ Auto Clicker Service Connected!", Toast.LENGTH_SHORT).show();
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
            CharSequence hint = node.getHintText();

            boolean matched = false;
            if (text != null && text.toString().toLowerCase().contains(trigger)) matched = true;
            if (desc != null && desc.toString().toLowerCase().contains(trigger)) matched = true;
            if (hint != null && hint.toString().toLowerCase().contains(trigger)) matched = true;

            if (matched) {
                if (rule.action.equals("click")) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Toast.makeText(this, "Clicked: " + trigger, Toast.LENGTH_SHORT).show();
                    return true;
                } else if (rule.action.equals("type")) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Bundle args = new Bundle();
                    args.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        rule.typeText
                    );
                    node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                    Toast.makeText(this, "Typed in: " + trigger, Toast.LENGTH_SHORT).show();
                    return true;
                } else if (rule.action.equals("clear")) {
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    Toast.makeText(this, "Cleared! Error found: " + trigger, Toast.LENGTH_SHORT).show();
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
