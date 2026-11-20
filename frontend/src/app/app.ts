import { Component, signal } from '@angular/core';
import { AppShell } from './shared/components/app-shell/app-shell';

@Component({
  imports: [AppShell],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('FragmentsInstants');
}
