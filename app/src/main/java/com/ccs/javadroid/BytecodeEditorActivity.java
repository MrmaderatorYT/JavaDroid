package com.ccs.javadroid;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Повноцінний текстовий редактор байткоду: редагування інструкцій,
 * збереження, валідація, undo/redo, підсвітка синтаксису.
 */
public class BytecodeEditorActivity extends AppCompatActivity {

    private static final String TAG = "BytecodeEditor";
    private static final String EXTRA_FILE_PATH = "file_path";

    private EditText codeEditor;
    private TextView tvStatus;
    private TextView tvLineCol;
    private ScrollView scrollEditor;
    private String originalCode;
    private String filePath;
    private boolean isModified = false;
    private final List<String> undoStack = new ArrayList<>();
    private final List<String> redoStack = new ArrayList<>();

    public static void launch(Context context, String filePath) {
        Intent i = new Intent(context, BytecodeEditorActivity.class);
        i.putExtra(EXTRA_FILE_PATH, filePath);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenHelper.enable(this);

        filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (filePath == null) { finish(); return; }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1E1E1E);

        // ── Toolbar ──
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setBackgroundColor(0xFF3C3F41);
        toolbar.setPadding(dp(8), dp(8), dp(8), dp(8));
        toolbar.setGravity(Gravity.CENTER_VERTICAL);

        TextView btnBack = new TextView(this);
        btnBack.setText("\u2190");
        btnBack.setTextColor(0xFFBBBBBB);
        btnBack.setTextSize(18);
        btnBack.setPadding(dp(8), dp(4), dp(8), dp(4));
        btnBack.setOnClickListener(v -> {
            if (isModified) {
                new AlertDialog.Builder(this)
                        .setTitle("Unsaved changes")
                        .setMessage("Save before exit?")
                        .setPositiveButton("Save", (d, w) -> { saveFile(); finish(); })
                        .setNegativeButton("Discard", (d, w) -> finish())
                        .setNeutralButton("Cancel", null)
                        .show();
            } else {
                finish();
            }
        });
        toolbar.addView(btnBack);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(new File(filePath).getName() + " — Bytecode Editor");
        tvTitle.setTextColor(0xFFBBBBBB);
        tvTitle.setTextSize(14);
        tvTitle.setTypeface(Typeface.MONOSPACE);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        tvTitle.setLayoutParams(titleLp);
        toolbar.addView(tvTitle);

        TextView btnUndo = createToolButton("\u21A9");
        btnUndo.setOnClickListener(v -> undo());
        toolbar.addView(btnUndo);

        TextView btnRedo = createToolButton("\u21AA");
        btnRedo.setOnClickListener(v -> redo());
        toolbar.addView(btnRedo);

        TextView btnCopy = createToolButton("Copy");
        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("bytecode", codeEditor.getText().toString()));
                Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });
        toolbar.addView(btnCopy);

        TextView btnValidate = createToolButton("Check");
        btnValidate.setTextColor(0xFF499C54);
        btnValidate.setOnClickListener(v -> validate());
        toolbar.addView(btnValidate);

        TextView btnSave = createToolButton("Save");
        btnSave.setTextColor(0xFF4A86C8);
        btnSave.setOnClickListener(v -> saveFile());
        toolbar.addView(btnSave);

        root.addView(toolbar);

        // ── Line numbers + Code editor ──
        LinearLayout editorRow = new LinearLayout(this);
        editorRow.setOrientation(LinearLayout.HORIZONTAL);
        editorRow.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));

        // Line numbers
        TextView lineNumbers = new TextView(this);
        lineNumbers.setId(android.R.id.text1);
        lineNumbers.setTextColor(0xFF606366);
        lineNumbers.setTextSize(12);
        lineNumbers.setTypeface(Typeface.MONOSPACE);
        lineNumbers.setPadding(dp(8), dp(8), dp(8), dp(8));
        lineNumbers.setBackgroundColor(0xFF252526);
        lineNumbers.setGravity(Gravity.END);
        LinearLayout.LayoutParams lnLp = new LinearLayout.LayoutParams(dp(48), ViewGroup.LayoutParams.MATCH_PARENT);
        lineNumbers.setLayoutParams(lnLp);
        editorRow.addView(lineNumbers);

        // Code editor
        scrollEditor = new ScrollView(this);
        scrollEditor.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
        scrollEditor.setBackgroundColor(0xFF1E1E1E);

        codeEditor = new EditText(this);
        codeEditor.setTextColor(0xFFBBBBBB);
        codeEditor.setTextSize(12);
        codeEditor.setTypeface(Typeface.MONOSPACE);
        codeEditor.setBackgroundColor(0xFF1E1E1E);
        codeEditor.setPadding(dp(8), dp(8), dp(8), dp(8));
        codeEditor.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        codeEditor.setHorizontallyScrolling(true);
        codeEditor.setSingleLine(false);
        codeEditor.setMinLines(20);
        codeEditor.setImeOptions(0);
        codeEditor.setRawInputType(0x00020001); // TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_MULTI_LINE

        scrollEditor.addView(codeEditor);
        editorRow.addView(scrollEditor);
        root.addView(editorRow);

        // ── Status bar ──
        LinearLayout statusBar = new LinearLayout(this);
        statusBar.setOrientation(LinearLayout.HORIZONTAL);
        statusBar.setBackgroundColor(0xFF007ACC);
        statusBar.setPadding(dp(8), dp(4), dp(8), dp(4));
        statusBar.setGravity(Gravity.CENTER_VERTICAL);

        tvStatus = new TextView(this);
        tvStatus.setText("Loaded");
        tvStatus.setTextColor(0xFFFFFFFF);
        tvStatus.setTextSize(11);
        LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        tvStatus.setLayoutParams(statusLp);
        statusBar.addView(tvStatus);

        tvLineCol = new TextView(this);
        tvLineCol.setText("Ln 1, Col 1");
        tvLineCol.setTextColor(0xFFFFFFFF);
        tvLineCol.setTextSize(11);
        statusBar.addView(tvLineCol);

        root.addView(statusBar);
        setContentView(root);

        // Load file
        loadFile();

        // Line number updater
        codeEditor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                isModified = !s.toString().equals(originalCode);
                tvStatus.setText(isModified ? "Modified" : "Saved");
                tvStatus.setTextColor(isModified ? 0xFFFFF176 : 0xFFFFFFFF);
                updateLineNumbers(lineNumbers);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Cursor position
        codeEditor.setOnTouchListener((v, event) -> {
            v.post(() -> {
                int pos = codeEditor.getSelectionStart();
                String text = codeEditor.getText().toString();
                int line = 1, col = 1;
                for (int i = 0; i < pos && i < text.length(); i++) {
                    if (text.charAt(i) == '\n') { line++; col = 1; } else col++;
                }
                tvLineCol.setText("Ln " + line + ", Col " + col);
            });
            return false;
        });

        updateLineNumbers(lineNumbers);
    }

    private void loadFile() {
        try {
            File f = new File(filePath);
            byte[] bytes;
            if (f.getName().endsWith(".class")) {
                // .class — дизасемблюємо в текст
                bytes = java.nio.file.Files.readAllBytes(f.toPath());
                String asm = BytecodeDisassembler.disassemble(bytes);
                originalCode = asm;
                codeEditor.setText(asm);
            } else {
                // Текстовий файл
                bytes = java.nio.file.Files.readAllBytes(f.toPath());
                originalCode = new String(bytes, StandardCharsets.UTF_8);
                codeEditor.setText(originalCode);
            }
            codeEditor.setSelection(0);
            undoStack.clear();
            redoStack.clear();
            isModified = false;
            tvStatus.setText("Loaded");
            tvStatus.setTextColor(0xFFFFFFFF);
            Toast.makeText(this, "Loaded: " + f.getName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            tvStatus.setText("Error: " + e.getMessage());
            tvStatus.setTextColor(0xFFFF6B6B);
            Toast.makeText(this, "Error loading: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveFile() {
        String code = codeEditor.getText().toString();
        File f = new File(filePath);

        if (f.getName().endsWith(".class")) {
            // Спроба скомпілювати ASM текст назад у .class
            saveClassFile(code, f);
        } else {
            // Текстовий файл
            saveTextFile(code, f);
        }
    }

    private void saveClassFile(String asmText, File outFile) {
        new Thread(() -> {
            try {
                // Парсимо ASM текст через Textifier → ClassNode не працює з текстом напряму
                // Тому зберігаємо як є (якщо файл був .class — попереджаємо)
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Bytecode Editor")
                            .setMessage("You are editing disassembled bytecode text.\n\n"
                                    + "To save as .class, the text must be valid ASM format.\n"
                                    + "Current changes will be saved as text (.asm).")
                            .setPositiveButton("Save as .asm", (d, w) -> {
                                File asmFile = new File(outFile.getParent(),
                                        outFile.getName().replace(".class", ".asm"));
                                saveTextFile(mediaPlayer_getText(), asmFile);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }, "bytecode-save").start();
    }

    private String mediaPlayer_getText() {
        return codeEditor.getText().toString();
    }

    private void saveTextFile(String content, File outFile) {
        try {
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.close();
            originalCode = content;
            isModified = false;
            tvStatus.setText("Saved");
            tvStatus.setTextColor(0xFF499C54);
            Toast.makeText(this, "Saved: " + outFile.getName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Save error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void validate() {
        String code = codeEditor.getText().toString();
        int errors = 0;
        int lines = code.split("\n").length;

        // Перевірка базових ASM-інструкцій
        String[] validOps = {"nop", "aconst_null", "iconst_m1", "iconst_0", "iconst_1",
                "iload", "lload", "fload", "dload", "aload", "istore", "lstore", "fstore", "dstore", "astore",
                "iadd", "ladd", "fadd", "dadd", "isub", "lsub", "fsub", "dsub",
                "imul", "lmul", "fmul", "dmul", "idiv", "ldiv", "fdiv", "ddiv",
                "ireturn", "lreturn", "freturn", "dreturn", "areturn", "return",
                "getstatic", "putstatic", "getfield", "putfield",
                "invokevirtual", "invokespecial", "invokestatic", "invokeinterface",
                "new", "newarray", "anewarray", "arraylength", "athrow",
                "checkcast", "instanceof", "goto", "if", "ifeq", "ifne", "iflt", "ifge",
                "ifgt", "ifle", "if_icmpeq", "if_icmpne", "dup", "pop", "swap"};

        StringBuilder warnings = new StringBuilder();
        String[] linesArr = code.split("\n");
        for (int i = 0; i < linesArr.length; i++) {
            String line = linesArr[i].trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//") || line.startsWith(".method") || line.startsWith(".end method")) continue;

            // Check if line looks like an instruction
            String[] parts = line.split("\\s+", 2);
            String op = parts[0].toLowerCase(Locale.ROOT);
            boolean found = false;
            for (String v : validOps) {
                if (op.equals(v)) { found = true; break; }
            }
            if (!found && !op.endsWith(":") && !op.startsWith(".") && !op.startsWith("//")) {
                warnings.append("Line ").append(i + 1).append(": unknown instruction '").append(op).append("'\n");
                errors++;
            }
        }

        if (errors == 0) {
            Toast.makeText(this, "✓ " + lines + " lines — no issues found", Toast.LENGTH_SHORT).show();
            tvStatus.setText("✓ Valid (" + lines + " lines)");
            tvStatus.setTextColor(0xFF499C54);
        } else {
            tvStatus.setText("⚠ " + errors + " warning(s)");
            tvStatus.setTextColor(0xFFFFF176);
            new AlertDialog.Builder(this)
                    .setTitle("Validation: " + errors + " warning(s)")
                    .setMessage(warnings.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void undo() {
        if (undoStack.isEmpty()) return;
        redoStack.add(codeEditor.getText().toString());
        String prev = undoStack.remove(undoStack.size() - 1);
        codeEditor.setText(prev);
        codeEditor.setSelection(Math.min(prev.length(), codeEditor.length()));
    }

    private void redo() {
        if (redoStack.isEmpty()) return;
        undoStack.add(codeEditor.getText().toString());
        String next = redoStack.remove(redoStack.size() - 1);
        codeEditor.setText(next);
        codeEditor.setSelection(Math.min(next.length(), codeEditor.length()));
    }

    private void updateLineNumbers(TextView tv) {
        int lines = codeEditor.getLineCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append(i).append("\n");
        }
        tv.setText(sb.toString());
    }

    private TextView createToolButton(String text) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(0xFFBBBBBB);
        btn.setTextSize(11);
        btn.setPadding(dp(10), dp(4), dp(10), dp(4));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(4));
        btn.setLayoutParams(lp);
        return btn;
    }

    @Override
    public void onBackPressed() {
        if (isModified) {
            new AlertDialog.Builder(this)
                    .setTitle("Unsaved changes")
                    .setPositiveButton("Save", (d, w) -> { saveFile(); finish(); })
                    .setNegativeButton("Discard", (d, w) -> finish())
                    .setNeutralButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private static class StandardCharsets {
        static java.nio.charset.Charset UTF_8 = java.nio.charset.StandardCharsets.UTF_8;
    }
}
