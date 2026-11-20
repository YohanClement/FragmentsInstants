import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PomodoroPage } from './pomodoro-page';

describe('PomodoroPage', () => {
  let component: PomodoroPage;
  let fixture: ComponentFixture<PomodoroPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PomodoroPage],
    }).compileComponents();

    fixture = TestBed.createComponent(PomodoroPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
