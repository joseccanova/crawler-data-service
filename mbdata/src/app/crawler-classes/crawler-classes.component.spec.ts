import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrawlerClassesComponent } from './crawler-classes.component';

describe('CrawlerClassesComponent', () => {
  let component: CrawlerClassesComponent;
  let fixture: ComponentFixture<CrawlerClassesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CrawlerClassesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CrawlerClassesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
