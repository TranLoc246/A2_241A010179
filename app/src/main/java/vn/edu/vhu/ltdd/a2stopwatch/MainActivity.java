package vn.edu.vhu.ltdd.a2stopwatch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "A2_2201234567";

    // Khóa lưu trạng thái vào Bundle
    private static final String KEY_RUNNING = "running";
    private static final String KEY_ACCUMULATED = "accumulated";
    private static final String KEY_START = "start";
    private static final String KEY_RECREATE = "recreate";
    private static final String KEY_LAPS = "laps"; // Key mới cho NC1

    private TextView tvTime, tvStatus, tvRecreate, tvLaps;
    private Button btnStartPause, btnReset, btnLap;

    // Trạng thái của đồng hồ
    private boolean running = false;
    private long accumulated = 0L;
    private long startTime = 0L;
    private int recreateCount = 0;

    // Danh sách các vòng Lap (NC1)
    private ArrayList<String> lapList = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateTimeText();
            handler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tvTime = findViewById(R.id.tvTime);
        tvStatus = findViewById(R.id.tvStatus);
        tvRecreate = findViewById(R.id.tvRecreate);
        tvLaps = findViewById(R.id.tvLaps);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnReset = findViewById(R.id.btnReset);
        btnLap = findViewById(R.id.btnLap);

        if (savedInstanceState != null) {
            running = savedInstanceState.getBoolean(KEY_RUNNING);
            accumulated = savedInstanceState.getLong(KEY_ACCUMULATED);
            startTime = savedInstanceState.getLong(KEY_START);
            recreateCount = savedInstanceState.getInt(KEY_RECREATE) + 1;

            // Khôi phục danh sách Lap khi xoay màn hình (NC1)
            ArrayList<String> savedLaps = savedInstanceState.getStringArrayList(KEY_LAPS);
            if (savedLaps != null) {
                lapList = savedLaps;
            }

            Log.d(TAG, "onCreate: KHÔI PHỤC trạng thái, running=" + running
                    + ", accumulated=" + accumulated + "ms");
        } else {
            Log.d(TAG, "onCreate: khởi tạo mới (savedInstanceState = null)");
        }

        btnStartPause.setOnClickListener(v -> {
            if (running) {
                pauseStopwatch();
            } else {
                startStopwatch();
            }
        });

        btnReset.setOnClickListener(v -> resetStopwatch());

        // Sự kiện khi bấm nút Lap (NC1)
        btnLap.setOnClickListener(v -> recordLap());

        updateUi();
    }

    // ---------------- Logic đồng hồ ----------------

    private long elapsed() {
        return running ? accumulated + (SystemClock.elapsedRealtime() - startTime) : accumulated;
    }

    private void startStopwatch() {
        running = true;
        startTime = SystemClock.elapsedRealtime();
        startTicking();
        updateUi();
        Log.i(TAG, "BẮT ĐẦU đếm giờ");
    }

    private void pauseStopwatch() {
        accumulated += SystemClock.elapsedRealtime() - startTime;
        running = false;
        stopTicking();
        updateUi();
        Log.i(TAG, "TẠM DỪNG tại " + accumulated + "ms");
    }

    private void resetStopwatch() {
        running = false;
        accumulated = 0L;
        startTime = 0L;
        stopTicking();
        lapList.clear(); // Xóa lịch sử Lap khi reset
        updateUi();
        Log.i(TAG, "ĐẶT LẠI về 00:00.0");
    }

    // Ghi nhận vòng Lap hiện tại (NC1)
    private void recordLap() {
        if (elapsed() == 0) return; // Không bấm lap khi chưa chạy

        long ms = elapsed();
        long phut = ms / 60000;
        long giay = (ms % 60000) / 1000;
        long phanMuoi = (ms % 1000) / 100;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d.%d", phut, giay, phanMuoi);
        String lapText = String.format(Locale.getDefault(), "Vòng %d: %s", lapList.size() + 1, timeFormatted);

        lapList.add(0, lapText); // Thêm vòng mới lên đầu danh sách
        updateLapUi();
        Log.i(TAG, "GHI VÒNG: " + lapText);
    }

    private void startTicking() {
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    private void stopTicking() {
        handler.removeCallbacks(ticker);
    }

    // ---------------- Cập nhật giao diện ----------------

    private void updateTimeText() {
        long ms = elapsed();
        long phut = ms / 60000;
        long giay = (ms % 60000) / 1000;
        long phanMuoi = (ms % 1000) / 100;
        tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d.%d", phut, giay, phanMuoi));
    }

    private void updateLapUi() {
        StringBuilder sb = new StringBuilder();
        for (String lap : lapList) {
            sb.append(lap).append("\n");
        }
        tvLaps.setText(sb.toString());
    }

    private void updateUi() {
        updateTimeText();
        updateLapUi();
        btnStartPause.setText(running ? R.string.pause : R.string.start);
        tvStatus.setText(running ? R.string.status_running : R.string.status_paused);
        tvRecreate.setText(getString(R.string.recreate_count, recreateCount));
    }

    // ---------------- Vòng đời ----------------

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume – bật lại việc cập nhật giao diện nếu đồng hồ đang chạy");
        if (running) {
            startTicking();
        }
        updateUi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTicking();
        Log.d(TAG, "onPause – tạm dừng cập nhật giao diện");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onDestroy() {
        stopTicking();
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    // ---------------- Lưu & khôi phục trạng thái ----------------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_RUNNING, running);
        outState.putLong(KEY_ACCUMULATED, accumulated);
        outState.putLong(KEY_START, startTime);
        outState.putInt(KEY_RECREATE, recreateCount);

        // Lưu danh sách Lap vào Bundle khi xoay màn hình (NC1)
        outState.putStringArrayList(KEY_LAPS, lapList);

        Log.d(TAG, "onSaveInstanceState – đã lưu " + elapsed() + "ms vào Bundle");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState – được gọi sau onStart()");
    }
}