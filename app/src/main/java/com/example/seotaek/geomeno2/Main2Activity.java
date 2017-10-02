package com.example.seotaek.geomeno2;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seotaek.geomeno2.common.TitleBitmapButton;
import com.example.seotaek.geomeno2.db.MemoDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Main2Activity extends AppCompatActivity {
    public static final String TAG = "MemoInsertActivity";

    private ImageView mPhoto;
    private EditText mMemoEdit;

    String mMemoId;
    String mMediaPhotoId;
    String mMediaPhotoUri;
    String mMediaVideoId;
    String mMediaVideoUri;
    String mMediaVoiceId;
    String mMediaVoiceUri;
    String mMediaHandwritingId;
    String mMediaHandwritingUri;

    boolean isPhotoFileSaved;
    boolean isPhotoCaptured;
    boolean isVideoRecorded;
    boolean isVoiceRecorded;
    boolean isHandwritingMade;

    Bitmap resultPhotoBitmap;

    TitleBitmapButton insertSaveBtn;
    String mMemoMode;
    String mDateStr;
    String mMemoStr;

    Calendar mCalendar = Calendar.getInstance();
    TitleBitmapButton insertDateButton;

    String tempPhotoUri;

    boolean isPhotoCanceled;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_CAMERA = 3;

    private Uri photoUri;
    private String photoUri2 ="112";

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    private static final int MULTIPLE_PERMISSIONS = 101;

    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        checkPermissions();
        initView();

        mPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isPhotoCaptured || isPhotoFileSaved) {
                    showDialog(BasicInfo.CONTENT_PHOTO_EX);
                } else {
                    showDialog(BasicInfo.CONTENT_PHOTO);
                }
            }
        });


        setBottomButtons();

        setMediaLayout();

        setCalendar();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);//v는 클릭된 뷰를 의미

                getMenuInflater().inflate(R.menu.option_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.m1:
                                Toast.makeText(getApplication(), "메뉴1", Toast.LENGTH_SHORT).show();
                                takePhoto();
                                break;
                            case R.id.m2:
                                Toast.makeText(getApplication(), "메뉴1", Toast.LENGTH_SHORT).show();
                                goToAlbum();
                                break;
                            case R.id.m3:
                                Toast.makeText(getApplication(), "메뉴1", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                popup.show();//Popup Menu 보이기
            }
        });

    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void initView() {
        mPhoto = (ImageView) findViewById(R.id.img_test);
        mMemoEdit = (EditText) findViewById(R.id.insert_memoEdit);
    }


    public void processIntent(Intent intent) {
        mMemoId = intent.getStringExtra(BasicInfo.KEY_MEMO_ID);
        mMemoEdit.setText(intent.getStringExtra(BasicInfo.KEY_MEMO_TEXT));
        mMediaPhotoId = intent.getStringExtra(BasicInfo.KEY_ID_PHOTO);
        photoUri = Uri.parse(intent.getStringExtra(BasicInfo.KEY_URI_PHOTO));
        mMediaVideoId = intent.getStringExtra(BasicInfo.KEY_ID_VIDEO);
        mMediaVideoUri = intent.getStringExtra(BasicInfo.KEY_URI_VIDEO);
        mMediaVoiceId = intent.getStringExtra(BasicInfo.KEY_ID_VOICE);
        mMediaVoiceUri = intent.getStringExtra(BasicInfo.KEY_URI_VOICE);
        mMediaHandwritingId = intent.getStringExtra(BasicInfo.KEY_ID_HANDWRITING);
        mMediaHandwritingUri = intent.getStringExtra(BasicInfo.KEY_URI_HANDWRITING);

        setMediaImage(mMediaPhotoId, photoUri, mMediaVideoId, mMediaVoiceId, mMediaHandwritingId);
    }

    public void setMediaImage(String photoId, Uri photoUri, String videoId, String voiceId, String handwritingId) {

        if (photoId.equals("") || photoId.equals("-1")) {
            mPhoto.setImageResource(R.drawable.person_add);
        } else {
            isPhotoFileSaved = true;
            mPhoto.setImageURI(Uri.parse(BasicInfo.FOLDER_PHOTO + photoUri));
        }

    }

    public void setBottomButtons() {
        insertSaveBtn = (TitleBitmapButton) findViewById(R.id.insert_saveBtn);

        // 저장 버튼
        insertSaveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //  boolean isParsed = parseValues();
                // if (isParsed)
                // {
                // if(mMemoMode.equals(BasicInfo.MODE_INSERT)) {
                saveInput();
                //  } else if(mMemoMode.equals(BasicInfo.MODE_MODIFY) || mMemoMode.equals(BasicInfo.MODE_VIEW)) {
                //      modifyInput();
                // }
                // }
            }
        });

    }

    /**
     * 데이터베이스에 레코드 추가
     */
    private void saveInput() {

        String insertDateStr = insertDateButton.getText().toString();
        try {
            Date insertDate = BasicInfo.dateDayNameFormat.parse(insertDateStr);
            mDateStr = BasicInfo.dateDayFormat.format(insertDate);
        } catch (ParseException ex) {
            Log.e(TAG, "Exception in parsing date : " + insertDateStr);
        }

        String memotxt = mMemoEdit.getText().toString();
        mMemoStr = memotxt;

        String photoFilename = insertPhoto();
        int photoId = -1;

        String SQL = null;

        if (photoFilename != null) {
            // query picture id
            SQL = "select _ID from " + MemoDatabase.TABLE_PHOTO + " where URI = '" + photoFilename + "'";

            if (MainActivity.mDatabase != null) {
                Cursor cursor = MainActivity.mDatabase.rawQuery(SQL);
                if (cursor.moveToNext()) {
                    photoId = cursor.getInt(0);
                }
                cursor.close();
            }
        }

        SQL = "insert into " + MemoDatabase.TABLE_MEMO +
                "(INPUT_DATE, CONTENT_TEXT, ID_PHOTO, ID_VIDEO, ID_VOICE, ID_HANDWRITING) values(" +
                "DATETIME('" + mDateStr + "'), " +
                "'" + mMemoStr + "', " +
                "'" + photoId + "', " +
                "'" + "" + "', " +
                "'" + "" + "', " +
                "'" + "" + "')";

        if (MainActivity.mDatabase != null) {
            MainActivity.mDatabase.execSQL(SQL);
        }

        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();

    }

    /**
     * 앨범의 사진을 사진 폴더에 복사한 후, PICTURE 테이블에 사진 정보 추가
     * 이미지의 이름은 현재 시간을 기준으로 한 getTime() 값의 문자열 사용
     *
     * @return 새로 추가된 이미지의 이름
     */

    private String insertPhoto() {
        String photoName = null;

        if (isPhotoCaptured) { // captured Bitmap

            // Temporary Hash for photo file name
            photoName = photoUri2;

            if (photoName != null) {
                // INSERT PICTURE INFO
                String SQL = "insert into " + MemoDatabase.TABLE_PHOTO + "(URI) values(" + "'" + photoName + "')";
                if (MainActivity.mDatabase != null) {
                    MainActivity.mDatabase.execSQL(SQL);
                }
            }
        }
        return photoName;
    }

    public void setMediaLayout() {
        isPhotoCaptured = false;
        isVideoRecorded = false;
        isVoiceRecorded = false;
        isHandwritingMade = false;

        //mVideoBtn = (TitleBitmapButton)findViewById(R.id.insert_videoBtn);
        // mVoiceBtn = (TitleBitmapButton)findViewById(R.id.insert_voiceBtn);

    }

    private void setCalendar() {
        insertDateButton = (TitleBitmapButton) findViewById(R.id.insert_dateBtn);
        insertDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String mDateStr = insertDateButton.getText().toString();
                Calendar calendar = Calendar.getInstance();
                Date date = new Date();
                try {
                    date = BasicInfo.dateDayNameFormat.parse(mDateStr);
                } catch (Exception ex) {
                    Log.d(TAG, "Exception in parsing date : " + date);
                }

                calendar.setTime(date);

                new DatePickerDialog(
                        Main2Activity.this,
                        dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                ).show();

            }
        });

        Date curDate = new Date();
        mCalendar.setTime(curDate);

        int year = mCalendar.get(Calendar.YEAR);
        int monthOfYear = mCalendar.get(Calendar.MONTH);
        int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);

        insertDateButton.setText(year + "년 " + (monthOfYear + 1) + "월 " + dayOfMonth + "일");

    }


    /**
     * 날짜 설정 리스너
     */
    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mCalendar.set(year, monthOfYear, dayOfMonth);
            insertDateButton.setText(year + "년 " + (monthOfYear + 1) + "월 " + dayOfMonth + "일");
        }
    };


    /**
     * 일자와 메모 확인
     */
    private boolean parseValues() {
        String insertDateStr = insertDateButton.getText().toString();
        try {
            Date insertDate = BasicInfo.dateDayNameFormat.parse(insertDateStr);
            mDateStr = BasicInfo.dateDayFormat.format(insertDate);
        } catch (ParseException ex) {
            Log.e(TAG, "Exception in parsing date : " + insertDateStr);
        }

        String memotxt = mMemoEdit.getText().toString();
        mMemoStr = memotxt;

        if (mMemoStr.trim().length() < 1) {
            showDialog(BasicInfo.CONFIRM_TEXT_INPUT);
            return false;
        }

        return true;
    }


    //정수사진 ㅎㅎ
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(Main2Activity.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(Main2Activity.this,
                    "com.example.seotaek.geomeno2.provider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new java.text.SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "nostest_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/NOSTest/");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }

    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == PICK_FROM_ALBUM) {
            if (data == null) {
                return;
            }
            photoUri = data.getData();
            cropImage();
        } else if (requestCode == PICK_FROM_CAMERA) {
            cropImage();
            // 갤러리에 나타나게
            MediaScannerConnection.scanFile(Main2Activity.this,
                    new String[]{photoUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        } else if (requestCode == CROP_FROM_CAMERA) {
            mPhoto.setImageURI(null);
            mPhoto.setImageURI(photoUri);
            photoUri2 = String.valueOf(photoUri);
        }
    }

    //Android N crop image
    public void cropImage() {

        isPhotoCaptured = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.grantUriPermission("com.android.camera", photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            File croppedFileName = null;
            try {
                croppedFileName = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + "/NOSTest/");
            File tempFile = new File(folder.toString(), croppedFileName.getName());

            photoUri = FileProvider.getUriForFile(Main2Activity.this,
                    "com.example.seotaek.geomeno2.provider", tempFile);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                grantUriPermission(res.activityInfo.packageName, photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, CROP_FROM_CAMERA);
        }
    }
}
