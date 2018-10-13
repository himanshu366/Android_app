package com.example.himanshu.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import com.dropbox.core.v2.files.FileMetadata;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

public class MainActivity extends AppCompatActivity implements UploadFileTask.Callback{

    private ProgressBar addImageProgressbar;
    private boolean isDocumentUploaded;
    private String documentImageUrl;
    private String filePath;
    private Integer valRadio;
    private Uri mCropImageUri;
    private ImageChooser mImageChooser;
    static final String ACCESS_TOKEN = "YOxP__Qq29AAAAAAAAAAGijaJhQU2jW_iM_xbaw-Z0PveZhoKMU5vo_T3THYiBDS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Button buttonPost = (Button) findViewById(R.id.buttonPost);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "POST", Toast.LENGTH_SHORT).show();
                new PostClient().execute();
            }
        });
    }


    private class PostClient  extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL("http://192.168.43.77:8080/setItemDetails"); //in the real code, there is an ip and a port
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();
                EditText et1 = (EditText) findViewById(R.id.editText2);
                EditText et2 = (EditText) findViewById(R.id.editText3);
                EditText et3 = (EditText) findViewById(R.id.editText4);
                EditText et4 = (EditText) findViewById(R.id.editText5);
                EditText et5 = (EditText) findViewById(R.id.editText6);
                EditText et6 = (EditText) findViewById(R.id.editText7);

                String name = et1.getText().toString();
                String category = et2.getText().toString();
                String id = et3.getText().toString();
                String amt = et4.getText().toString();
                String tax = et5.getText().toString();
                String taxP = et6.getText().toString();
                int amount = Integer.parseInt(amt);
                int taxAmount = Integer.parseInt(tax);
                float taxPnt = Float.parseFloat(taxP);
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("item_name",name);
                jsonParam.put("item_cat",category);
                jsonParam.put("payment_type",valRadio);
                jsonParam.put("txn_id",id);
                jsonParam.put("amount",amount);
                jsonParam.put("tax",taxAmount);
                jsonParam.put("tax_percentage",taxPnt);
                jsonParam.put("image_url",filePath);
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG" , conn.getResponseMessage());

                conn.disconnect();
            } catch (Exception e) {
                Log.v("error:", String.valueOf(e));
            }
            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater mMenuInflater=getMenuInflater();
        mMenuInflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.ic_action_name) {
            Intent i = new Intent(this,InventoryListActivity.class);
            this.startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onUploadImageButtonClicked(View v){
        if (mImageChooser == null){
            mImageChooser = new ImageChooser(this);
            mImageChooser.cropWidth = 100;
            mImageChooser.cropHeight = 200;
            mImageChooser.fixAspectRatio = false;
        }
        mImageChooser.start();
    }

    @Override
    protected void onActivityResult(int  requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }else if(requestCode == 1) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        switch (requestCode) {
            case ImageChooser.REQUESTCODE_PICKIMAGE:
                mImageChooser.cropImageWithData(data);
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                mCropImageUri = result.getUri();
                DbxRequestConfig config = DbxRequestConfig.newBuilder("himanshu/inventory-app").build();
                DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

                UploadFileTask uploadFileTask = new UploadFileTask(this, client, this);
                uploadFileTask.execute(mCropImageUri);
//                try {
//                    Thread t = new Thread()
//                        InputStream in = new FileInputStream(mCropImageUri.getPath());
//                        FileMetadata metadata = client.files().uploadBuilder("/test.txt")
//                                .uploadAndFinish(in);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (DbxException e) {
//                    e.printStackTrace();
//                }
//                HashMap<String , String > map = new HashMap<String , String >();
//                map.put("base64PhotoContent", Util.encodeImageFileToBase64(mCropImageUri, Bitmap.CompressFormat.PNG, 100));
//                map.put("contentFormat", "png");
//                int width = (int) getResources().getDisplayMetrics().density*100;
//                map.put("contentSize", width+"x"+width);
//                performRequest(CDRetrofit.getSharedBackendAPIInstance(this), "uploadAgentImage", AgentImageUploadRespose.class, new Class[] {HashMap.class}, new Object[] {map}, this, ApiType.APITYPE_AGENT_IMAGE_UPLOAD, 0, null, null);
                break;

        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode){
            case ImageChooser.REQUESTCODE_PICKIMAGE:
                mImageChooser.onRequestPermissionsResult(permissions, grantResults);
                break;
        }

    }

    @Override
    public void onUploadComplete(FileMetadata result) {
        Log.d("success", String.valueOf(result));
        filePath = result.getPathDisplay();
    }

    @Override
    public void onError(Exception e) {
        Log.d("error",e.getMessage());
    }

    public void onRadioButtonClicked(View v) {

        boolean checked = ((RadioButton) v).isChecked();

        switch(v.getId()){
            case R.id.radioButton:
                if(checked)
                valRadio=1;
                break;

            case R.id.radioButton2:
                if(checked)
                valRadio=2;
                break;
            case R.id.radioButton3:
                if(checked)
                valRadio=3;
                    break;

            case R.id.radioButton4:
                if(checked)
                valRadio=4;
                    break;
        }
    }
    }
