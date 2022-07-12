import { Component, OnInit , AfterViewInit } from '@angular/core';
import { RestService } from '../services/rest.service';
import { pipe , map } from 'rxjs';
import { SearchContainer , Identity } from 'src/app/class-model/class-model';


@Component({
  selector: 'app-crawler-classes',
  templateUrl: './crawler-classes.component.html',
  styleUrls: ['./crawler-classes.component.css']
})
export class CrawlerClassesComponent implements OnInit {

  classes :  IClass[];
	
  first = 0;
  rows = 10;
  
  position="top";
  
  sortField = "IClass.classALias";
  
  selectedClass : IMetaClass;
  
  toggle : boolean;
  
  autoResize: true;
  
  public getFirst(){
	  return this.first;
  }
  
  public getRows(){
	  return this.rows;
  }
  
  constructor(protected restService : RestService) { }

  ngOnInit(): void {
	  this.prepare();
  }
  
  ngAfterViewInit(){
	  this.prepareClasses ();
  }
  
  prepare(){
	  this.classes = new Array<IClass>();
  }
  
  public prepareClasses (){
	  this
	  	.restService
	  	.getClasses()
	  	.pipe (map (x => x))
	  	.subscribe({
	  					next: response => this.processClasses(response)
	  			}
	  	);
  }

  public onPage(e:any){
	  this.first = e.first;
	  this.rows = e.rows;
	  console.log('the event ' + JSON.stringify(e));
  }
  
  public processClasses(response : any) {
	  try {
		     let i = 0;
		     for(var key in response){
		    	 let clazz = { classAlias: key , className : response[key]};
		    	 this.classes.push (clazz);
		     }
		      }catch(e){console.log('an error');}
  }
  
  
  public next() {
      this.first = this.first + this.rows;
  }

  public prev() {
      this.first = this.first - this.rows;
  }

  public reset() {
      this.first = 0;
  }

  public totalRecords(){
	  return this.classes ? this.classes.length : 0;
  }
  
  isLastPage(): boolean {
      return this.classes ? this.first === (this.classes.length - this.rows): true;
  }

  isFirstPage(): boolean {
      return this.classes ? this.first === 0 : true;
  }
  
  public processClassAlias(classAlias:string , className:string){
	  this
	  	.restService
	  	.proccessMetaClassUrl(classAlias.toLowerCase() + "/metaclass")
	  	.subscribe({
	  		next: next => this.processMetaClass(classAlias , className, next)
	  	});
	  
  }
  
  protected processMetaClass(classAlias:string ,className:string, next:any){
	  if (this.selectedClass && this.selectedClass.classAlias == classAlias){
	 		this.toggle = this.toggle?false:true;
	  }else if (this.selectedClass == <IMetaClass>{}) {
		  this.toggle = true;
	  }else { 
		  this.toggle = false;
	  }
	 if (!this.toggle){
	  let attributes = <Array<any>>next.metaAttributes;
	  let selectedAttr = new Array<IAttribute>();
	  attributes
	  .forEach(a =>{
		  let metaAttribute= <any>{
			  clazz : a.clazz,
			  columnName : a.columnName,
			  fieldName: this.snakeToCamel(  a.fieldName),
			  isId: a.isId,
			  length: a.length,
			  sqlType: a.sqlType,
			  required: a.required
		  };
		  selectedAttr.push (metaAttribute);		  
	  });
	  let identity = <Identity>next.identity;
	  this.selectedClass = <IMetaClass>{
		  classAlias: classAlias,
		  className: className,
		  metaAttributes: selectedAttr,
		  identity: identity
	  };
	  this.processPks(this.selectedClass); 
	 }else {
		 this.selectedClass = <IMetaClass>{};
	 }
  }
  
  public processPks(clazz:IMetaClass){
	  let cols = clazz.identity?.columns;	  
	  clazz.metaAttributes
	  .forEach(at => {
		  if(cols)
			  cols
			  .forEach(c =>{
				  let name = this.snakeToCamel(c.name);
				  if(name == at.fieldName){
					  at.isId = true;
					  console.log('found a pk');
				  }
			  });
	  });
  }
  
  display = false;
  alias:string; 
  
  public showDialog(alias:string){
	  this.alias=alias;
	  let url = "/"+ alias.toLowerCase() + "/exampleValue" ;
	  this.restService.processSampleUrl(url)
	  .subscribe ({
		  next: next => this.processSampleDialogResponse(next)
	  });
  }
  
  public processaExemplo(alias:string){
	  this.alias=alias;
	  let url = "/"+ alias.toLowerCase() + "/exampleValue" ;
	  this.restService.processSampleUrl(url)
	  .subscribe ({
		  next: next => this.geraExemploSearch(next)
	  });
  }
  
  public geraExemploSearch(data:any){
	  let searchParameter = {
			  entity:data, 
			  sortParameters:{
				  start:0,
				  pageSize:1
			  } 	  
	  }
	  this.exampleValue = JSON.stringify(searchParameter,null,'\t');
  }
  
  public exampleValue:string;
  
  public getExampleValue():string{
	  return this.exampleValue;	
  }
  
  public processSampleDialogResponse(data:any){
	  this.display = true;
	  this.exampleValue = JSON.stringify(data , null , '\t');
  }
  
  public clearExample(){
	  this.exampleValue="";
  }
  
  public processaBusca(alias:string){
	  let url = "/" + alias.toLowerCase() + "/search";
	  let param = <any>JSON.parse(this.exampleValue);
	  this.restService
	  	.proccessSearch(url , param)
	  	.subscribe(
	  	{
	  		next: next=> this.processaSearchResult(next)
	  	});
  }
  
  resultadoBusca:string;
  
  public processaSearchResult(data:any){
	  this.resultadoBusca=JSON.stringify(data , null,'\t');
	  console.log(data);
  }
  
  public snakeToCamel(str:string):string
	{
		str = str.toLowerCase();
//		 Capitalize first letter of string
		// Run a loop till string
		// string contains underscore
		let i=0;
		while (str.match("([_])")) {
		    let value =	<any> str.match("(_[a-z])") || [];
		    if (value.length>0){
					str = str
							.replace(
									value[0],
									str.charAt(
												str.indexOf("_") + 1).toUpperCase());
					if (str.match("/_[0-9]+/g")) {
						let subsequence = <Array<string>>str.match("_[0-9]+");
						if (subsequence && subsequence.length > 0){ 
							str = str
								.replace(
										subsequence[0],subsequence[0].substring(1));
						}
					}
		    }
			if (i > 5) 
				break;
			
			i++;
		}
//		// Return string
		return str;
	}
  
}



export interface IAttribute{
	clazz:string; 
	columnName:string;
	fieldName:string;
	isId: boolean;
	length: number;
	sqlType: string, 
	required: boolean
}

export interface IMetaClass extends IClass{
	metaAttributes : Array<IAttribute>;
	identity?: Identity;
}

export interface IClass {
	classAlias:string; 
	className: string;
}


export interface ISearchContainer {
	entity:any;
	sortParameters:Map<string,any>;
}
