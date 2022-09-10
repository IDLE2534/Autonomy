package com.android.platforming.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.platforming.clazz.Post;
import com.android.platforming.clazz.TableItem;
import com.android.platforming.interfaze.ListenerInterface;
import com.example.platforming.R;

import java.util.ArrayList;

public class ScheduleEditAdapter extends RecyclerView.Adapter<ScheduleEditAdapter.ViewHolder> {

    ArrayList<TableItem> schedules;

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        EditText subject;
        EditText teacher;
        ImageView delete;

        ViewHolder(View itemView) {
            super(itemView);

            // 뷰 객체에 대한 참조. (hold strong reference)
            time = itemView.findViewById(R.id.tv_schedule_edit_time);
            subject = itemView.findViewById(R.id.et_schedule_edit_subject);
            teacher = itemView.findViewById(R.id.et_schedule_edit_teacher);
            delete = itemView.findViewById(R.id.iv_schedule_edit_delete);
        }
    }

    public ScheduleEditAdapter(ArrayList<TableItem> schedules) {
        this.schedules = schedules;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public ScheduleEditAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        View view = inflater.inflate(R.layout.item_recyclerview_post, parent, false) ;

        return new ScheduleEditAdapter.ViewHolder(view);
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(ScheduleEditAdapter.ViewHolder holder, int position) {
        holder.time.setText((position + 1) + "교시");
        holder.subject.setText(schedules.get(position).getMainText());
        holder.teacher.setText(schedules.get(position).getSubText());
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return schedules.size() ;
    }
}