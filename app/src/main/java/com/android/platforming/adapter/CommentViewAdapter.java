package com.android.platforming.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.platforming.clazz.Comment;
import com.android.platforming.clazz.Post;
import com.android.platforming.clazz.User;
import com.android.platforming.interfaze.ListenerInterface;
import com.example.platforming.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CommentViewAdapter extends RecyclerView.Adapter<CommentViewAdapter.ViewHolder> {


    //게시판 좋아요, 삭제
    //댓글 삭제


    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");
    private ArrayList<Comment> mData = null ;

    ListenerInterface listenerInterface;

    public void setListenerInterface(ListenerInterface listenerInterface) {
        this.listenerInterface = listenerInterface;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView nickname;
        TextView date;
        TextView comment;

        ViewHolder(View itemView) {
            super(itemView);

            // 뷰 객체에 대한 참조. (hold strong reference)
            profile = itemView.findViewById(R.id.iv_noticeboard_comment_profile);
            nickname = itemView.findViewById(R.id.tv_noticeboard_comment_nickname);
            date = itemView.findViewById(R.id.tv_noticeboard_comment_date);
            comment = itemView.findViewById(R.id.tv_noticeboard_comment_comment);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    if (pos != RecyclerView.NO_POSITION) {
                        listenerInterface.onSuccess(pos);
                    }
                }
            });
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public CommentViewAdapter(ArrayList<Comment> list) {
        mData = list ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public CommentViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        View view = inflater.inflate(R.layout.item_noticeboard_comment, parent, false) ;
        CommentViewAdapter.ViewHolder vh = new CommentViewAdapter.ViewHolder(view) ;
        return vh;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.profile.setImageResource(User.getProfiles().get(mData.get(position).getProfileIndex()));
        holder.nickname.setText(mData.get(position).getNickname());
        holder.comment.setText(mData.get(position).getComment());
        holder.date.setText(dateFormat.format(mData.get(position).getDate()));
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }
}
