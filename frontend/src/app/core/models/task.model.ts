import { Tag } from './tag.model';

export type TaskStatus = 'ACTIVE' | 'EN_PAUSE' | 'TERMINEE';

export type TaskPriority = 'BASSE' | 'NORMALE' | 'HAUTE' | 'CRITIQUE';

export interface Task {
  id: number;
  title: string;
  description: string | null;
  estimatedMinutes: number | null;
  priority: TaskPriority;
  dueDate: string | null;
  status: TaskStatus;
  completedAt: string | null;
  createdAt: string;
  updatedAt: string;
  tags: Tag[];
  realTimeMinutes: number;
  remainingTimeMinutes: number;
}

export interface TaskRequest {
  title: string;
  description?: string | null;
  estimatedMinutes?: number | null;
  priority?: TaskPriority;
  dueDate?: string | null;
  tagIds?: number[];
}
