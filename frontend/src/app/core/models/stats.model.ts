export type StatsPeriod = 'today' | 'week';

export interface TagShare {
  tagId: number;
  tagName: string;
  durationMinutes: number;
  percentage: number;
}

export interface WeeklyFocus {
  date: string;
  focusTimeMinutes: number;
}

export interface MoodFatigueTrend {
  mood: Measure[];
  physicalFatigue: Measure[];
  mentalFatigue: Measure[];
}

export interface Measure {
  date: string;
  value: number;
}

export interface DailyCompletedTask {
  date: string;
  count: number;
}
