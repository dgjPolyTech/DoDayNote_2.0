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
        TextView textNotToday;
        TextView textDDay;
        CardView cardView;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_habit_title);
            textDate = itemView.findViewById(R.id.text_habit_date);
            checkDone = itemView.findViewById(R.id.check_habit_done);
            textNotToday = itemView.findViewById(R.id.text_not_today);
            textDDay = itemView.findViewById(R.id.text_d_day);
            cardView = (CardView) itemView;
        }

        public void bind(final Habit habit, final OnHabitClickListener listener) {
            textTitle.setText(habit.getTitle());
            
            String startDate = habit.getStartDate() != null ? habit.getStartDate() : "";
            String endDate = habit.getEndDate() != null ? habit.getEndDate() : "";
            
            String dateStr = "<font color='#70C18E'>" + startDate + "</font> 부터 <font color='#70C18E'>" + endDate + "</font>";
            
            String activeDaysForHtml = habit.getActiveDays();
            if (activeDaysForHtml != null && activeDaysForHtml.length() >= 7 && !activeDaysForHtml.equals("0000000") && !activeDaysForHtml.equals("1111111")) {
                StringBuilder daysSb = new StringBuilder("<br><font color='#000000'>(");
                String[] daysArr = {"월", "화", "수", "목", "금", "토", "일"};
                boolean first = true;
                for (int i = 0; i < 7; i++) {
                    if (activeDaysForHtml.charAt(i) == '1') {
                        if (!first) daysSb.append("/");
                        daysSb.append(daysArr[i]);
                        first = false;
                    }
                }
                daysSb.append(")</font>");
                dateStr += daysSb.toString();
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                textDate.setText(android.text.Html.fromHtml(dateStr, android.text.Html.FROM_HTML_MODE_COMPACT));
            } else {
                textDate.setText(android.text.Html.fromHtml(dateStr));
            }
            
            String dDayStr = "";
            if (endDate.length() >= 10) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    java.util.Date end = sdf.parse(endDate);
                    
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    cal.set(java.util.Calendar.MINUTE, 0);
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    java.util.Date todayDate = cal.getTime();
                    
                    long diff = end.getTime() - todayDate.getTime();
                    long days = diff / (1000 * 60 * 60 * 24);
                    
                    if (days == 0) {
                        dDayStr = "D-Day";
                    } else if (days > 0) {
                        dDayStr = "D-" + days;
                    } else {
                        dDayStr = "D+" + Math.abs(days);
                    }
                } catch (Exception e) {
                }
            }
            textDDay.setText(dDayStr);

            boolean isDoneAny = false;
            // 기기 날짜와 상관없이, 리스트에 있는 기록 중 하나라도 완료된 상태인지 확인
            // (서버가 오늘의 기록만 보낸다고 가정하거나, 가장 최근 기록을 기준으로 판단)
            if (habit.getRecords() != null && !habit.getRecords().isEmpty()) {
                // 가장 마지막 레코드의 상태를 현재 상태로 간주
                isDoneAny = habit.getRecords().get(habit.getRecords().size() - 1).isDone();
            }

            boolean isTodayActive = true;
            String activeDays = habit.getActiveDays();
            if (activeDays != null && activeDays.length() >= 7 && !activeDays.equals("0000000") && !activeDays.equals("1111111")) {
                int dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK);
                int index = (dayOfWeek + 5) % 7;
                isTodayActive = (activeDays.charAt(index) == '1');
            }

            // 완료 상태에 따른 UI 처리
            updateUI(isDoneAny, isTodayActive);

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

        private void updateUI(boolean isDone, boolean isTodayActive) {
            if (!isTodayActive) {
                cardView.setCardBackgroundColor(Color.WHITE);
                checkDone.setVisibility(View.GONE);
                textDDay.setVisibility(View.GONE);
                textNotToday.setVisibility(View.VISIBLE);
                
                textTitle.setPaintFlags(textTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                textTitle.setTextColor(Color.BLACK);
                
                // 텍스트 투명도 조절
                textTitle.setAlpha(0.5f);
                textDate.setAlpha(0.5f);
                textNotToday.setAlpha(1.0f);
            } else if (isDone) {
                cardView.setCardBackgroundColor(Color.parseColor("#F0F0F0"));
                checkDone.setVisibility(View.VISIBLE);
                textDDay.setVisibility(View.VISIBLE);
                textDDay.setTextColor(Color.DKGRAY);
                textNotToday.setVisibility(View.GONE);
                checkDone.setBackgroundResource(R.drawable.shape_checkbox_checked);
                
                textTitle.setPaintFlags(textTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                textTitle.setTextColor(Color.DKGRAY);
                
                // 텍스트 투명도 조절
                textTitle.setAlpha(0.4f);
                textDate.setAlpha(0.4f);
                textDDay.setAlpha(0.4f);
                checkDone.setAlpha(0.4f);
            } else {
                cardView.setCardBackgroundColor(Color.WHITE);
                checkDone.setVisibility(View.VISIBLE);
                textDDay.setVisibility(View.VISIBLE);
                textDDay.setTextColor(Color.parseColor("#70C18E"));
                textNotToday.setVisibility(View.GONE);
                checkDone.setBackgroundResource(R.drawable.shape_checkbox_outline);
                
                textTitle.setPaintFlags(textTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                textTitle.setTextColor(Color.BLACK);
                
                // 텍스트 투명도 원상복구
                textTitle.setAlpha(1.0f);
                textDate.setAlpha(1.0f);
                textDDay.setAlpha(1.0f);
                checkDone.setAlpha(1.0f);
            }
        }
    }
}
