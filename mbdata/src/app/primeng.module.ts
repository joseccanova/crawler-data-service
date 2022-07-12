// ===========================================================================
// File: APP.MODULE-PRIMENG.ts
import { SharedModule, Header, Footer } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { Dialog, DialogModule } from 'primeng/dialog';
import { ConfirmDialog, ConfirmDialogModule } from 'primeng/confirmdialog';
import { DropdownModule } from 'primeng/dropdown';
import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';
import { ListboxModule } from 'primeng/listbox';
import { RadioButtonModule } from 'primeng/radiobutton';
import { PanelModule } from 'primeng/panel';
import { CalendarModule } from 'primeng/calendar';
import { AccordionModule } from 'primeng/accordion';
import { TabViewModule } from 'primeng/tabview';
import { FocusTrapModule } from 'primeng/focustrap';
import { CheckboxModule } from 'primeng/checkbox';
import { TreeTableModule } from 'primeng/treetable';
import { TreeModule } from 'primeng/tree';
import {InputTextModule} from 'primeng/inputtext';
import {InputTextareaModule} from 'primeng/inputtextarea';
import {InputSwitchModule} from 'primeng/inputswitch';
import {InputNumberModule} from 'primeng/inputnumber';
import {MultiSelectModule} from 'primeng/multiselect';
import {MenuModule} from 'primeng/menu';
import {MenuItem} from 'primeng/api';
import {CardModule} from 'primeng/card';
import {DividerModule} from 'primeng/divider';
import {SelectButtonModule} from 'primeng/selectbutton';
import {TreeSelectModule} from 'primeng/treeselect';
import {MessagesModule} from 'primeng/messages';
import {MessageModule} from 'primeng/message';
import {ToastModule} from 'primeng/toast';

//
export const APP_PRIMENG_MODULE = [
	SharedModule,
	TableModule,
	DialogModule,
	ConfirmDialogModule,
	DropdownModule,
	MenubarModule,
	ButtonModule,
	ListboxModule,
	RadioButtonModule,
	PanelModule,
	AccordionModule,
	CalendarModule,
	TabViewModule,
	FocusTrapModule,
	CheckboxModule,
	TreeTableModule,
	TreeModule,
	InputTextModule,
	InputTextareaModule,
	InputSwitchModule,
	InputNumberModule,
	MultiSelectModule,
	MenuModule, 
	CardModule,
	DividerModule,
	SelectButtonModule,
	TreeSelectModule,
	MessagesModule,
	MessageModule,
	ToastModule
];
//
export const APP_PRIMENG_COMPONENTS = [
	Dialog,
	ConfirmDialog,
	Header,
	Footer
];
//
import { MessageService } from 'primeng/api';
//
export const APP_PRIMENG_PROVIDERS = [
	MessageService
];
// ===========================================================================