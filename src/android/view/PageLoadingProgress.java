package org.apache.cordova.inappwebview.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Feng on 2017/9/15.
 */

public class PageLoadingProgress {

    private ProgressDialog progressDialog = null;

    public PageLoadingProgress(){
    }

    public void show(final Activity activity){
        final Context context = activity;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog != null && progressDialog.isShowing()){
                    return;
                }
                close(activity);

                progressDialog = new ProgressDialog(context);
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        progressDialog = null;
                    }
                });

                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);

                RelativeLayout centeredLayout = new RelativeLayout(context);
                centeredLayout.setGravity(Gravity.CENTER);
                centeredLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                ProgressBar progressBar = new ProgressBar(context);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                progressBar.setLayoutParams(layoutParams);
                centeredLayout.addView(progressBar);

                /*LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));

                linearLayout.addView(progressBar);

                TextView textView = new TextView(context);
                textView.setText("loading...");
                textView.setTextColor(Color.GRAY);
                RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                textView.setLayoutParams(layoutParams1);


                linearLayout.addView(textView);
                centeredLayout.addView(linearLayout);*/

                progressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                if(activity.isFinishing()){
                   return;
                }
                progressDialog.show();
                progressDialog.setContentView(centeredLayout);
            }
        });

    }

    public void close(Activity activity){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                progressDialog = null;
            }
        });
    }
}
