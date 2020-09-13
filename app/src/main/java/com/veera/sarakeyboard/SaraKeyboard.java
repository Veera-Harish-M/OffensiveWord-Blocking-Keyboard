package com.veera.sarakeyboard;
        import android.Manifest;
        import android.app.Application;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.SharedPreferences;
        import android.content.pm.PackageManager;
        import android.graphics.PixelFormat;
        import android.inputmethodservice.InputMethodService;
        import android.inputmethodservice.Keyboard;
        import android.inputmethodservice.KeyboardView;
        import android.media.AudioManager;
        import android.net.Uri;
        import android.telephony.SmsManager;
        import android.util.Log;
        import android.view.Gravity;
        import android.view.KeyEvent;
        import android.view.LayoutInflater;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.Window;
        import android.view.WindowManager;
        import android.view.inputmethod.InputConnection;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.Toast;

        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;

        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.Random;

public class SaraKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    String words[]={"hello","wow"};
    private KeyboardView kv;
    private Keyboard keyboard;
    boolean locked=false;
    String result="";
    int key;
    SharedPreferences sharedPreferences;
    String s1;
    String Captured="";
    private  boolean isCaps = false;

    @Override
    public View onCreateInputView() {

        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard,null);
        keyboard = new Keyboard(this,R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);


        sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        s1 = sharedPreferences.getString("phone", "");


        if(s1.isEmpty()){
            final WindowManager win_details = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

            final WindowManager.LayoutParams params_details = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,0,
                    PixelFormat.TRANSPARENT);

            params_details.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params_details.width = WindowManager.LayoutParams.MATCH_PARENT;
            params_details.format = PixelFormat.TRANSLUCENT;
            params_details.x=0;
            params_details.y=100;
            params_details.gravity = Gravity.TOP;





            LayoutInflater inflaters = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View mDialogView_details = inflaters.inflate(R.layout.details_dialog, null);
            win_details.addView(mDialogView_details, params_details);

           Button Go=mDialogView_details.findViewById(R.id.go);
           final EditText phone=mDialogView_details.findViewById(R.id.phno);

           Go.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if(phone.getText().toString().length()==10){
                       s1=phone.getText().toString();
                       result="";
                       sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                       SharedPreferences.Editor myEdit = sharedPreferences.edit();
                       myEdit.putString("phone",phone.getText().toString());
                       myEdit.commit();
                       win_details.removeViewImmediate(mDialogView_details);



                   }else {
                       phone.requestFocus();
                       phone.setError("Enter Valid Phone Number");
                   }
               }
           });
            mDialogView_details.setOnTouchListener(new View.OnTouchListener() {
                WindowManager.LayoutParams updatedParameters = params_details;
                double x;
                double y;
                double pressedX;
                double pressedY;


                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            x = updatedParameters.x;
                            y = updatedParameters.y;

                            pressedX = event.getRawX();
                            pressedY = event.getRawY();

                            break;

                        case MotionEvent.ACTION_MOVE:
                            updatedParameters.x = (int) (x + (event.getRawX() - pressedX));
                            updatedParameters.y = (int) (y + (event.getRawY() - pressedY));

                            win_details.updateViewLayout(mDialogView_details, updatedParameters);

                        default:
                            break;
                    }

                    return false;
                }
            });


        }

        return kv;

    }

    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }

    @Override
    public void onKey(int i, int[] ints) {


        InputConnection ic = getCurrentInputConnection();
        playClick(i);
        if(locked!=true) {
            switch (i) {
                case Keyboard.KEYCODE_DELETE:
                    ic.deleteSurroundingText(1, 0);
                    if (result.length() != 0) {
                        result = result.substring(0, result.length() - 1);
                        Log.i("hellooooooo", result);
                    }
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    isCaps = !isCaps;
                    keyboard.setShifted(isCaps);
                    kv.invalidateAllKeys();
                    break;
                case Keyboard.KEYCODE_DONE:
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    result = result + "\n";
                    String res = result.replace("\n", "");
                    String typed_words[] = res.split(" ");

                    int kodi=0;
                    for (i = 0; i < typed_words.length; i++) {
                        if (Arrays.asList(words).contains(typed_words[i])) {
                            Log.i("helloooo", "offensive word");
                            locked=true;
                            kodi=1;
                            key= new Random().nextInt(900000) + 100000;
                            String content=" Your Child has Used Offensive word "+typed_words[i]+" in Keyboard.Your Security Lock Key is " + Integer.toString(key);
                            SmsManager smsManager = SmsManager.getDefault();
                            ArrayList<String> msgArray = smsManager.divideMessage(content);
                            smsManager.sendMultipartTextMessage(s1, null,msgArray, null, null);
                            Toast.makeText(getApplicationContext(),"Your Keyboard has been locked due to offensive Word Usage",Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                    if(kodi==0)
                        result="";
                    break;
                default:
                    char code = (char) i;
                    if (Character.isLetter(code) && isCaps)
                        code = Character.toUpperCase(code);
                    ic.commitText(String.valueOf(code), 1);
                    Log.i("heo",s1);
                    if (String.valueOf(code).equals(" ")) {
                        result = result + " ";

                        String resl = result.replace("\n", "");
                        String typed_word[] = resl.split(" ");

                            if (Arrays.asList(words).contains(typed_word[typed_word.length-1])) {
                                Log.i("helloooo", "offensive word");
                                locked=true;
                                Log.i("he",s1);
                                key= new Random().nextInt(900000) + 100000;
                                String content=" Your Child has Used Offensive word "+typed_word[typed_word.length-1]+ " in Keyboard.Your Security Lock Key is " + Integer.toString(key);
                                SmsManager smsManager = SmsManager.getDefault();

                                smsManager.sendTextMessage(s1, null,content, null, null);
                               /* getContentResolver().delete(Uri.parse("content://sms/outbox"), "address = ? and body = ?", new String[] {s1,content});
                                getContentResolver().delete(Uri.parse("content://sms/sent"), "address = ? and body = ?", new String[] {s1,content});
                                */
                               Toast.makeText(getApplicationContext(),"Your Keyboard has been locked due to offensive Word Usage",Toast.LENGTH_LONG).show();
                                break;
                            }

                        Log.i("helloo", result);

                    } else {
                        result = result + String.valueOf(code);
                    }
            }
        }else{
            Log.i("helloooo","here");

            Toast.makeText(getApplicationContext(),"Your Keyboard has been locked due to offensive Word Usage...Check your parent's Messages for Security Code",Toast.LENGTH_LONG).show();

            final WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, 0,
                    PixelFormat.TRANSPARENT);

            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.x=0;
            params.y=100;
            params.gravity = Gravity.TOP;





            LayoutInflater inflaters = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View mDialogView = inflaters.inflate(R.layout.dialog, null);
            locked=false;
            wm.addView(mDialogView, params);

            final EditText dialog_security =mDialogView.findViewById(R.id.security);
            final Button dialog_proceed=mDialogView.findViewById(R.id.proceed);

            dialog_proceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(),"Keyboard Unlocked",Toast.LENGTH_LONG).show();

                    if(dialog_security.getText().toString().equals(Integer.toString(key))){
                        locked=false;
                        result="";
                        wm.removeViewImmediate(mDialogView);
                        Toast.makeText(getApplicationContext(),"Keyboard Unlocked",Toast.LENGTH_LONG).show();
                    }
                    else{
                        dialog_security.requestFocus();
                        dialog_security.setError("Invalid Security Key");
                        Toast.makeText(getApplicationContext(),"Invalid Security Key",Toast.LENGTH_LONG).show();

                    }
                }
            });

            mDialogView.setOnTouchListener(new View.OnTouchListener() {
                WindowManager.LayoutParams updatedParameters = params;
                double x;
                double y;
                double pressedX;
                double pressedY;


                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            x = updatedParameters.x;
                            y = updatedParameters.y;

                            pressedX = event.getRawX();
                            pressedY = event.getRawY();

                            break;

                        case MotionEvent.ACTION_MOVE:
                            updatedParameters.x = (int) (x + (event.getRawX() - pressedX));
                            updatedParameters.y = (int) (y + (event.getRawY() - pressedY));

                            wm.updateViewLayout(mDialogView, updatedParameters);

                        default:
                            break;
                    }

                    return false;
                }
            });



            ImageView cancel=mDialogView.findViewById(R.id.btn);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    wm.removeViewImmediate(mDialogView);
                    locked=true;
                }
            });




/*
            builder.setMessage("Your Keyboard has been locked On Offensive Word Usage");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String m_Text = input.getText().toString();
                    if (Integer.toString(key).equals(m_Text)){
                        locked=false;
                        Toast.makeText(getApplicationContext(),"Security Key Mached",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Security key Does'nt Match",Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

            Toast.makeText(getApplicationContext(),"",Toast.LENGTH_LONG).show();*/
        }

    }

    private void playClick(int i) {

        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(i)
        {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}