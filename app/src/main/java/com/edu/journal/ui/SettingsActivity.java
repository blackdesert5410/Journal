package com.edu.journal.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.edu.journal.R;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {
    private Spinner spinnerFont;
    private EditText etUsername;
    private Button btnSaveUsername;
    private Button btnClearCache;
    private Button btnReset;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "settings_pref";
    private static final String KEY_FONT = "selected_font";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        initViews();
        setupFontSpinner();
        loadSavedSettings();
        setupClickListeners();
    }

    private void initViews() {
        spinnerFont = findViewById(R.id.spinner_font);
        etUsername = findViewById(R.id.et_username);
        btnSaveUsername = findViewById(R.id.btn_save_username);
        btnClearCache = findViewById(R.id.btn_clear_cache);
        btnReset = findViewById(R.id.btn_reset);
    }

    private void setupFontSpinner() {
        String[] fonts = {"默认字体", "微软雅黑", "宋体", "黑体"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fonts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFont.setAdapter(adapter);
    }

    private void loadSavedSettings() {
        String savedFont = sharedPreferences.getString(KEY_FONT, "默认字体");
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, "");

        int fontPosition = 0;
        String[] fonts = {"默认字体", "微软雅黑", "宋体", "黑体"};
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i].equals(savedFont)) {
                fontPosition = i;
                break;
            }
        }
        spinnerFont.setSelection(fontPosition);
        etUsername.setText(savedUsername);
    }

    private void setupClickListeners() {
        spinnerFont.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedFont = parent.getItemAtPosition(position).toString();
                sharedPreferences.edit().putString(KEY_FONT, selectedFont).apply();
                Toast.makeText(SettingsActivity.this, "字体已更改为: " + selectedFont, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        btnSaveUsername.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (!username.isEmpty()) {
                sharedPreferences.edit().putString(KEY_USERNAME, username).apply();
                Toast.makeText(SettingsActivity.this, "用户名已保存", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
            }
        });

        btnClearCache.setOnClickListener(v -> clearCache());

        btnReset.setOnClickListener(v -> showResetConfirmDialog());
    }

    private void clearCache() {
        try {
            File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
            Toast.makeText(this, "缓存已清除", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "清除缓存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    private void showResetConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("重置应用")
                .setMessage("确定要重置应用吗？这将清除所有数据并重启应用。")
                .setPositiveButton("确定", (dialog, which) -> resetApp())
                .setNegativeButton("取消", null)
                .show();
    }

    private void resetApp() {
        try {
            // 清除所有SharedPreferences数据
            sharedPreferences.edit().clear().apply();
            
            // 清除缓存目录
            File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
            
            // 清除外部存储中的应用数据
            File externalFilesDir = getExternalFilesDir(null);
            if (externalFilesDir != null && externalFilesDir.isDirectory()) {
                deleteDir(externalFilesDir);
            }

            // 清除数据库文件
            File dbDir = new File(getApplicationInfo().dataDir + "/databases");
            if (dbDir.exists() && dbDir.isDirectory()) {
                deleteDir(dbDir);
            }

            Toast.makeText(this, "应用已重置", Toast.LENGTH_SHORT).show();
            
            // 跳转到应用设置页面，让用户手动清除权限
            new AlertDialog.Builder(this)
                .setTitle("清除权限")
                .setMessage("请在系统设置中清除应用权限，然后点击确定重启应用。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("直接重启", (dialog, which) -> restartApp())
                .show();
            
        } catch (Exception e) {
            Toast.makeText(this, "重置失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void restartApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        finish();
    }
}
