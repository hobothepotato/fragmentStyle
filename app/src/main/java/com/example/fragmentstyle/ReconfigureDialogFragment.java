package com.example.fragmentstyle;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.fragmentstyle.Preferences;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReconfigureDialogFragment extends DialogFragment {

    //  Logging
    private final static String TAG = "RECONFIG_DIALOG";

    //  Member variables
    ReconfigureDialogListener mListener;
    EditText ccCurValue;
    EditText ccNewValue;

    //  Resource values used by Dialog to update Preferences
    int res;
    int defaultRes;

    public ReconfigureDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //  Verify that the host activity implements the callback interface
        try {
            //  Instantiate listener so we can send events to the host
            mListener = (ReconfigureDialogListener) context;
        } catch (ClassCastException e) {
            //  Context does not implement callback interface, throw exception
            throw new ClassCastException(TAG + ": " + context.toString() + " must implement ReconfigureDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Get variables
        Bundle args = getArguments();
        res = args.getInt("res");
        defaultRes = args.getInt("defaultRes");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //  Get the layout inflater
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_custom_command, null);

        //  Get values of Preference
        ccCurValue = view.findViewById(R.id.custom_command_value_current);
        ccNewValue = view.findViewById(R.id.custom_command_value_new);

        //  Set current values for viewing
        ccCurValue.setText(Preferences.readPreference(getContext(), res, defaultRes));
        ccCurValue.setKeyListener(null);

        //  Focus cursor on new value
        ccNewValue.setText(Preferences.readPreference(getContext(), res, defaultRes));
        ccNewValue.requestFocus();
        ccNewValue.selectAll();

        //  Inflate and set the layout for the dialog
        builder.setView(view)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //  Save new value to Preferences
                        Preferences.savePreference(getContext(), res, ccNewValue.getText().toString());
                        //  Send notification to parent Activity
                        mListener.onDialogPositiveClick(ReconfigureDialogFragment.this, res);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //  Cancel dialog
                        ReconfigureDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    /**
     * An interface for other classes to implement, allowing them to respond to events
     *  within the Dialog
     */
    public interface ReconfigureDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, int res);
    }

}
