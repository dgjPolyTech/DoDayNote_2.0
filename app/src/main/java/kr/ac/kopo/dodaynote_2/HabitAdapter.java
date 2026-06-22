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

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habitList = new ArrayList<>();
    private OnHabitClickListener listener;

    public interface OnHabitClickListener {
        void onHabitClick(Habit habit);
        void onCheckClick(Habit habit, View checkView, CardView cardView);
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
            // 날짜 데이터가 Habit 도메인에 아직 없는 것 같아 하드코딩 유지하거나 빈 값 처리
            textDate.setText("습관 진행 중"); 

            // 완료 상태에 따른 UI 처리
            updateUI(habit.isDone());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onHabitClick(habit);
                }
            });

            checkDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onCheckClick(habit, checkDone, cardView);
                }
            });
        }

        private void updateUI(boolean isDone) {
            if (isDone) {
                cardView.setAlpha(0.5f);
                cardView.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
                checkDone.setBackgroundResource(R.drawable.shape_checkbox_checked);
            } else {
                cardView.setAlpha(1.0f);
                cardView.setCardBackgroundColor(Color.WHITE);
                checkDone.setBackgroundResource(R.drawable.shape_checkbox_outline);
            }
        }
    }
}
