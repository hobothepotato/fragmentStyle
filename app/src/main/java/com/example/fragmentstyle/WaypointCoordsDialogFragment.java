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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaypointCoordsDialogFragment extends DialogFragment {

    //  Logging
    private static final String TAG = "WP_COORDS_DIALOG";

    //  Waypoint coordinates
    WaypointCoordsDialogListener listener;

    //  Layout
    EditText xText;
    EditText yText;

    public WaypointCoordsDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //  Verify that the host activity implements the callback interface
        try {
            //  Instantiate listener so we can send events to the host
            listener = (WaypointCoordsDialogFragment.WaypointCoordsDialogListener) context;
        } catch (ClassCastException e) {
            //  Context does not implement callback interface, throw exception
            throw new ClassCastException(TAG + ": " + context.toString() + " must implement WaypointCoordsDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //  Get the layout inflater
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_waypoint_coords, null);

        //  Layout items
        xText = view.findViewById(R.id.waypoint_x);
        yText = view.findViewById(R.id.waypoint_y);

        yText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO) {
                    addWaypoint();
                    //  Cancel dialog
                    WaypointCoordsDialogFragment.this.getDialog().dismiss();
                    return true;
                }
                return false;
            }
        });

        //  Create dialog
        builder.setView(view)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                addWaypoint();
                            }
                        }
                )
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //  Cancel dialog
                        WaypointCoordsDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public interface WaypointCoordsDialogListener {
        void onSuccessfullyAddedWaypoint(int x, int y);
    }

    private void addWaypoint() {
        if (xText != null && yText != null) {
            int x;
            int y;

            //  Check for empty values
            try {
                x = Integer.parseInt(xText.getText().toString());
                y = Integer.parseInt(yText.getText().toString());
            } catch (NumberFormatException e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(getContext(), "Waypoint values are not valid!", Toast.LENGTH_SHORT).show();
                return;
            }

            //  Check for invalid values
            if (x < 0 || x > 14 || y < 0 || y > 19) {
                Toast.makeText(getContext(), "Waypoint values are not valid!", Toast.LENGTH_SHORT).show();
            } else {
                //  Place waypoint in Arena
                listener.onSuccessfullyAddedWaypoint(x, y);
            }
        }
    }

}
