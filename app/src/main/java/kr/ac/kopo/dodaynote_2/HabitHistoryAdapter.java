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
            tvRate.setText("달성률 계산 중...");

            kr.ac.kopo.dodaynote_2.network.ApiClient.getApiService()
                .getHabitRecords(habit.getId())
                .enqueue(new retrofit2.Callback<List<HabitRecord>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<HabitRecord>> call, retrofit2.Response<List<HabitRecord>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            habit.setRecords(response.body());
                            int rate = calculateRate(habit);
                            tvRate.setText("달성률 " + rate + "%");
                        } else {
                            tvRate.setText("달성률 0%");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<HabitRecord>> call, Throwable t) {
                        tvRate.setText("달성률 0%");
                    }
                });
        }

        private int calculateRate(Habit habit) {
            if (habit.getRecords() == null || habit.getRecords().isEmpty()) return 0;
            if (habit.getStartDate() == null || habit.getStartDate().isEmpty()) return 0;
            
            try {
                String startStr = habit.getStartDate().replace(".", "-");
                String endStr = habit.getEndDate() != null ? habit.getEndDate().replace(".", "-") : "";
                
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Date startDate = sdf.parse(startStr);
                if (startDate == null) return 0;
                
                java.util.Date endDate = null;
                if (!endStr.isEmpty()) {
                    endDate = sdf.parse(endStr);
                }
                java.util.Date today = new java.util.Date();
                java.util.Date effectiveEnd = (endDate != null && endDate.before(today)) ? endDate : today;
                
                if (startDate.after(today)) return 0;
                
                long totalDays = (effectiveEnd.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000) + 1;
                if (totalDays <= 0) return 0;
                
                long successCount = 0;
                for (HabitRecord record : habit.getRecords()) {
                    if (record.isDone() && record.getRecordDate() != null) {
                        String rDateStr = record.getRecordDate().length() >= 10
                                ? record.getRecordDate().substring(0, 10).replace(".", "-")
                                : record.getRecordDate().replace(".", "-");
                        java.util.Date rDate = sdf.parse(rDateStr);
                        if (rDate != null && !rDate.before(startDate) && !rDate.after(effectiveEnd)) {
                            successCount++;
                        }
                    }
                }
                
                int rate = (int) (((float) successCount / totalDays) * 100);
                return rate > 100 ? 100 : rate;
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
