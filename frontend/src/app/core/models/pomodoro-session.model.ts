export type PomodoroSessionMode = 'FOCUS' | 'PAUSE_COURTE' | 'PAUSE_LONGUE';

export type CompletionStatus = 'TERMINEE' | 'INTERROMPUE';

export interface PomodoroSession {
  id: number;
  mode: PomodoroSessionMode;
  taskId: number | null;
  startedAt: string;
  endedAt: string;
  actualDurationSeconds: number;
  completionStatus: CompletionStatus;
  createdAt: string;
}

export interface PomodoroSessionRequest {
  mode: PomodoroSessionMode;
  taskId?: number | null;
  startedAt: string;
  endedAt: string;
  completionStatus: CompletionStatus;
}
