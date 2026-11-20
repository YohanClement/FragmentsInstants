import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MoodEntryPage } from './mood-entry-page';

describe('MoodEntryPage', () => {
  let component: MoodEntryPage;
  let fixture: ComponentFixture<MoodEntryPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MoodEntryPage],
    }).compileComponents();

    fixture = TestBed.createComponent(MoodEntryPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
