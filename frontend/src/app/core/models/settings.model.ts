export interface Settings {
  focusDurationMinutes: number;
  shortBreakDurationMinutes: number;
  longBreakDurationMinutes: number;
  pomodorosBeforeLongBreak: number;
  priorityCoefficientLow: number;
  priorityCoefficientNormal: number;
  priorityCoefficientHigh: number;
  priorityCoefficientCritical: number;
  soundNotificationsEnabled: boolean;
}

export interface SettingsRequest {
  focusDurationMinutes?: number;
  shortBreakDurationMinutes?: number;
  longBreakDurationMinutes?: number;
  pomodorosBeforeLongBreak?: number;
  priorityCoefficientLow?: number;
  priorityCoefficientNormal?: number;
  priorityCoefficientHigh?: number;
  priorityCoefficientCritical?: number;
  soundNotificationsEnabled?: boolean;
}
