package kr.ac.kopo.dodaynote_2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.ac.kopo.dodaynote_2.domain.Habit;
import kr.ac.kopo.dodaynote_2.domain.HabitRecord;

public class HabitHistoryAdapter extends RecyclerView.Adapter<HabitHistoryAdapter.ViewHolder> {

    private List<Habit> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Habit habit);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Habit> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit_card_complete, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = items.get(position);
        holder.bind(habit);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvRate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.text_habit_title);
            tvDate = itemView.findViewById(R.id.text_habit_date);
            tvRate = itemView.findViewById(R.id.text_achievement_rate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(items.get(position));
                }
            });
        }

        public void bind(Habit habit) {
            tvTitle.setText(habit.getTitle());
            String dateRange = (habit.getStartDate() != null ? habit.getStartDate() : "") + " ~ " +
                               (habit.getEndDate() != null ? habit.getEndDate() : "");
            tvDate.setText(dateRange);

            // Calculate simple achievement rate
            int rate = calculateRate(habit);
            tvRate.setText("달성률 " + rate + "%");
        }

        private int calculateRate(Habit habit) {
            if (habit.getRecords() == null || habit.getRecords().isEmpty()) return 0;
            long successCount = 0;
            for (HabitRecord record : habit.getRecords()) {
                if (record.isDone()) successCount++;
            }
            int rate = (int) ((double) successCount / habit.getRecords().size() * 100);
            return rate;
        }
    }
}
