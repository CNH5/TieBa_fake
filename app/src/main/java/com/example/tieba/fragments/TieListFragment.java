package com.example.tieba.fragments;

import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tieba.BackstageInteractive;
import com.example.tieba.Constants;
import com.example.tieba.R;
import com.example.tieba.adapter.TieAdapter;
import com.example.tieba.beans.Tie;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author sheng
 * @date 2021/9/10 16:39
 */
public class TieListFragment extends Fragment implements View.OnClickListener {
    private List<Tie> tieList = new ArrayList<>();
    private Thread getDataThread;
    private String account;
    private TieAdapter adapter;
    private RecyclerView lvTieList;

    public void setAccount(String account) {
        this.account = account;
    }

    public TieListFragment() {
    }

    public static TieListFragment newInstance() {
        TieListFragment fragment = new TieListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tie_list_fragment, container, false);
        lvTieList = v.findViewById(R.id.tie_list);
        lvTieList.setLayoutManager(new LinearLayoutManager(getActivity()));
        v.findViewById(R.id.tie_sort).setOnClickListener(this);
        refreshData();
        return v;
    }

    //????????????
    private void getData() {
        getDataThread = new Thread(() -> {
            HttpUrl get_tie_list_url = Objects.requireNonNull(HttpUrl.parse(Constants.GET_TIE_LIST_PATH)).newBuilder()
                    .addQueryParameter("account", account)
                    //???????????????????????????????????????1
                    .addQueryParameter("ba", "1")
                    .build();

            Type type_tie_list = new TypeToken<List<Tie>>() {
            }.getType();  //?????????????????????
            try {
                tieList = new Gson().fromJson(
                        BackstageInteractive.get(get_tie_list_url),
                        type_tie_list
                );
            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(getActivity(), "????????????!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        getDataThread.start();
    }

    //??????????????????????????????????????????????????????????????????
    private void waitData() {
        try {
            getDataThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setListAdapter() {
        adapter = new TieAdapter(getActivity(), tieList, account);
        lvTieList.setAdapter(adapter);
    }

    public void refreshData() {
        getData();
        waitData();
        setListAdapter();
    }


    public void changeListItem(int position, Tie tie) {
        adapter.changeItem(position, tie);
    }

    @Override
    public void onClick(View v) {
        final String[] items = {"????????????", "??????????????????", "??????????????????", "??????????????????"};
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setItems(items, (dialog, which) -> Toast.makeText(getActivity(), "????????????!", Toast.LENGTH_SHORT).show())
                .show();
    }
}
