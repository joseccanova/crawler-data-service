import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';

import { HttpClientModule } from '@angular/common/http';

import { ReactiveFormsModule } from '@angular/forms';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import {MatTableModule} from '@angular/material/table';

import {MatPaginatorModule} from '@angular/material/paginator';

import {MatGridListModule} from '@angular/material/grid-list';

import {ScrollingModule} from '@angular/cdk/scrolling';

import {MatSelectModule} from '@angular/material/select';

import {MatCheckboxModule} from '@angular/material/checkbox';

import {MatDialogModule} from '@angular/material/dialog';

import { APP_PRIMENG_MODULE , APP_PRIMENG_PROVIDERS} from "src/app/primeng.module";

import { CrawlerClassesComponent } from './crawler-classes/crawler-classes.component';
import { PojoExampleComponent } from './crawler-classes/pojo-example/pojo-example.component'
import {FormsModule} from '@angular/forms';
import { ModelGraphComponent } from './model-graph/model-graph.component'

import { GojsAngularModule } from 'gojs-angular';


@NgModule({
  declarations: [
    AppComponent,
    CrawlerClassesComponent,
    PojoExampleComponent,
    ModelGraphComponent],
  imports: [
    BrowserModule,
    HttpClientModule,
    ReactiveFormsModule,
    BrowserAnimationsModule, 
    MatTableModule,
    MatPaginatorModule,
    MatGridListModule,
    ScrollingModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDialogModule,
    APP_PRIMENG_MODULE,
    FormsModule,
    GojsAngularModule
  ],
  providers: [APP_PRIMENG_PROVIDERS],
  bootstrap: [AppComponent]
})
export class AppModule { }
