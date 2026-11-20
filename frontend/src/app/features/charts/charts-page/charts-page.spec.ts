import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChartsPage } from './charts-page';

describe('ChartsPage', () => {
  let component: ChartsPage;
  let fixture: ComponentFixture<ChartsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChartsPage],
    }).compileComponents();

    fixture = TestBed.createComponent(ChartsPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
