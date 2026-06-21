package com.ccs.javadroid.util;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Нативний рендерер Markdown через SpannableStringBuilder.
 * Підтримує: заголовки, bold, italic, code, посилання, списки, цитати, HR.
 */
public final class MarkdownRenderer {

    private static final int COLOR_TEXT = 0xFFDFE1E5;
    private static final int COLOR_HEADING = 0xFFFFFFFF;
    private static final int COLOR_BOLD = 0xFFFFFFFF;
    private static final int COLOR_CODE_BG = 0xFF2B2D30;
    private static final int COLOR_CODE_TEXT = 0xFFA9B7C6;
    private static final int COLOR_LINK = 0xFF4A86C8;
    private static final int COLOR_LINK_TEXT = 0xFF6897BB;
    private static final int COLOR_BLOCKQUOTE = 0xFF808080;
    private static final int COLOR_LIST = 0xFFA9B7C6;
    private static final int COLOR_HR = 0xFF555555;
    private static final int COLOR_IMAGE = 0xFF6A8759;

    private static final int COLOR_TEXT_LIGHT = 0xFF3C3F41;
    private static final int COLOR_HEADING_LIGHT = 0xFF000000;
    private static final int COLOR_CODE_BG_LIGHT = 0xFFF5F5F5;
    private static final int COLOR_CODE_TEXT_LIGHT = 0xFF333333;
    private static final int COLOR_LINK_LIGHT = 0xFF2972C7;
    private static final int COLOR_HR_LIGHT = 0xFFCCCCCC;

    private MarkdownRenderer() {}

    @NonNull
    public static SpannableStringBuilder render(@NonNull String markdown, boolean dark) {
        return render(markdown, dark, 16);
    }

    @NonNull
    public static SpannableStringBuilder render(@NonNull String markdown, boolean dark, int baseFontSize) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        String[] lines = markdown.split("\n", -1);

        boolean inCodeBlock = false;
        StringBuilder codeBlock = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Fenced code block
            if (trimStart(line).startsWith("```")) {
                if (inCodeBlock) {
                    // End code block
                    String code = codeBlock.toString();
                    if (code.endsWith("\n")) code = code.substring(0, code.length() - 1);
                    appendCodeBlock(sb, code, dark, baseFontSize);
                    codeBlock.setLength(0);
                    inCodeBlock = false;
                } else {
                    inCodeBlock = true;
                }
                if (i < lines.length - 1 || inCodeBlock) sb.append("\n");
                continue;
            }

            if (inCodeBlock) {
                codeBlock.append(line).append("\n");
                continue;
            }

            // Empty line
            if (line.trim().isEmpty()) {
                sb.append("\n");
                continue;
            }

            // Horizontal rule
            if (isHorizontalRule(line)) {
                appendHR(sb, dark);
                sb.append("\n");
                continue;
            }

            // Heading
            int headingLevel = getHeadingLevel(line);
            if (headingLevel > 0) {
                appendHeading(sb, line.substring(headingLevel).trim(), headingLevel, dark, baseFontSize);
                sb.append("\n");
                continue;
            }

            // Blockquote
            if (trimStart(line).startsWith("> ")) {
                String content = trimStart(line).substring(2);
                appendBlockquote(sb, content, dark, baseFontSize);
                sb.append("\n");
                continue;
            }

            // Unordered list
            if (isUnorderedListItem(line)) {
                String content = getUnorderedListContent(line);
                appendListItem(sb, content, dark, baseFontSize, false, getIndentLevel(line));
                sb.append("\n");
                continue;
            }

            // Ordered list
            if (isOrderedListItem(line)) {
                String content = getOrderedListContent(line);
                appendListItem(sb, content, dark, baseFontSize, true, getIndentLevel(line));
                sb.append("\n");
                continue;
            }

            // Table: | col | col |
            if (line.trim().startsWith("|") && line.trim().endsWith("|")) {
                // Collect table rows
                List<String> tableRows = new ArrayList<>();
                while (i < lines.length && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                    tableRows.add(lines[i].trim());
                    i++;
                }
                i--; // will be incremented by for loop
                appendTable(sb, tableRows, dark, baseFontSize);
                sb.append("\n");
                continue;
            }

            // Task list: - [ ] or - [x]
            if (isTaskListItem(line)) {
                String content = getTaskListContent(line);
                boolean checked = line.contains("[x]") || line.contains("[X]");
                appendTaskListItem(sb, content, dark, baseFontSize, checked, getIndentLevel(line));
                sb.append("\n");
                continue;
            }

            // Regular paragraph
            appendParagraph(sb, line, dark, baseFontSize);
            sb.append("\n");
        }

        // Handle unclosed code block
        if (inCodeBlock && codeBlock.length() > 0) {
            String code = codeBlock.toString();
            if (code.endsWith("\n")) code = code.substring(0, code.length() - 1);
            appendCodeBlock(sb, code, dark, baseFontSize);
        }

        // Remove trailing newlines
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.delete(sb.length() - 1, sb.length());
        }

        return sb;
    }

    // ── Heading ──────────────────────────────────────────────

    private static void appendHeading(SpannableStringBuilder sb, String text, int level,
                                       boolean dark, int baseFontSize) {
        int start = sb.length();
        appendInlineFormatted(sb, text, dark);
        int end = sb.length();

        float scale;
        int color;
        switch (level) {
            case 1: scale = 1.6f; color = dark ? COLOR_HEADING : COLOR_HEADING_LIGHT; break;
            case 2: scale = 1.4f; color = dark ? COLOR_HEADING : COLOR_HEADING_LIGHT; break;
            case 3: scale = 1.2f; color = dark ? COLOR_HEADING : COLOR_HEADING_LIGHT; break;
            default: scale = 1.1f; color = dark ? COLOR_HEADING : COLOR_HEADING_LIGHT; break;
        }

        sb.setSpan(new RelativeSizeSpan(scale), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    // ── Code block ───────────────────────────────────────────

    private static void appendCodeBlock(SpannableStringBuilder sb, String code, boolean dark, int baseFontSize) {
        int start = sb.length();
        sb.append(code);
        int end = sb.length();

        sb.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(dark ? COLOR_CODE_TEXT : COLOR_CODE_TEXT_LIGHT),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new AbsoluteSizeSpan(baseFontSize - 2), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    // ── Blockquote ───────────────────────────────────────────

    private static void appendBlockquote(SpannableStringBuilder sb, String text, boolean dark, int baseFontSize) {
        int start = sb.length();
        sb.append(text);
        int end = sb.length();

        sb.setSpan(new ForegroundColorSpan(dark ? COLOR_BLOCKQUOTE : COLOR_LINK_LIGHT),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new LeadingMarginSpan.Standard(baseFontSize, 0), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    // ── List item ────────────────────────────────────────────

    private static void appendListItem(SpannableStringBuilder sb, String text, boolean dark,
                                        int baseFontSize, boolean ordered, int indent) {
        String bullet = ordered ? "  " : (indent > 0 ? "    " : "•  ");
        int start = sb.length();
        sb.append(bullet);
        sb.setSpan(new ForegroundColorSpan(dark ? COLOR_LIST : COLOR_TEXT_LIGHT),
                start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        start = sb.length();
        appendInlineFormatted(sb, text, dark);
    }

    // ── HR ───────────────────────────────────────────────────

    private static void appendHR(SpannableStringBuilder sb, boolean dark) {
        int start = sb.length();
        sb.append("────────────────────────────────────");
        int end = sb.length();
        sb.setSpan(new ForegroundColorSpan(dark ? COLOR_HR : COLOR_HR_LIGHT),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    // ── Paragraph with inline formatting ─────────────────────

    private static void appendParagraph(SpannableStringBuilder sb, String text, boolean dark, int baseFontSize) {
        appendInlineFormatted(sb, text, dark);
    }

    // ── Inline formatting (bold, italic, code, links) ────────

    private static void appendInlineFormatted(SpannableStringBuilder sb, String text, boolean dark) {
        int i = 0;
        int len = text.length();

        while (i < len) {
            char c = text.charAt(i);

            // Inline code: `...`
            if (c == '`') {
                int end = text.indexOf('`', i + 1);
                if (end < 0) end = len;
                int start = sb.length();
                sb.append(text, i + 1, end);
                sb.setSpan(new TypefaceSpan("monospace"), start, sb.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new ForegroundColorSpan(dark ? COLOR_CODE_TEXT : COLOR_CODE_TEXT_LIGHT),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                i = end + 1;
                continue;
            }

            // Bold+italic: ***...*** or ___...___
            if (i + 2 < len && ((c == '*' && text.charAt(i + 1) == '*' && text.charAt(i + 2) == '*')
                    || (c == '_' && text.charAt(i + 1) == '_' && text.charAt(i + 2) == '_'))) {
                char marker = c;
                int end = findClosingMarker(text, i + 3, marker, 3);
                if (end > 0) {
                    int start = sb.length();
                    sb.append(text, i + 3, end);
                    sb.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, sb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new ForegroundColorSpan(dark ? COLOR_BOLD : COLOR_HEADING_LIGHT),
                            start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = end + 3;
                    continue;
                }
            }

            // Bold: **...** or __...__
            if (i + 1 < len && ((c == '*' && text.charAt(i + 1) == '*')
                    || (c == '_' && text.charAt(i + 1) == '_'))) {
                char marker = c;
                int end = findClosingMarker(text, i + 2, marker, 2);
                if (end > 0) {
                    int start = sb.length();
                    sb.append(text, i + 2, end);
                    sb.setSpan(new StyleSpan(Typeface.BOLD), start, sb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new ForegroundColorSpan(dark ? COLOR_BOLD : COLOR_HEADING_LIGHT),
                            start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = end + 2;
                    continue;
                }
            }

            // Italic: *...* or _..._
            if (c == '*' || c == '_') {
                int end = text.indexOf(c, i + 1);
                if (end > 0 && end < len && text.charAt(end) != '\n') {
                    int start = sb.length();
                    sb.append(text, i + 1, end);
                    sb.setSpan(new StyleSpan(Typeface.ITALIC), start, sb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = end + 1;
                    continue;
                }
            }

            // Strikethrough: ~~...~~
            if (c == '~' && i + 1 < len && text.charAt(i + 1) == '~') {
                int end = text.indexOf("~~", i + 2);
                if (end > 0) {
                    int start = sb.length();
                    sb.append(text, i + 2, end);
                    sb.setSpan(new StrikethroughSpan(), start, sb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = end + 2;
                    continue;
                }
            }

            // Link: [text](url)
            if (c == '[') {
                int bracketEnd = text.indexOf(']', i + 1);
                if (bracketEnd > 0 && bracketEnd + 1 < len && text.charAt(bracketEnd + 1) == '(') {
                    int parenEnd = text.indexOf(')', bracketEnd + 2);
                    if (parenEnd > 0) {
                        String linkText = text.substring(i + 1, bracketEnd);
                        int start = sb.length();
                        sb.append(linkText);
                        sb.setSpan(new ForegroundColorSpan(dark ? COLOR_LINK_TEXT : COLOR_LINK_LIGHT),
                                start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        sb.setSpan(new UnderlineSpan(), start, sb.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        i = parenEnd + 1;
                        continue;
                    }
                }
            }

            // Image: ![alt](url)
            if (c == '!' && i + 1 < len && text.charAt(i + 1) == '[') {
                int bracketEnd = text.indexOf(']', i + 2);
                if (bracketEnd > 0 && bracketEnd + 1 < len && text.charAt(bracketEnd + 1) == '(') {
                    int parenEnd = text.indexOf(')', bracketEnd + 2);
                    if (parenEnd > 0) {
                        String alt = text.substring(i + 2, bracketEnd);
                        int start = sb.length();
                        sb.append("[img: ").append(alt).append("]");
                        sb.setSpan(new ForegroundColorSpan(dark ? COLOR_IMAGE : COLOR_LINK_LIGHT),
                                start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        i = parenEnd + 1;
                        continue;
                    }
                }
            }

            // LaTeX inline math: $...$
            if (c == '$' && (i == 0 || text.charAt(i - 1) != '\\')) {
                int end = text.indexOf('$', i + 1);
                if (end > 0 && end < len && (end + 1 >= len || text.charAt(end + 1) != '$')) {
                    String math = text.substring(i + 1, end);
                    int start = sb.length();
                    renderLatex(sb, math, dark);
                    i = end + 1;
                    continue;
                }
            }

            // LaTeX display math: $$...$$
            if (c == '$' && i + 1 < len && text.charAt(i + 1) == '$'
                    && (i == 0 || text.charAt(i - 1) != '\\')) {
                int end = text.indexOf("$$", i + 2);
                if (end > 0) {
                    String math = text.substring(i + 2, end);
                    int start = sb.length();
                    sb.append("\n");
                    renderLatex(sb, math, dark);
                    sb.append("\n");
                    i = end + 2;
                    continue;
                }
            }

            // LaTeX command: \command (in non-math context, just render as-is)
            if (c == '\\') {
                sb.append(c);
                i++;
                if (i < len && Character.isLetter(text.charAt(i))) {
                    int cmdStart = i;
                    while (i < len && Character.isLetter(text.charAt(i))) i++;
                    String cmd = text.substring(cmdStart, i);
                    // Greek letters
                    String greek = getGreekLetter(cmd);
                    if (greek != null) {
                        sb.delete(sb.length() - 1, sb.length()); // remove backslash
                        sb.append(greek);
                    } else {
                        sb.append(cmd);
                    }
                }
                continue;
            }

            sb.append(c);
            i++;
        }
    }

    // ── LaTeX renderer ───────────────────────────────────────

    private static void renderLatex(SpannableStringBuilder sb, String math, boolean dark) {
        int start = sb.length();
        int i = 0;
        int len = math.length();

        while (i < len) {
            char c = math.charAt(i);

            // LaTeX command
            if (c == '\\') {
                i++;
                if (i >= len) break;
                int cmdStart = i;
                while (i < len && Character.isLetter(math.charAt(i))) i++;
                String cmd = math.substring(cmdStart, i);

                // Consume braces for commands that take arguments
                int args = getArgumentsCount(cmd);
                StringBuilder rendered = new StringBuilder();
                rendered.append(getLatexSymbol(cmd));

                for (int a = 0; a < args && i < len; a++) {
                    while (i < len && math.charAt(i) == ' ') i++;
                    if (i < len && math.charAt(i) == '{') {
                        int braceDepth = 1;
                        int braceStart = i + 1;
                        i++;
                        while (i < len && braceDepth > 0) {
                            if (math.charAt(i) == '{') braceDepth++;
                            else if (math.charAt(i) == '}') braceDepth--;
                            i++;
                        }
                        String arg = math.substring(braceStart, i - 1);
                        if (a == 0) {
                            rendered.setLength(0);
                            rendered.append(getLatexSymbol(cmd)).append("(").append(arg).append(")");
                        } else {
                            rendered.append(" / (").append(arg).append(")");
                        }
                    } else if (i < len && math.charAt(i) == '\\') {
                        break;
                    } else {
                        break;
                    }
                }

                String result = rendered.toString();
                sb.append(result);
                continue;
            }

            // Superscript: ^{...} or ^x
            if (c == '^') {
                i++;
                if (i < len && math.charAt(i) == '{') {
                    int depth = 1;
                    int s = i + 1;
                    i++;
                    while (i < len && depth > 0) {
                        if (math.charAt(i) == '{') depth++;
                        else if (math.charAt(i) == '}') depth--;
                        i++;
                    }
                    sb.append("^").append(math.substring(s, i - 1));
                } else if (i < len) {
                    sb.append("^").append(math.charAt(i));
                    i++;
                }
                continue;
            }

            // Subscript: _{...} or _x
            if (c == '_') {
                i++;
                if (i < len && math.charAt(i) == '{') {
                    int depth = 1;
                    int s = i + 1;
                    i++;
                    while (i < len && depth > 0) {
                        if (math.charAt(i) == '{') depth++;
                        else if (math.charAt(i) == '}') depth--;
                        i++;
                    }
                    sb.append("_").append(math.substring(s, i - 1));
                } else if (i < len) {
                    sb.append("_").append(math.charAt(i));
                    i++;
                }
                continue;
            }

            // Braces: just skip
            if (c == '{' || c == '}') {
                i++;
                continue;
            }

            // Greek letters inline
            if (c == '\\' && i + 1 < len && Character.isLetter(math.charAt(i + 1))) {
                i++;
                int s = i;
                while (i < len && Character.isLetter(math.charAt(i))) i++;
                String cmd = math.substring(s, i);
                String greek = getGreekLetter(cmd);
                sb.append(greek != null ? greek : "\\" + cmd);
                continue;
            }

            sb.append(c);
            i++;
        }

        int end = sb.length();
        if (end > start) {
            sb.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(dark ? 0xFF6897BB : 0xFF2972C7),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static int getArgumentsCount(String cmd) {
        switch (cmd) {
            case "frac": case "dfrac": case "tfrac": return 2;
            case "sqrt": case "text": case "mathrm": case "mathbf": case "mathit":
            case "mathbb": case "mathcal": case "overline": case "underline":
            case "hat": case "bar": case "vec": case "dot": case "ddot":
                return 1;
            case "sum": case "prod": case "int": case "oint": case "iint":
            case "lim": case "infty": case "pi": case "alpha": case "beta":
            case "gamma": case "delta": case "epsilon": case "theta":
            case "lambda": case "mu": case "sigma": case "omega":
            case "partial": case "nabla": case "pm": case "mp":
            case "times": case "div": case "cdot": case "leq": case "geq":
            case "neq": case "approx": case "equiv": case "subset":
            case "supset": case "subseteq": case "supseteq": case "in":
            case "notin": case "cup": case "cap": case "emptyset":
            case "forall": case "exists": case "neg": case "land": case "lor":
            case "leftarrow": case "rightarrow": case "leftrightarrow":
            case "Leftarrow": case "Rightarrow": case "Leftrightarrow":
                return 0;
            default: return 1;
        }
    }

    private static String getLatexSymbol(String cmd) {
        switch (cmd) {
            case "frac": case "dfrac": case "tfrac": return "";
            case "sqrt": return "√";
            case "text": case "mathrm": case "mathbf": case "mathit": return "";
            case "overline": return "";
            case "underline": return "";
            case "hat": return "^";
            case "bar": return "¯";
            case "vec": return "→";
            case "dot": return "·";
            case "sum": return "∑";
            case "prod": return "∏";
            case "int": case "oint": return "∫";
            case "iint": return "∬";
            case "lim": return "lim";
            case "partial": return "∂";
            case "nabla": return "∇";
            case "infty": return "∞";
            case "pm": return "±";
            case "mp": return "∓";
            case "times": return "×";
            case "div": return "÷";
            case "cdot": return "·";
            case "leq": return "≤";
            case "geq": return "≥";
            case "neq": return "≠";
            case "approx": return "≈";
            case "equiv": return "≡";
            case "subset": return "⊂";
            case "supset": return "⊃";
            case "subseteq": return "⊆";
            case "supseteq": return "⊇";
            case "in": return "∈";
            case "notin": return "∉";
            case "cup": return "∪";
            case "cap": return "∩";
            case "emptyset": return "∅";
            case "forall": return "∀";
            case "exists": return "∃";
            case "neg": return "¬";
            case "land": return "∧";
            case "lor": return "∨";
            case "leftarrow": return "←";
            case "rightarrow": return "→";
            case "leftrightarrow": return "↔";
            case "Leftarrow": return "⇐";
            case "Rightarrow": return "⇒";
            case "Leftrightarrow": return "⇔";
            default: return "\\" + cmd;
        }
    }

    private static String getGreekLetter(String cmd) {
        switch (cmd) {
            case "alpha": return "α";
            case "beta": return "β";
            case "gamma": return "γ";
            case "delta": return "δ";
            case "epsilon": return "ε";
            case "zeta": return "ζ";
            case "eta": return "η";
            case "theta": return "θ";
            case "iota": return "ι";
            case "kappa": return "κ";
            case "lambda": return "λ";
            case "mu": return "μ";
            case "nu": return "ν";
            case "xi": return "ξ";
            case "omicron": return "ο";
            case "pi": return "π";
            case "rho": return "ρ";
            case "sigma": return "σ";
            case "tau": return "τ";
            case "upsilon": return "υ";
            case "phi": return "φ";
            case "chi": return "χ";
            case "psi": return "ψ";
            case "omega": return "ω";
            case "Gamma": return "Γ";
            case "Delta": return "Δ";
            case "Theta": return "Θ";
            case "Lambda": return "Λ";
            case "Xi": return "Ξ";
            case "Pi": return "Π";
            case "Sigma": return "Σ";
            case "Phi": return "Φ";
            case "Psi": return "Ψ";
            case "Omega": return "Ω";
            default: return null;
        }
    }

    private static int findClosingMarker(String text, int start, char marker, int count) {
        for (int i = start; i <= text.length() - count; i++) {
            boolean match = true;
            for (int j = 0; j < count; j++) {
                if (text.charAt(i + j) != marker) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    private static int getHeadingLevel(String line) {
        int level = 0;
        for (int i = 0; i < line.length() && i < 6; i++) {
            if (line.charAt(i) == '#') level++;
            else break;
        }
        if (level > 0 && level < line.length() && line.charAt(level) == ' ') return level;
        return 0;
    }

    private static boolean isHorizontalRule(String line) {
        String trimmed = line.trim();
        if (trimmed.length() < 3) return false;
        char c = trimmed.charAt(0);
        if (c != '-' && c != '*' && c != '_') return false;
        int count = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            if (trimmed.charAt(i) == c) count++;
            else if (trimmed.charAt(i) != ' ') return false;
        }
        return count >= 3;
    }

    private static boolean isUnorderedListItem(String line) {
        String trimmed = trimStart(line);
        return (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ "));
    }

    private static String getUnorderedListContent(String line) {
        String trimmed = trimStart(line);
        if (trimmed.startsWith("- ")) return trimmed.substring(2);
        if (trimmed.startsWith("* ")) return trimmed.substring(2);
        if (trimmed.startsWith("+ ")) return trimmed.substring(2);
        return trimmed;
    }

    private static boolean isOrderedListItem(String line) {
        String trimmed = trimStart(line);
        for (int i = 0; i < trimmed.length() && i < 4; i++) {
            if (!Character.isDigit(trimmed.charAt(i))) return false;
        }
        int dot = trimmed.indexOf('.');
        return dot > 0 && dot < 5 && dot + 1 < trimmed.length() && trimmed.charAt(dot + 1) == ' ';
    }

    private static String getOrderedListContent(String line) {
        String trimmed = trimStart(line);
        int dot = trimmed.indexOf('.');
        return dot > 0 ? trimmed.substring(dot + 2) : trimmed;
    }

    private static int getIndentLevel(String line) {
        int spaces = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') spaces++;
            else if (line.charAt(i) == '\t') spaces += 4;
            else break;
        }
        return spaces / 4;
    }

    private static String trimStart(String s) {
        int i = 0;
        while (i < s.length() && s.charAt(i) == ' ') i++;
        return s.substring(i);
    }

    // ── Task list ────────────────────────────────────────────

    private static boolean isTaskListItem(String line) {
        String trimmed = trimStart(line);
        return (trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ")
                || trimmed.startsWith("- [X] ") || trimmed.startsWith("* [ ] ")
                || trimmed.startsWith("* [x] ") || trimmed.startsWith("* [X] "));
    }

    private static String getTaskListContent(String line) {
        String trimmed = trimStart(line);
        if (trimmed.startsWith("- [ ] ") || trimmed.startsWith("* [ ] "))
            return trimmed.substring(6);
        if (trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ")
                || trimmed.startsWith("* [x] ") || trimmed.startsWith("* [X] "))
            return trimmed.substring(6);
        return trimmed;
    }

    private static void appendTaskListItem(SpannableStringBuilder sb, String text, boolean dark,
                                           int baseFontSize, boolean checked, int indent) {
        int pad = indent * 2;
        for (int p = 0; p < pad; p++) sb.append("  ");

        int start = sb.length();
        sb.append(checked ? "☑ " : "☐ ");
        sb.setSpan(new ForegroundColorSpan(dark ? 0xFF499C54 : 0xFF107C10),
                start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        start = sb.length();
        appendInlineFormatted(sb, text, dark);
        if (checked) {
            sb.setSpan(new StrikethroughSpan(), start, sb.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    // ── Table ────────────────────────────────────────────────

    private static void appendTable(SpannableStringBuilder sb, List<String> rows, boolean dark,
                                    int baseFontSize) {
        if (rows.isEmpty()) return;

        // Parse header
        String headerRow = rows.get(0);
        String[] headers = parseTableRow(headerRow);

        // Skip separator row (|---|---|)
        int dataStart = 1;
        if (rows.size() > 1 && rows.get(1).matches("\\|[-:|\\s]+\\|")) {
            dataStart = 2;
        }

        // Header
        int start = sb.length();
        sb.append("┌");
        for (int h = 0; h < headers.length; h++) {
            sb.append("─".repeat(Math.max(1, headers[h].length() + 2)));
            if (h < headers.length - 1) sb.append("┬");
        }
        sb.append("┐\n");
        sb.setSpan(new ForegroundColorSpan(dark ? 0xFF555555 : 0xFFCCCCCC),
                start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Header content
        start = sb.length();
        sb.append("│");
        for (int h = 0; h < headers.length; h++) {
            String cell = headers[h].trim();
            sb.append(" ").append(cell);
            int pad = Math.max(0, headers[h].length() + 1 - cell.length());
            for (int p = 0; p < pad; p++) sb.append(" ");
            sb.append("│");
        }
        sb.append("\n");
        sb.setSpan(new ForegroundColorSpan(dark ? COLOR_HEADING : COLOR_HEADING_LIGHT),
                start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new StyleSpan(Typeface.BOLD), start, sb.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Separator
        start = sb.length();
        sb.append("├");
        for (int h = 0; h < headers.length; h++) {
            sb.append("─".repeat(Math.max(1, headers[h].length() + 2)));
            if (h < headers.length - 1) sb.append("┼");
        }
        sb.append("┤\n");
        sb.setSpan(new ForegroundColorSpan(dark ? 0xFF555555 : 0xFFCCCCCC),
                start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Data rows
        for (int r = dataStart; r < rows.size(); r++) {
            String[] cells = parseTableRow(rows.get(r));
            start = sb.length();
            sb.append("│");
            for (int c = 0; c < headers.length; c++) {
                String cell = c < cells.length ? cells[c].trim() : "";
                sb.append(" ").append(cell);
                int pad = Math.max(0, headers.length > c ? headers[c].length() + 1 - cell.length() : 0);
                for (int p = 0; p < pad; p++) sb.append(" ");
                sb.append("│");
            }
            sb.append("\n");
            sb.setSpan(new ForegroundColorSpan(dark ? COLOR_TEXT : COLOR_TEXT_LIGHT),
                    start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Bottom border
        start = sb.length();
        sb.append("└");
        for (int h = 0; h < headers.length; h++) {
            sb.append("─".repeat(Math.max(1, headers[h].length() + 2)));
            if (h < headers.length - 1) sb.append("┴");
        }
        sb.append("┘\n");
        sb.setSpan(new ForegroundColorSpan(dark ? 0xFF555555 : 0xFFCCCCCC),
                start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static String[] parseTableRow(String row) {
        String trimmed = row.trim();
        if (trimmed.startsWith("|")) trimmed = trimmed.substring(1);
        if (trimmed.endsWith("|")) trimmed = trimmed.substring(0, trimmed.length() - 1);
        return trimmed.split("\\|", -1);
    }
}
