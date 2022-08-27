package com.todo.machen.listHelper;

import android.annotation.SuppressLint;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.todo.machen.MainActivity;
import com.todo.machen.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddBottomSheetFragment extends BottomSheetDialogFragment {

    protected static String date;
    protected static String time;
    protected static String title;
    protected static String desc;

    @SuppressLint("SimpleDateFormat") private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public View dialog;

    public static AddBottomSheetFragment newInstance(){
        return new AddBottomSheetFragment();
    }

    public static Map<String, Object> getTask(){
        Map<String, Object> TASK = new HashMap<>();
        TASK.put("title", title);
        TASK.put("description", desc);
        TASK.put("date", date);
        TASK.put("time", time);
        return TASK;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saveInstanceState){
        this.dialog = inflater.inflate(R.layout.dialog_fragment, container,
                false);

        TextInputLayout taskTitle = dialog.findViewById(R.id.taskTitle);
        attachEditTextListener(taskTitle.getEditText(), "Title");
        TextInputLayout taskDesc = dialog.findViewById(R.id.taskDesc);
        attachEditTextListener(taskDesc.getEditText(), "Description");

        date = getTodayDate();
        time = getTimeNow();

        TextView addDate = dialog.findViewById(R.id.addDate);
        attachTextViewListener(addDate, "Date");
        TextView addTime = dialog.findViewById(R.id.addTime);
        attachTextViewListener(addTime, "Time");
        ((TextView)dialog.findViewById(R.id.Date)).setText(date);

        dialog.findViewById(R.id.dialogDismiss).setOnClickListener(view -> {
            //Map<String, Object> Task = getTask();
            //MainActivity.data.add(new setData(Task.get("title").toString(), Task.get("description").toString(), null));
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Log.d("UI thread", "ListUpdate\nTitle: " + title + "Description: " + desc);
                    MainActivity.data.add(new setData(title, desc, "https://images.unsplash.com/photo-1632137975828-c8a32b35565a?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80"));
                    MainActivity.adapter.notifyDataSetChanged();
                    addToFirebase();
                    AddBottomSheetFragment.super.dismiss();
                }
            });
        });

        return dialog;
    }

    private void addToFirebase(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(MainActivity.sharedPref.getString(getString(R.string.email_saved),"null"))
                .set(getTask())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("BSF", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("BSF", "Error writing document", e);
                    }
                });}

    private String getTimeNow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return (hour + ":" + minute + ":" + second);
    }

    private String getTodayDate(){
        return dateFormat.format(System.currentTimeMillis());
    }

    private void getDate(){
        new Thread(() -> {
            MaterialDatePicker<Long> dpd = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pick Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            dpd.show(getFragmentManager(), "Date Picker Dialog");
            dpd.addOnPositiveButtonClickListener(selection -> {
                date =  dateFormat.format(selection);
                Log.i("Date Picked: ", date);
                ((TextView) dialog.findViewById(R.id.Date)).setText(date);
            });
        }).start();
    }

    private void getTime(){
        new Thread(() -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            MaterialTimePicker mtp = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(hour)
                    .setMinute(minute)
                    .setTitleText("Pick the Schedule Time")
                    .build();
            mtp.show(getFragmentManager(), "Time Picker Dialog");

            mtp.addOnPositiveButtonClickListener(view -> {
                time = mtp.getHour()+":"+mtp.getMinute()+":00";
                Log.i("Time Picked: ", time);
                ((TextView) dialog.findViewById(R.id.Time)).setText(time);
            });
        }).start();
    }

    private void attachTextViewListener(TextView textView, String param) {
        if (param.equalsIgnoreCase("date")) {
            textView.setOnClickListener(view -> {
                getDate();
            });
        } else {
            textView.setOnClickListener(view -> {
                getTime();
            });
        }
    }

    private void attachEditTextListener(EditText editText, String field){
        if(field.equalsIgnoreCase("title"))
            editText.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {}

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {}

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    //get the String from CharSequence with s.toString() and process it to validation
                    title = s.toString();
                }
            });
        else if(field.equalsIgnoreCase("description"))
            editText.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {}

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {}

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    //get the String from CharSequence with s.toString() and process it to validation
                    desc = s.toString();
                }
            });
    }
}
