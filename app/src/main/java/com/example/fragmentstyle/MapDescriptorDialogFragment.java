package com.example.fragmentstyle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import com.example.fragmentstyle.R;
import com.example.fragmentstyle.ReconfigureDialogFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapDescriptorDialogFragment extends DialogFragment {

    //  Logging
    private static final String TAG = "MAP_DESC_DIALOG";

    //  Map Descriptors
    String currP1MapDescriptor;
    String currP2MapDescriptor;
    String currImageDescriptor;

    public MapDescriptorDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Get current map descriptor values
        Bundle args = getArguments();
        currP1MapDescriptor = args.getString("p1");
        currP2MapDescriptor = args.getString("p2");
        currImageDescriptor = args.getString("img");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //  Get the layout inflater
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_map_descriptor, null);

        //  Layout items
        TextView p1MapDescriptorText = view.findViewById(R.id.arena_p1_descriptor);
        TextView p2MapDescriptorText = view.findViewById(R.id.arena_p2_descriptor);
        TextView imageDescriptorText = view.findViewById(R.id.image_descriptor);

        //  Setting values of Layout items
        if (currP1MapDescriptor.trim().length() != 0) {
            p1MapDescriptorText.setText(currP1MapDescriptor);
        } else {
            p1MapDescriptorText.setText(R.string.arena_descriptors_none);
        }

        if (currP2MapDescriptor.trim().length() != 0) {
            p2MapDescriptorText.setText(currP2MapDescriptor);
        } else {
            p2MapDescriptorText.setText(R.string.arena_descriptors_none);
        }

        if (currImageDescriptor.trim().length() != 0) {
            imageDescriptorText.setText(currImageDescriptor);
        } else {
            imageDescriptorText.setText(R.string.arena_descriptors_none);
        }

        builder.setView(view)
                .setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //  Cancel dialog
                        MapDescriptorDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}
