package com.randeepa.cloud;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

public class ReportSaleFragment extends Fragment implements View.OnClickListener {

    Utils utils;

    EditText date_ET;
    Spinner showroom_dealer_SP;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View reportSale = inflater.inflate(R.layout.fragment_report_sale, container, false);

        utils = new Utils();

        date_ET = reportSale.findViewById(R.id.date_ET);
        showroom_dealer_SP = reportSale.findViewById(R.id.showroom_dealer_SP);

        date_ET.setText(utils.getYear() + "-" + utils.getMonth() + "-" + utils.getDay());

        date_ET.setOnClickListener(this);

        return reportSale;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.date_ET:
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month++;
                        date_ET.setText(year + "-" + month + "-" + dayOfMonth);
                    }
                }, utils.getYear(), utils.getMonth() - 1, utils.getDay());
                datePickerDialog.show();
                break;
        }
    }
}
