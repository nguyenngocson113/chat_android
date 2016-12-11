package com.example.lang_tu.chatchu;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private String outputFile = null;

    Button btnDangky, btnChat;
    ImageButton btnGhiam, btnxong, btnGui;
    EditText edtUsername;
    ListView lvUsername, lvChat;


    ArrayList<String> mangUsernames;
    ArrayList<String> mangChat;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.1.112:3000");
        } catch (URISyntaxException e) {}

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSocket.connect();
        mSocket.on("ketquadangkuun", onNewMessage_DangkyUn);
        mSocket.on("server-gui-username", onNewMessage_DanhsachUn);
        mSocket.on("server-gui-tinchat", onNewMessage_DanhsachTinchat);
        mSocket.on("server-gui-amthanh", onNewMessage_guiamthanh);

        mangChat = new ArrayList<String>();


        edtUsername = (EditText)findViewById(R.id.editTextUsername);
            btnDangky = (Button)findViewById(R.id.buttonDangky);
            btnDangky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.emit("client-gui-username", edtUsername.getText().toString());
            }
        });

        btnChat = (Button)findViewById(R.id.buttonchat);
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.emit("client-gui-tinchat", edtUsername.getText().toString());



            }
        });

        btnGui = (ImageButton)findViewById(R.id.imageButtonGui);
        btnGui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tim thay file huyvu.3gpp
                String path =  outputFile = Environment.getExternalStorageDirectory().
                        getAbsolutePath() + "/huyvu.3gpp";

                byte[] amthanh = FileLocal_To_Byte(path);

                mSocket.emit("client-gui-amthanh", amthanh);


            }
        });

        btnGhiam = (ImageButton)findViewById(R.id.imageButtonGhiam);
        btnGhiam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(v);

            }
        });

        btnxong = (ImageButton)findViewById(R.id.imageButtonXong);
        btnxong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop(v);

            }
        });




    }

    private Emitter.Listener onNewMessage_DanhsachTinchat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String noidung;

                    try {
                        noidung = data.getString("tinchat");

                        lvChat = (ListView)findViewById(R.id.listViewchat);
                        mangChat.add(noidung);
                        ArrayAdapter adapter3 = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1,mangChat );
                        lvChat.setAdapter(adapter3);

                    } catch (JSONException e) {
                        return;
                    }


                }
            });
        }
    };


    private Emitter.Listener onNewMessage_DanhsachUn = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    JSONArray noidung;

                    try {
                        noidung = data.getJSONArray("danhsach");
                        lvUsername = (ListView)findViewById(R.id.listViewUsername);
                        mangUsernames = new ArrayList<String>();
                        for (int i=0; i <noidung.length();i++)
                        {
                            mangUsernames.add(noidung.get(i).toString());
                        }

                        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1, mangUsernames);
                        lvUsername.setAdapter(adapter);


                        Toast.makeText(getApplicationContext(),noidung.length() + "", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        return;
                    }


                }
            });
        }
    };



    private Emitter.Listener onNewMessage_DangkyUn = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   JSONObject data = (JSONObject) args[0];
                   String noidung;

                   try {
                       noidung = data.getString("noidung");
                       if (noidung == "true") {
                           Toast.makeText(getApplicationContext(),"Dang ky thanh cong", Toast.LENGTH_SHORT).show();
                       } else {
                           Toast.makeText(getApplicationContext(), "Da ton tai", Toast.LENGTH_SHORT).show();
                       }
                   } catch (JSONException e) {
                       return;
                   }


               }
           });
        }
    };

    public void start(View view){
        try {

            outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/huyvu.3gpp";
            myRecorder = new MediaRecorder();
            myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            myRecorder.setOutputFile(outputFile);

            myRecorder.prepare();
            myRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Start recording...",
                Toast.LENGTH_SHORT).show();
    }

    public void stop(View view){
        try {
            myRecorder.stop();
            myRecorder.release();
            myRecorder  = null;

            Toast.makeText(getApplicationContext(), "Stop recording...",
                    Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public byte[] FileLocal_To_Byte(String path){
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }


    private Emitter.Listener onNewMessage_guiamthanh = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    byte[] amthanh;

                    try {
                        amthanh = (byte[]) data.get("noidung");
                        playMp3FromByte(amthanh);

                    } catch (JSONException e) {
                        return;
                    }

                }
            });
        }
    };

    private void playMp3FromByte(byte[] mp3SoundByteArray) {
        try {

            File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            MediaPlayer mediaPlayer = new MediaPlayer();

            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }

}
