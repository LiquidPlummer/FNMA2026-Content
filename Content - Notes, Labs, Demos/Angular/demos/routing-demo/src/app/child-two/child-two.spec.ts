import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChildTwo } from './child-two';

describe('ChildTwo', () => {
  let component: ChildTwo;
  let fixture: ComponentFixture<ChildTwo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChildTwo]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ChildTwo);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
