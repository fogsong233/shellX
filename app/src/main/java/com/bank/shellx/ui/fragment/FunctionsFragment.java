package com.bank.shellx.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.shellx.R;
import com.bank.shellx.ui.adapter.FunctionItemsAdapter;
import com.bank.shellx.utils.ui.fuctionItemsBuilders.StaticItems;

//主要功能页面
public class FunctionsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_functions, container, false);
        RecyclerView rcv = view.findViewById(R.id.functions_recycler_view);
        rcv.setLayoutManager(new LinearLayoutManager(getContext()));
        FunctionItemsAdapter adapter = new FunctionItemsAdapter(getContext(), StaticItems.getStaticItemsData());
        rcv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return view;
    }

}
