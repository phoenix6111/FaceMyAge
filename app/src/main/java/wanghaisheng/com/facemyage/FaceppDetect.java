package wanghaisheng.com.facemyage;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by sheng on 2016/1/3.
 */
public class FaceppDetect {
    public interface Callback {
        void success(JSONObject result);
        void error(FaceppParseException exception);
    }

    public static void detect(final Bitmap bitmap ,final Callback callback) {
        //另外开启一个线程做请求操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpRequests requests = new HttpRequests(Constant.APP_KEY,Constant.APP_SECRET,true,true);
                    Bitmap bmSmall = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bmSmall.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    byte[] arrays = outputStream.toByteArray();

                    PostParameters params = new PostParameters();
                    params.setImg(arrays);

                    JSONObject results = requests.detectionDetect(params);

                    Log.e("Face Age",results.toString());

                    if(null != callback) {
                        callback.success(results);
                    }

                }catch (FaceppParseException e) {
                    e.printStackTrace();
                    if(null != callback) {
                        callback.error(e);
                        Log.e("JSONObject", e.toString());
                    }
                }
            }
        }).start();
    }

    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }
}
