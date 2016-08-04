package wanghaisheng.com.facemyage;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;

import mehdi.sakout.fancybuttons.FancyButton;
import wanghaisheng.com.facemyage.imagefileselector.ImageCropper;
import wanghaisheng.com.facemyage.imagefileselector.ImageFileSelector;
import wanghaisheng.com.facemyage.mrdialog.MrHUD;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FancyButton mPhotoBtn,mDetectBtn,mCameraBtn;
    private ImageView ivReset,ivAbout;

    //当前选中图片的路径
    private String mCurrentPhotoPath;
    private Paint mPaint;
    private Dialog dialogs;
    private ImageView mImageView;
    private Bitmap mBitmap;
    private MrHUD dialog;

    private static final int MSG_SUCCESS = 0X110;
    private static final int MSG_ERROR = 0X111;

    private ImageFileSelector mImageFileSelector;
    private ImageCropper mImageCropper;
    private File mCurrentSelectFile;

    //添加的年龄数
    private int mAgePP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initImageFileSelector();

        initEvents();
    }

    /**
     * 改变图片的大小，防止内存溢出
     */
    private void resizePhoto() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, options);

        double ratio = Math.max(options.outWidth*1.0d/1024f,options.outHeight*1.0d/1024f);
        options.inSampleSize = (int) Math.ceil(ratio);
        options.inJustDecodeBounds = false;
        mBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,options);
    }

    /**
     * 初始化View
     */
    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(R.string.app_name);

        dialog = new MrHUD(MainActivity.this);

        this.mImageView = (ImageView) findViewById(R.id.id_photo);
        this.mPhotoBtn = (FancyButton) findViewById(R.id.btn_pick_img);
        this.mDetectBtn = (FancyButton) findViewById(R.id.btn_detect);
        this.mCameraBtn = (FancyButton) findViewById(R.id.btn_camera);
        this.ivReset = (ImageView) findViewById(R.id.iv_reset);
        this.ivAbout = (ImageView) findViewById(R.id.iv_about);

        mPaint = new Paint();
    }

    /**
     * 初始化点击事件
     */
    private void initEvents() {
        this.mPhotoBtn.setOnClickListener(this);
        this.mDetectBtn.setOnClickListener(this);
        this.mCameraBtn.setOnClickListener(this);
        this.ivReset.setOnClickListener(this);
        this.ivAbout.setOnClickListener(this);

        ImageUtil.registerDoubleClickListener(this.mImageView, new OnDoubleClickListener() {
            @Override
            public void OnSingleClick(View v) {

            }

            @Override
            public void OnDoubleClick(View v) {
                //双击之后年龄增加10，但增加的年龄不能大于30
                if(mAgePP<=30) {
                    mAgePP += 10;
                }
            }
        });
    }

    private void initImageFileSelector() {
        //初始化图片选择器
        mImageFileSelector = new ImageFileSelector(this);
        mImageFileSelector.setCallback(new ImageFileSelector.Callback() {
            @Override
            public void onSuccess(final String file) {
                if (!TextUtils.isEmpty(file)) {
                    mCurrentSelectFile = new File(file);
                    //选择了文件之后立即裁剪
                    mImageCropper.setOutPut(340, 340);
                    mImageCropper.setOutPutAspect(1, 1);
                    mImageCropper.cropImage(mCurrentSelectFile);
                } else {
                    Toast.makeText(MainActivity.this,"select image file loadError",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this,"select image file loadError",Toast.LENGTH_LONG).show();
            }
        });

        //初始化图片裁剪器
        mImageCropper = new ImageCropper(this);
        mImageCropper.setCallback(new ImageCropper.ImageCropperCallback() {
            @Override
            public void onCropperCallback(ImageCropper.CropperResult result, File srcFile, File outFile) {
                mCurrentSelectFile = outFile;

                if (result == ImageCropper.CropperResult.success) {
                    mBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());
                    mCurrentPhotoPath = outFile.getAbsolutePath();
                    mImageView.setImageBitmap(mBitmap);
                } else if (result == ImageCropper.CropperResult.error_illegal_input_file) {
                    Toast.makeText(MainActivity.this,"input file loadError",Toast.LENGTH_LONG).show();
                } else if (result == ImageCropper.CropperResult.error_illegal_out_file) {
                    Toast.makeText(MainActivity.this,"output file loadError",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImageFileSelector.onActivityResult(requestCode, resultCode, data);
        mImageCropper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_reset:
                //重置之后增加的年龄为0
                mAgePP = 0;
                mImageView.setImageResource(R.drawable.default_1);
                mCurrentPhotoPath = null;
                break;
            case R.id.iv_about:
                startActivity(new Intent(MainActivity.this,AboutActivity.class));
                break;
            case R.id.btn_pick_img:
                //利用系统图片选择器选择图片
                mImageFileSelector.selectImage(MainActivity.this);
                break;
            case R.id.btn_camera:
                mImageFileSelector.takePhoto(MainActivity.this);
                break;
            case R.id.btn_detect:
                //检测网络连接
                if(!NetUtil.checkNetWork(MainActivity.this)) {
                    showError("识别年龄需要联网获取数据，请先打开网络连接");
                    return;
                }

                if(null != mCurrentPhotoPath && !mCurrentPhotoPath.trim().equals("")) {
                    resizePhoto();
                } else {
                    //如果没有选择，则使用默认的图片
                    mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_1);
                }

                showWaitDialog("正在识别中");
                FaceppDetect.detect(mBitmap, new FaceppDetect.Callback() {
                    @Override
                    public void success(JSONObject result) {
                        Message message = Message.obtain();
                        message.what = MSG_SUCCESS;
                        message.obj = result;

                        mHandler.sendMessageDelayed(message, 500);
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        Message message = Message.obtain();
                        message.what = MSG_ERROR;
                        message.obj = exception.getErrorMessage();

                        mHandler.sendMessageDelayed(message,500);
                    }
                });
                break;
        }
    }

    public class DetectHandler extends Handler {
        WeakReference<MainActivity> activityRef;

        public DetectHandler(MainActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SUCCESS:
                    JSONObject result = (JSONObject) msg.obj;
                    prepareRSBitmap(result);
                    mImageView.setImageBitmap(mBitmap);
                    break;
                case MSG_ERROR:
                    String errorMsg = (String) msg.obj;
                    Log.d("err",errorMsg);
                    showError(errorMsg);
                    break;
            }
            hideWaitDialog();
        }
    }

    private Handler mHandler = new DetectHandler(MainActivity.this);

    /**
     * 绘制人脸识别结果的bitmap
     * @param result
     */
    private void prepareRSBitmap(JSONObject result) {

        //Bitmap bitmap = Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(),mBitmap.getConfig());
        Bitmap bitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        try {
            JSONArray faces = result.getJSONArray("face");
            int faceCount = faces.length();
            if(faceCount == 0) {
//                dialog.showErrorMessage("长得太抽象o(╯□╰)o,识别不出来");
                dialogs = new AlertDialog.Builder(this)
                        .setTitle("检测结果")
                        .setMessage("长得太抽象o(╯□╰)o,识别不出来")
                        .setNegativeButton("重来",
                                new DialogInterface.OnClickListener()
                                {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which)
                                    {
                                        dialogs.dismiss();
                                    }
                                }).create();
                dialogs.show();
                return;
            }

            for(int i=0; i<faceCount; i++) {
                JSONObject face = faces.getJSONObject(i);
                JSONObject posObj = face.getJSONObject("position");
                float x = (float) posObj.getJSONObject("center").getDouble("x");
                float y = (float) posObj.getJSONObject("center").getDouble("y");
                float w = (float) posObj.getDouble("width");
                float h = (float) posObj.getDouble("height");

                //将百分比转化为实际的像素值
                x = x/100 * bitmap.getWidth();
                y = y/100 * bitmap.getHeight();
                w = w/100 * bitmap.getWidth();
                h = h/100 * bitmap.getHeight();

                mPaint.setColor(0xffffffff);

                //mPaint.setColor(Color.RED);
                mPaint.setAntiAlias(true);
                mPaint.setStrokeWidth(1);
                mPaint.setStrokeCap(Paint.Cap.ROUND);

                canvas.drawLine(x-w/2,y-h/2,x-w/2,y+h/2,mPaint);
                canvas.drawLine(x-w/2,y-h/2,x+w/2,y-h/2,mPaint);
                canvas.drawLine(x+w/2,y-h/2,x+w/2,y+h/2,mPaint);
                canvas.drawLine(x-w/2,y+h/2,x+w/2,y+h/2,mPaint);

                //get age and gender
                int age = face.getJSONObject("attribute").getJSONObject("age").getInt("value");
                String gender = face.getJSONObject("attribute").getJSONObject("gender").getString("value");

                Bitmap ageBitmap = buildAgeBitmap(age, "Male".equals(gender));

                int ageWidth = ageBitmap.getWidth();
                int ageHeight = ageBitmap.getHeight();
                Log.d("test print","test print......................");
                if(bitmap.getWidth()< mBitmap.getWidth()&&bitmap.getHeight()< mBitmap.getHeight()) {
                    //获取bitmap相对原图的缩放比例
                    float ratio = Math.max(bitmap.getWidth()*1.0f/ mBitmap.getWidth(),bitmap.getHeight()*1.0f/ mBitmap.getHeight());
                    //ratio = ratio*0.8f;
                    Log.d("ratio","create scaledbitmap");
                    ageBitmap = Bitmap.createScaledBitmap(ageBitmap,(int)(ageWidth*ratio),(int)(ageHeight*ratio),false);
                }

                if(ageBitmap.getWidth()>w) {
                    float ratio = w/(ageBitmap.getWidth()*1.0f);
                    ageBitmap = Bitmap.createScaledBitmap(ageBitmap,(int)(ageWidth*ratio),(int)(ageHeight*ratio),false);
                }

                canvas.drawBitmap(ageBitmap,x-ageBitmap.getWidth()/2,y+h/2,null);

                mBitmap = bitmap;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //绘制年龄bitmap
    private Bitmap buildAgeBitmap(int age, boolean isMale) {
        LinearLayout mAgeGenderView = (LinearLayout) getLayoutInflater().inflate(
                R.layout.ageandgender_layout, null);
        TextView genderView = (TextView) mAgeGenderView.findViewById(R.id.tv_gender);
        String gender = isMale?"男":"女";
        genderView.setText(gender);
        TextView ageView = (TextView) mAgeGenderView.findViewById(R.id.tv_age);
        ageView.setText(mAgePP+age + "");

        mAgeGenderView.setDrawingCacheEnabled(true);
        mAgeGenderView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        mAgeGenderView.layout(0, 0, mAgeGenderView.getMeasuredWidth(),
                mAgeGenderView.getMeasuredHeight());
        mAgeGenderView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(mAgeGenderView.getDrawingCache());
        mAgeGenderView.destroyDrawingCache();

        return bitmap;
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mImageFileSelector.onSaveInstanceState(outState);
        mImageCropper.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageFileSelector.onRestoreInstanceState(savedInstanceState);
        mImageCropper.onRestoreInstanceState(savedInstanceState);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mImageFileSelector.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void showWaitDialog() {
        showWaitDialog("加载中。。。");
    }

    public void showWaitDialog(int resid) {
        showWaitDialog(getString(resid));
    }

    public void showWaitDialog(String message) {
        if(dialog == null) {
            dialog = new MrHUD(MainActivity.this);
        }
        dialog.showLoadingMessage(message,true);
    }

    public void hideWaitDialog() {
        dialog.dismiss();
    }

    public void showInfo(String msg) {
        if(dialog == null) {
            dialog = new MrHUD(MainActivity.this);
        }
        dialog.showInfoMessage(msg);
    }

    public void showInfo(int msgRes) {
        showInfo(getString(msgRes));
    }

    public void showError(String msg) {
        if(dialog == null) {
            dialog = new MrHUD(MainActivity.this);
        }

        dialog.showErrorMessage(msg);
    }

    public void showSuccess(String msg) {
        if(dialog == null) {
            dialog = new MrHUD(MainActivity.this);
        }
        dialog.showSuccessMessage(msg);
    }

}
