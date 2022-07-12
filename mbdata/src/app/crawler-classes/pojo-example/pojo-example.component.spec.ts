import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PojoExampleComponent } from './pojo-example.component';

describe('PojoExampleComponent', () => {
  let component: PojoExampleComponent;
  let fixture: ComponentFixture<PojoExampleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PojoExampleComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PojoExampleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
