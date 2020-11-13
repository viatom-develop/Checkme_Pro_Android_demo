package com.checkme.azur.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.checkme.azur.bluetooth.BTConstant;
import com.checkme.azur.bluetooth.WriteFileListener;
import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.PatientItem;
import com.checkme.azur.tools.PreferenceUtils;
import com.checkme.azur.utils.FileDriver;
import com.checkme.azur.utils.JsonRequest;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.azur.utils.StringUtils;
import com.checkme.newazur.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadActivity extends BaseActivity implements WriteFileListener {

    private Button bnSync;
    private ProgressBar progressBar;
    private ImageButton btnRefresh;
    private ListView userListView;
    private ArrayList<PatientItem> patientList = new ArrayList<PatientItem>();

    private RequestQueue queue;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MSG_PART_FINISHED:
                    int progress = msg.arg1;
                    progressBar.setProgress(progress);
                    break;
                case BTConstant.CMD_WORD_START_WRITE:
                    LogUtils.d("update succesful.");
                    showMsg("Sync patient list successfully");
                    clearAllData();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean isTheSameIntent(int menuTag) {
        return (menuTag == Constant.MSG_BNDOWNLOAD_CLICKED);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        iniUI();
    }




    public void iniUI() {
        reFreshTitle(getResources().getString(R.string.title_download));
        reFreshActionBarBn(false, false);

        queue = Volley.newRequestQueue(this);

        bnSync = (Button) findViewById(R.id.btn_push);
        progressBar = (ProgressBar) findViewById(R.id.push_progress_bar);
        btnRefresh = (ImageButton) findViewById(R.id.btn_refresh_user_list);
        userListView = (ListView) findViewById(R.id.user_list);

        bnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeUserList();
            }
        });
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshUserList();
            }
        });

        getUserList();
    }

    public void writeUserList() {
        progressBar.setVisibility(View.VISIBLE);
        byte[] fileBuf = FileDriver.read(Constant.download_dir, "usr.dat");
        Constant.binder.interfaceWriteFile("usr.dat", fileBuf, BTConstant.CMD_WORD_START_WRITE, 10000, this);
    }

    public void refreshUserList() {
        // ui
        bnSync.setEnabled(false);

        getUserList();
    }

    private void getUserList() {
        String url = "https://cloud.viatomtech.com/test/users";
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json+fhir");

        JsonRequest request = new JsonRequest(url, headers, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                LogUtils.d(response.toString());
                showUserList(response);
                showMsg("download patient list successfully !");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.d(error.toString());
            }
        });

        queue.add(request);
    }

    public void showUserList(JSONObject response) {
        JSONArray patients = response.optJSONArray("users");
        if (patients != null && patients.length() != 0) {
            ArrayList<PatientItem> list = new ArrayList<PatientItem>();
            List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();

            for (int i = 0; i < patients.length(); i++) {
                JSONObject patientJson = patients.optJSONObject(i);
                PatientItem patient = new PatientItem(patientJson);
                list.add(patient);

                Map<String, Object> listItem =  new HashMap<String, Object>();
                listItem.put("name", patient.getName());
                listItem.put("gender", patient.getGender());
                listItem.put("birth", patient.getBirth());
                listItem.put("height", patient.getHeight() + "cm");
                listItem.put("weight", patient.getWeight() + "kg");
                listItems.add(listItem);
            }


            SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems, R.layout.list_patients, new String[] {"name", "gender", "birth", "height", "weight"}, new int[] {R.id.patient_name, R.id.patient_gender, R.id.patient_birth, R.id.patient_height, R.id.patient_weight});

            userListView.setAdapter(simpleAdapter);

            // save to file
            this.patientList = list;
            bnSync.setEnabled(true);
            savePatientList();
        }
    }

    private void savePatientList() {
        byte[] buf = new byte[27 * (1+patientList.size())];
        System.arraycopy(guestByte, 0, buf, 0, 27);

        for (int i =0; i<patientList.size(); i++) {
            byte[] data = patientToByte(patientList.get(i));

            System.arraycopy(data, 0, buf, 27*(i+1), 27);
        }

        FileDriver.write(Constant.download_dir, "usr.dat", buf);
    }

    private byte[] patientToByte(PatientItem item) {
        byte[] patientByte = new byte[27];
        patientByte[0] = (byte) item.getId();

        byte[] name = item.getName().getBytes();
        for (int i = 0; i<16; i++){
            if (i<name.length) {
                patientByte[i+1] = name[i];
            } else {
                patientByte[i+1] = 0x00;
            }
        }
        patientByte[17] = (byte) item.getIco();
        if (item.getGender().equals("male")) {
            patientByte[18] = 0x00;
        }else {
            patientByte[18] = 0x01;
        }

        int year = Integer.valueOf(item.getBirth().substring(0,4));
        int month = Integer.valueOf(item.getBirth().substring(5,7));
        int day = Integer.valueOf(item.getBirth().substring(8));

        patientByte[19] = (byte) (year);
        patientByte[20] = (byte) (year>>8);
        patientByte[21] = (byte) (month);
        patientByte[22] = (byte) (day);

        patientByte[23] = (byte) (item.getWeight() * 10);
        patientByte[24] = (byte) ((item.getWeight() * 10)>>8);

        patientByte[25] = (byte) item.getHeight();
        patientByte[26] = (byte) (item.getHeight() >>8);

        LogUtils.d("patient byte: " + StringUtils.byte2hex(patientByte));
        return patientByte;
    }

    private byte[] guestByte = {0x01, 0x47, 0x75, 0x65, 0x73, 0x74, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, (byte) 0xc7, 0x07, 0x09, 0x14, 0x00, 0x00, 0x00, 0x00};

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void clearAllData() {
        Constant.defaultUser.getSpo2list().clear();
        Constant.defaultUser.getTempList().clear();
        Constant.defaultUser.getSlmList().clear();
        Constant.defaultUser.getEcgList().clear();
        String name = PreferenceUtils.readStrPreferences(getApplicationContext(), "PreDeviceName");
        String mode = PreferenceUtils.readStrPreferences(getApplicationContext(), name+"CKM_MODE");
        if (mode == null || mode.equals("") || mode.equals("MODE_HOME")) {
            LogUtils.d("普通模式");
            Constant.curUser.getPedList().clear();
        }else {
            LogUtils.d("医院模式");
        }
//				Constant.curUser.getPedList().clear();
        Constant.userList = null;
        Constant.spotUserList = null;
    }

    @Override
    public void onWritePartFinished(String fileName, byte fileType, float percentage) {
        MsgUtils.sendMsg(handler, Constant.MSG_PART_FINISHED, (int) (percentage * 100));
    }

    @Override
    public void onWriteSuccess(String fileName, byte fileType) {
        // TODO Auto-generated method stub
        MsgUtils.sendMsg(handler, fileType);
    }

    @Override
    public void onWriteFailed(byte fileType, byte errCode) {
//        Toast.makeText(this, "write failed", Toast.LENGTH_LONG).show();
        LogUtils.d("write failed");
        MsgUtils.sendMsg(handler, fileType);
    }
}
