import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChildOne } from './child-one';

describe('ChildOne', () => {
  let component: ChildOne;
  let fixture: ComponentFixture<ChildOne>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChildOne]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ChildOne);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
