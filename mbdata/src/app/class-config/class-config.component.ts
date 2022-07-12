import { Component, OnInit , ViewChild , AfterViewInit , Inject } from '@angular/core';
import { RestService } from '../rest.service';
import { Observable , fromEvent , map , merge ,  of as observableOf } from 'rxjs'
import {MatPaginator , MatPaginatorDefaultOptions , MatPaginatorIntl } from '@angular/material/paginator';
import {MatSort, SortDirection} from '@angular/material/sort';
import {catchError, startWith, switchMap} from 'rxjs/operators';
import {MatTableDataSource , MatTable} from '@angular/material/table';
import { SelectionModel  } from '@angular/cdk/collections';
import {MatDialog, MatDialogRef , MatDialogConfig , MAT_DIALOG_DATA} from '@angular/material/dialog';
import { ComponentType } from '@angular/cdk/portal';

@Component({
  selector: 'app-class-config',
  templateUrl: './class-config.component.html',
  styleUrls: ['./class-config.component.css']
})
export class ClassConfigComponent implements OnInit , AfterViewInit {

	resultsLength = 0;
	  isLoadingResults = true;
	  isRateLimitReached = false;
  protected classes : any;
  displayedColumns: string[] = ['key'];
  dataSource:MatTableDataSource<any>;
  mappings:Mapping[];
  metaclassColumns: string[] = ['name'];
  metaclass: MatTableDataSource<any>;
  currentMapping?:any;
  initialSelection = [];
  allowMultiSelect = true;
  selection?:any;
  selectElements = new Map<any,any>();
  
  @ViewChild(MatPaginator) paginator: MatPaginator;
  
  constructor(public dialog: MatDialog , protected restService : RestService ) { 
	  this.classes = [];
	  this.mappings = [];
	  this.selection = new SelectionModel<any>(this.allowMultiSelect, this.initialSelection);
  }
  
  
  ngOnInit(): void {
	  this.dataSource  = new  MatTableDataSource(this.processRequest());  }
  
  public processRequest():any{
	  this.requestClasses();
	  return this.dataSource;
  }
  
  public openDialog(enterAnimationDuration: string, exitAnimationDuration: string , element?:any): void {
	  this.processMetaClass(element.key , element.value , enterAnimationDuration , exitAnimationDuration);
  }
  
  ngAfterViewInit() {
	  this.dataSource.paginator = this.paginator;
  }
  public requestClasses(){
	  this.restService.getClasses() .subscribe(
		        (response) => {                           //next() callback
		        	this.processClasses(response);
		          },
		          (error) => {                              //error() callback
		            console.error('Request failed with error ' + JSON.stringify(error))},
		          () => {                                   //complete() callback
		          });
  }
  
  public toggleAllRows(event?:any , element?:any):any{
	  console.log('the event '  + JSON.stringify(element));	
	  console.log('sleection' + JSON.stringify(event.checked));
	  if(event.checked)
	  	this.selectElements.set(element.key , element.value);
	  else {
		  	console.log('deleting');
		  	console.log ('removed ' + this.selectElements.delete(element.key));
	  }
	  this.processSimpleSearch(element);
	  let t = {};
	  return element?element:t;	
  }
  
  
  public processSimpleSearch(element?:any){
	  
	  
	  
  }
  
  public isAllSelected():boolean{
	  return false;
  }
  
  public processMetaClass(key : any , value:any,enterAnimationDuration?: string, exitAnimationDuration?: string):any{
	  let mapping :any;
	  let store = <any>[];
	  let data:any;
	  console.log('the key ' + key + ' the value ' + value);
	  return this.restService.getMappings()
			   	.pipe (map((x)=>x))
			  	.subscribe(
			  		  mappings =>{
			  		  let map = this.processMappings(mappings, key , value);
			  		  this.processResultMapping(map);
			  		  this.processSample(map,enterAnimationDuration,exitAnimationDuration);
			  		  store.push(map);
			  		return map || {};
			  	}
			  );
  }
  
  public processResultMapping(mapping:any){
	  this.restService.proccessMetaClassUrl(mapping.metaclassUrl).subscribe(
			  	metaclass =>{this.processMetaClassResult(metaclass , mapping)}
			  );

  }
  
  public processMetaClassResult	(metaclass:any , mapping?:any){
	  let attributes = <any>[];
	  console.log('metaclass' + JSON.stringify(metaclass.metaAttributes));
	  metaclass.metaAttributes.
	  forEach((a:any) => {
		 let att = new Attribute (a.columnName , a.isId , a.length);
		 attributes.push(att);
	  });
	  this.metaclass = new MatTableDataSource(attributes);
	  this.currentMapping = new MappingAttribute(mapping,attributes);
	  let searchUrl = mapping.searchUrl;
	  let sampleEntity = mapping.sample;
	  let sort = {
	  				start :  0,
	  				pageSize:  100};
	  const container = new SearchContainer(sort , sampleEntity);
	  this.restService.proccessSearch(searchUrl,container)
	  .subscribe(r => {
		  this.processEntity(r,mapping);
	  });
  }
  
  
  public processEntity(r?:any,mapping?:any){
	  r && mapping?mapping.setSample(r):console.log('nothing to sample');
  }
  
  public getCurrentMapping(){
	  return this.currentMapping;
  }
  
  public getMetaclass(){
	  return this.metaclass;
  }
  
  public processMappings(mappings:any , key:any , value:any):any{
	  let map = null;
	  let servlets = mappings.contexts['mb-data-service'].mappings.dispatcherServlets.dispatcherServlet;
	  servlets.forEach((servlet:any) => {
		  let conditions = servlet.details?.requestMappingConditions;
		  if (conditions){
		  let patary = conditions?.patterns;
			  patary?.forEach((p:any)=> {
				  if(p.includes("/"+key.toLowerCase()+"/")){
					  if (p.includes("metaclass")){
						  let sampleUrl = "/"+key.toLowerCase()+"/exampleValue";
						  let searchUrl = "/"+key.toLowerCase()+"/search";
						  map = new Mapping(p , searchUrl , sampleUrl , key , value);
						  this.processSample(map);
						  if(!this.mappings.includes(map))
						  		this.mappings.push(map);
						  		console.log('metaclass ' + JSON.stringify(map));
					  }
				  }
			  });		  
		  }
	  });
	  return map;
  }
  
  public processSample(map:Mapping,enterAnimationDuration?: string, exitAnimationDuration?: string):any{
	  return this.restService.processSampleUrl(map.sampleUrl)
	  .subscribe((sample:any)=> { 
		  map.setSample(sample);
		  if(enterAnimationDuration && exitAnimationDuration){
		  		this.showDialog(sample , enterAnimationDuration , exitAnimationDuration);
		  }
	  });
  }
  
  public showDialog (sample?:any,enterAnimationDuration?:string,exitAnimationDuration?:string){
	  let dialog =  this.dialog?.open(DialogAnimationsExampleDialog, {
	      width: '400px',
	      height: '300px',
	      enterAnimationDuration,
	      exitAnimationDuration,
	      data: {
		        sample: sample,
			}
	    });
		console.log(dialog);
  }
  
  public setClasses(classes2: any) {
	this.classes = classes2;
  }
  
  public getClasses():any{
	  return this.classes;
  }
  
  public processClasses(response  :any){
	 // console.log('the response' + JSON.stringify(response));
      try {
     let i = 0;
     for(var key in response){
    	 console.log(key + ' ' + response[key] );
    	 this.classes.push ({key: key,
    		 				 value : response[key]});
     }
      }catch(e){console.log('an error');}
      this.dataSource = new MatTableDataSource<any>(this.classes);
	  this.dataSource.paginator = this.paginator;
  }
  
}

export class SearchContainer {
	
	 sortParameters:any;
	 entity?:any;
	
	
	constructor(sort:any, entity2?:any){
		this.sortParameters=sort;
		this.entity=entity2;
	}
	
}

export class Mapping {
	
	metaclassUrl:any; 
	key:any; 
	value:any;
	sampleUrl?:string;
	searchUrl?:string;
	sample?:any;
	
	constructor(metaclassUrl2?:string,searchUrl2?:string,sampleUrl2?:string,key2?:string,value2?:string){
		this.metaclassUrl = metaclassUrl2;
		this.key = key2;
		this.value=value2;
		this.sampleUrl = sampleUrl2;
		this.searchUrl= searchUrl2;
	}
	
	public setSample(s:any){
		let value = JSON.stringify(s);
		this.sample=s;
	}
	
	public getSample(){
		return this.sample;
	}
	
}

export class Attribute {

	name?:any;
	isId?:any;
	length?:any;
	
	constructor(name2?:string,isId2?:any,length2?:any){
		this.name=name2;
		this.isId=isId2;
		this.length=length2;
	}
	
}

export class MappingAttribute{
	
	mapping?:any;
	attributes?:[];
	
	constructor (map?:any,atts?:any){
		this.mapping=map; 
		this.attributes=atts;
	}
}

@Component({
	  selector: 'dialog-entity',
	  templateUrl: 'dialog-entity.html',
	})
	export class DialogAnimationsExampleDialog implements OnInit{
	   
	  instance?:any;
	  fields?:Array<any>;
	  dataSource?:MatTableDataSource<any>;
	  displayedColumns?: string[] = ['key' , 'value'];
	  
	  constructor(@Inject(MAT_DIALOG_DATA) public data: any , public dialogRef: MatDialogRef<DialogAnimationsExampleDialog> ) {
		  if(data)
			  this.instance=data;
	  }
	  
	  ngOnInit(): void {
		  console.log('THE ON INIT OF THE DIALOG');
		  if(this.instance){
			  this.proccessInstanceFields();
		  }
	}
	  public getDataSource?(){
		  return this.dataSource;
	  }
	  
	  
	  public proccessInstanceFields(){
		  if(this.instance){
				let sample = this.instance?.sample;	
				this.fields=new Array<any>();
				for (var key in sample){
						let val = sample[key];
						this.fields.push({key : key , value: val});
				}
				this.dataSource = new MatTableDataSource<any>(this.fields);
		  }
	  }
	  
}