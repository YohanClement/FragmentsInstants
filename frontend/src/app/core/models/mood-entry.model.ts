export interface MoodEntry {
  id: number;
  date: string;
  moodScore: number;
  physicalFatigueScore: number;
  mentalFatigueScore: number;
  updatedAt: string;
}

export interface TodayMoodResponse {
  moodEntry: MoodEntry | null;
  extremesExplanation: string;
}

export interface MoodEntryRequest {
  moodScore: number;
  physicalFatigueScore: number;
  mentalFatigueScore: number;
}
