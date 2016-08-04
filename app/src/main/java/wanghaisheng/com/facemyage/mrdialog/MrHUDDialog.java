package wanghaisheng.com.facemyage.mrdialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import wanghaisheng.com.facemyage.R;

class MrHUDDialog extends Dialog {
	private Context context;
	public MrHUDDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
	}
	
	public static MrHUDDialog createDialog(Context context) {
		MrHUDDialog dialog = new MrHUDDialog(context, R.style.MrHUDDialog);
		dialog.setContentView(R.layout.mr_dialog_layout);
		dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		return dialog;
	}

	public void setMessage(String message) {
		TextView msgView = (TextView)findViewById(R.id.simplehud_message);
		msgView.setText(message);
	}
	
	public void setImage(int resId) {
		ImageView image = (ImageView)findViewById(R.id.simplehud_image);
		image.setImageResource(resId);
		
		if(resId==R.drawable.dialog_loading) {
			Animation anim = AnimationUtils.loadAnimation(context, R.anim.mr_hud_progressbar);
			anim.start();
			image.startAnimation(anim);
		}
	}
	

}
