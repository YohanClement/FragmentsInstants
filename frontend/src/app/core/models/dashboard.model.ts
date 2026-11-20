import { Task } from './task.model';

export type NextBreakType = 'COURTE' | 'LONGUE';

export interface Dashboard {
  todaysTasks: Task[];
  completedTaskTodayCount: number;
  cumulativeFocusTimeMinutes: number;
  weightedProgress: number;
  pomodoroDailyCount: number;
  pomodorosRemainingBeforeLongBreak: number;
  weeklyMoodFatigueAverages: WeeklyMoodFatigueAverages;
  nextBreakType: NextBreakType;
}

export interface WeeklyMoodFatigueAverages {
  physicalFatigue: number | null;
  mentalFatigue: number | null;
  mood: number | null;
}
