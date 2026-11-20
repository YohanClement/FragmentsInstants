import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'dashboard',
    title: 'Tableau de bord',
    loadComponent: () =>
      import('./features/dashboard/dashboard-page/dashboard-page').then((m) => m.DashboardPage),
  },
  {
    path: 'tasks',
    title: 'Liste des tâches',
    loadComponent: () => import('./features/tasks/task-list/task-list').then((m) => m.TaskList),
  },
  {
    path: 'pomodoro',
    title: 'Pomodoro',
    loadComponent: () =>
      import('./features/pomodoro/pomodoro-page/pomodoro-page').then((m) => m.PomodoroPage),
  },
  {
    path: 'tags',
    title: 'Etiquettes',
    loadComponent: () => import('./features/tags/tags-page/tags-page').then((m) => m.TagsPage),
  },
  {
    path: 'mood',
    title: 'Humeurs',
    loadComponent: () =>
      import('./features/mood/mood-entry-page/mood-entry-page').then((m) => m.MoodEntryPage),
  },
  {
    path: 'stats',
    title: 'Statistiques',
    loadComponent: () =>
      import('./features/charts/charts-page/charts-page').then((m) => m.ChartsPage),
  },
  {
    path: 'settings',
    title: 'Paramètres',
    loadComponent: () =>
      import('./features/settings/settings-page/settings-page').then((m) => m.SettingsPage),
  },
  { path: '**', redirectTo: 'tasks' },
];
