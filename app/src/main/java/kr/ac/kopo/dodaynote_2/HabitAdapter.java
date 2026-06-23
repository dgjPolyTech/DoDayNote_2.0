package kr.ac.kopo.dodaynote_2;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.ac.kopo.dodaynote_2.domain.Habit;
import kr.ac.kopo.dodaynote_2.domain.HabitRecord;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habitList = new ArrayList<>();
    private OnHabitClickListener listener;

    public interface OnHabitClickListener {
        void onHabitClick(Habit habit);
        void onCheckClick(Habit habit, int position);
    }

    public HabitAdapter(OnHabitClickListener listener) {
        this.listener = listener;
    }

    public void setHabits(List<Habit> habits) {
        this.habitList = habits;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit_card, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        holder.bind(habit, listener);
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        TextView textDate;
        View checkDone;
        CardView cardView;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_habit_title);
            textDate = itemView.findViewById(R.id.text_habit_date);
            checkDone = itemView.findViewById(R.id.check_habit_done);
            cardView = (CardView) itemView;
        }

        public void bind(final Habit habit, final OnHabitClickListener listener) {
            textTitle.setText(habit.getTitle());
            textDate.setText("습관 진행 중"); 

            boolean isDoneAny = false;
            // 기기 날짜와 상관없이, 리스트에 있는 기록 중 하나라도 완료된 상태인지 확인
            // (서버가 오늘의 기록만 보낸다고 가정하거나, 가장 최근 기록을 기준으로 판단)
            if (habit.getRecords() != null && !habit.getRecords().isEmpty()) {
                // 가장 마지막 레코드의 상태를 현재 상태로 간주
                isDoneAny = habit.getRecords().get(habit.getRecords().size() - 1).isDone();
            }

            // 완료 상태에 따른 UI 처리
            updateUI(isDoneAny);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onHabitClick(habit);
                }
            });

            checkDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onCheckClick(habit, getAdapterPosition());
                }
            });
        }

        private void updateUI(boolean isDone) {
            if (isDone) {
                cardView.setAlpha(0.4f);
                cardView.setCardBackgroundColor(Color.parseColor("#D3D3D3"));
                checkDone.setBackgroundResource(R.drawable.shape_checkbox_checked);
                textTitle.setPaintFlags(textTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                textTitle.setTextColor(Color.DKGRAY);
            } else {
                cardView.setAlpha(1.0f);
                cardView.setCardBackgroundColor(Color.WHITE);
                checkDone.setBackgroundResource(R.drawable.shape_checkbox_outline);
                textTitle.setPaintFlags(textTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                textTitle.setTextColor(Color.BLACK);
            }
        }
    }
}
