package com.oracle.bits.parasjos.sdpdassignment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

public class SlateActivity extends AppCompatActivity {
    public static final String activityKey  = "Slate for Kids";
    SlateCanvas slate ;
    int currentColour= Color.RED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slate);
        slate = new SlateCanvas(this,currentColour);
        setContentView(slate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.getItem(0);
        item.setVisible(true);
        item.setEnabled(true);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                slate = new SlateCanvas(getBaseContext(),currentColour);
                setContentView(slate);
                return false;
            }
        });
        //Add the colour chooser option
        menu.add(0, 2, 0, getString(R.string.choose_colour));

        item = menu.getItem(2);
        item.setIcon(R.drawable.ic_dark_colour_chooser);
        item.setVisible(true);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showDialog();
                return false;
            }
        });

        return true;
    }

    private void showDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.choose_colour));
        RadioGroup radioGrp = getRadioButtonGroup();
        radioGrp.setId(3*100);
        System.out.println("RdioGrp id is "+radioGrp.getId());
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int radioBtnID = radioGroup.getCheckedRadioButtonId();
                RadioButton radioB = radioGroup.findViewById(radioBtnID);
                System.out.println("User chose in final: '"+radioB.getText()+"'.");
                currentColour = Color.RED;
                if(radioB.getText().toString().equalsIgnoreCase(getString(R.string.colour_blue))){
                    currentColour = Color.BLUE;
                } else if(radioB.getText().toString().equalsIgnoreCase(getString(R.string.colour_green))){
                    currentColour = Color.GREEN;
                }
                System.out.println("User chose final colour: "+currentColour);
                slate.setNewColour(currentColour);
            }
        });
        alert.setView(radioGrp);
        alert.setCancelable(false);

        alert.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        System.out.println("Showing alert");
        alert.show();
        System.out.println("After show alert");

    }

    public RadioGroup getRadioButtonGroup(){
        RadioGroup radioGrp = new RadioGroup(this);
        RadioButton radButton = new RadioButton(this);
        radButton.setId((2)+1);
        radButton.setText(R.string.colour_red);
        if(currentColour==Color.RED) {
            radButton.setSelected(true);
            radButton.setChecked(true);
        }
        radioGrp.addView(radButton);

        radButton = new RadioButton(this);
        radButton.setId((2)+2);
        radButton.setText(R.string.colour_blue);
        if(currentColour==Color.BLUE) {
            radButton.setSelected(true);
            radButton.setChecked(true);
        }
        radioGrp.addView(radButton);

        radButton = new RadioButton(this);
        radButton.setId((2)+3);
        radButton.setText(R.string.colour_green);
        if(currentColour==Color.GREEN) {
            radButton.setSelected(true);
            radButton.setChecked(true);
        }
        radioGrp.addView(radButton);

        return radioGrp;
    }

    public void goToHome(MenuItem item) {
        finish();
    }

    public class SlateCanvas extends View {

        private List<Paint> mPaintList = new ArrayList<>();
        private List<Path> mPathList = new ArrayList<>();
        private Paint mPaint;
        private Path mPath;
        private int currentColour = Color.RED;

        public SlateCanvas(Context context,int colour) {
            super(context);
            currentColour = colour;
            mPaint = new Paint();
            mPaint.setColor(currentColour);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(10);
            mPath = new Path();
            mPaintList.add(mPaint);
            mPathList.add(mPath);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            for (int i = 0; i<mPaintList.size();i++){
                canvas.drawPath(mPathList.get(i), mPaintList.get(i));
            }
        }

        protected void setNewColour(int colour){
            currentColour = colour;
            mPaint = new Paint();
            mPaint.setColor(currentColour);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(10);
            mPath = new Path();
            mPaintList.add(mPaint);
            mPathList.add(mPath);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    mPath.moveTo(event.getX(), event.getY());
                    break;

                case MotionEvent.ACTION_MOVE:
                    mPath.lineTo(event.getX(), event.getY());
                    invalidate();
                    break;
            }
            return true;
        }
    }

    private void saveSlateArt(){


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
