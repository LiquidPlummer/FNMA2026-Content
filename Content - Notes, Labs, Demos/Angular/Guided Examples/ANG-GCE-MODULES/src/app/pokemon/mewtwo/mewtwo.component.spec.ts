import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MewtwoComponent } from './mewtwo.component';

describe('MewtwoComponent', () => {
  let component: MewtwoComponent;
  let fixture: ComponentFixture<MewtwoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MewtwoComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MewtwoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
