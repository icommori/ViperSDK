package com.innocomm.testreadercmd;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.innocomm.vipersdk.BCRManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView logtext;
    private EditText editor;
    private BCRManager mBCRManager;
    private boolean toggle_illumination = false;

    private long mStartTestTime;
    public long Test10 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        logtext = findViewById(R.id.logtext);
        logtext.setMovementMethod(new ScrollingMovementMethod());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        editor = findViewById(R.id.editor);
        editor.setInputType(InputType.TYPE_NULL);
        editor.setTextIsSelectable(true);
        editor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    example_confirm();//match this behavior to your 'Send' (or Confirm) button
                }
                return true;
            }
        });
        logtext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    editor.requestFocus();
                    editor.selectAll();
                }
            }
        });
        mBCRManager = new BCRManager(this);
        mBCRManager.setParam(BCRManager.KEY_QRCODE, 1);
        Log.v(TAG, "" + mBCRManager.getBCRDrvName());
    }

    private void example_confirm() {
        editor.requestFocus();
        editor.selectAll();
        PrintLog(editor.getText().toString());

    }

    @Override
    protected void onPause() {
        super.onPause();
        autoTestStarting = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (autoTestStarting) {
            MenuItem settingsItem = menu.findItem(R.id.action_autotest);
            settingsItem.setTitle(R.string.action_autotest_stop);
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName("com.innocomm.iscanservice", "com.innocomm.iscanservice.Act_Camera"));
        PackageManager manager = getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
        if (infos.size() == 0) {
            menu.removeItem(R.id.action_camera);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (autoTestStarting) {
            MenuItem settingsItem = menu.findItem(R.id.action_autotest);
            settingsItem.setTitle(R.string.action_autotest_stop);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_get) {
            PrintLog("Reader Enable: " + mBCRManager.GetReaderSetting());
            PrintLog("Touch Enable: " + mBCRManager.GetTouchSetting());
            return true;
        } else if (id == R.id.action_async_scan) { //silent mode
            AsyncScan();
            return true;
        } else if (id == R.id.action_scan) { //silent mode
            mBCRManager.scan(true);
            return true;
        } else if (id == R.id.action_touch_enable) {
            mBCRManager.setTouchEnable(true);
            return true;
        } else if (id == R.id.action_touch_disable) {
            mBCRManager.setTouchEnable(false);
            return true;
        } else if (id == R.id.action_reader_enable) {
            mBCRManager.setReaderEnable(true);
            return true;
        } else if (id == R.id.action_reader_disable) {
            mBCRManager.setReaderEnable(false);
            return true;
        } else if (id == R.id.action_autotest) {
            autoTestStarting = !autoTestStarting;
            if (autoTestStarting) {
                PrintLog("Start testing...");
                TestList.clear();
                TotalTest = 0;
                Test10 = 0;
                mStartTestTime = Calendar.getInstance().getTimeInMillis();
                mHandler.sendEmptyMessage(MSG_TEST);
            }
            return true;
        } else if (id == R.id.action_camera) {
            StartCameraAct();
            return true;
        } else if (id == R.id.action_toggle_illumination) {
            toggle_illumination = !toggle_illumination;
            mBCRManager.setParam(BCRManager.KEY_Decoding_Illumination_level, toggle_illumination ? 0 : 10);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void AsyncScan() {
        mBCRManager.asyncScan(new BCRManager.ScanResultListener() {
            @Override
            public void onDone(boolean success, String result) {
                if (success) {
                    PrintLog("Result: " + result);

                } else {
                    PrintLog("HTTP Fail: " + result);
                }
            }
        });
    }

    private void PrintLog(final String log) {
        Log.v(TAG, log);
        logtext.post(new Runnable() {
            @Override
            public void run() {
                logtext.append(log + "\n");
            }
        });

    }

    ///Test
    private boolean autoTestStarting = false;
    private static final long DELAY_TIME = 0;
    public static final int COUNTER_THRESHHOLD = 100;
    public static final int MSG_TEST = 0;
    public long TotalTest = 0;
    public List<Long> TestList = new ArrayList<>();
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TEST:

                    HandleTest();
                    break;
            }
        }
    };

    private void HandleTest() {
        mBCRManager.asyncScan(new BCRManager.ScanResultListener() {
            @Override
            public void onDone(boolean success, String result) {
                if (isFinishing()) return;

                if (success) {
                    long diff = 0;
                    try {
                        JSONObject jobj = new JSONObject(result);
                        diff = jobj.getLong("decode_time");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.v(TAG, "Test Time: " + diff + " " + result);
                    TestList.add(diff);
                    TotalTest++;
                    Test10++;
                    if (TestList.size() == COUNTER_THRESHHOLD) {

                        long total = 0;
                        for (long i : TestList) {
                            total += i;
                        }
                        total = total / COUNTER_THRESHHOLD;

                        PrintLog("Average: " + total + "/" + COUNTER_THRESHHOLD + " times./" + TotalTest);
                        TestList.clear();
                    }

                    float diff2 = (Calendar.getInstance().getTimeInMillis() - mStartTestTime);
                    if (Test10 != 0 && diff2 != 0)
                        Log.v(TAG, "SPS: " + (Test10 * 1000l / diff2) + " " + Test10 + "/" + diff2);

                    if ((TotalTest % 10) == 0) {
                        Test10 = 0;
                        mStartTestTime = Calendar.getInstance().getTimeInMillis();
                    }

                } else {
                    PrintLog("Test Fail: " + result);
                }

                if (autoTestStarting) mHandler.sendEmptyMessageDelayed(MSG_TEST, DELAY_TIME);
            }
        });

    }


    private void StartCameraAct() {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName("com.innocomm.iscanservice", "com.innocomm.iscanservice.Act_Camera"));
        startActivity(intent);
    }


    private void SyncScan() { //emulate trigger key
        Log.v(TAG, "SyncScan");
        mBCRManager.scan(true);
    }


}
