import { Component, OnInit , AfterViewInit, ElementRef , ChangeDetectorRef,  ViewChild, ViewEncapsulation } from '@angular/core';
import * as go from 'gojs';
import { DataSyncService, DiagramComponent, PaletteComponent } from 'gojs-angular';
import produce from "immer";
import { RestService } from 'src/app/rest.service'
import { ISearchContainer , IClass , IMetaClass , IAttribute , Identity} from 'src/app/class-model/class-model';
import { map } from 'rxjs'
import {MessageService} from 'primeng/api';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-model-graph',
  templateUrl: './model-graph.component.html',
  styleUrls: ['./model-graph.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ModelGraphComponent implements OnInit {

  classes :  IClass[];
  edges: Edge[];
  vertexes: Vertex[];
  model: Model;
  links: Link[];
  diagram : go.Diagram;
  @ViewChild('myDiagramDiv') input: ElementRef<HTMLDivElement>;
  selectedClass : IMetaClass;
  
  constructor(protected restService:RestService , private messageService: MessageService) { }

  ngOnInit(): void {
	  this.prepare();
  }
  
  prepare(){
	  this.classes = new Array<IClass>();
	  this.edges = new Array<Edge>();
	  this.vertexes = new Array<Vertex>();
	  this.links = new Array<Link>();
  }
  ngAfterViewInit(){
	  this.getClassEsquema();
  }
  
  public reprocessClassEsquema(){
	  console.log("get esquema");
	  this
	  	.restService
	  	.getClasses()
	  	.pipe(x => this.getRelations(x))
	  	.pipe(map(x => this.processVertexes(x)))
	  	.subscribe(
	  			{
	  				next: classes=> this.processClasses(classes)
	  						.subscribe({
	  							next: next => this.prepareDiagram(next)
	  						})
	  			});
  }
  
  public getClassEsquema(){
	  console.log("get esquema");
	  this
	  	.restService
	  	.getClasses()
	  	.pipe(x => this.getRelations(x))
	  	.pipe(map(x => this.processVertexes(x)))
	  	.subscribe(
	  			{
	  				next: classes=> this.processClasses(classes)
	  						.subscribe({
	  							next: next => this.prepareDiagram(next)
	  						})
	  			});
  }
  
  public processVertexes(x:any){
	  let i = 0;
	  for (var key in x){
		  let vertex:Vertex;
		  vertex = {key: key , name: x[key] , color: "red"};
		  this.vertexes.push(vertex);
	  }
	  return x;
  }
  
  public getRelations(x:any):any{
	  this.prepare();
	  console.log('getting relations' , x);
	  this
	  	.restService
	  	.getGraph()
	  	.subscribe(
	  			{
	  				next: edges=> this.processEdges(edges)
	  			});
	  return x;
  }

  public processEdges(edges:any){
  
	  try {
		     let i = 0;
		     for(var key in edges){
		    	 let edge = { source: edges[key].source , target : edges[key].target};
		    	 let link = {from : edges[key].source , to  : edges[key].target};
		    	 this.edges.push (edge);
		    	 this.links.push(link);
		     }
		      }catch(e){console.log('an error');}
}
	public getLinkAlias(l:Link):Link{
	  let fromName = this.classes.filter(cls => cls.className == l.from).map(cls => cls.classAlias)[0]; 	
	  let toName = this.classes.filter(cls => cls.className == l.to).map(cls => cls.classAlias)[0];	
	  return {from: fromName , to : toName};
  	}

  public processClasses(response : any):Observable<any> {
	  try {
		     let i = 0;
		     for(var key in response){
		    	 let clazz = { classAlias: key , className : response[key]};
		    	 this.classes.push (clazz);
		     }
		      }catch(e){console.log('an error');}
	  let linkData = new Array<Link>();
	  this.links
	  .forEach (l =>{
		 let theLink =   this.getLinkAlias(l);
		 linkData.push(theLink);
	  });
	  
	  this.model = {nodeDataArray: this.vertexes , linkDataArray: linkData};
	  return new Observable<any>(
		  subscriber => {
			  subscriber.next(this.model);
		  }
	  );
  }
  	
  public prepareDiagram(model:Model){
	  const $ = go.GraphObject.make;

	  if (!this.diagram){
	  	this.diagram = new go.Diagram(this.input.nativeElement);
	  	 let layout = new go.TreeLayout(  { angle: 0, nodeSpacing: 10, layerSpacing: 30 });
		    
			this.diagram.attach(
					{
						  layout: layout
					});
	}
	this.diagram.model = new go.GraphLinksModel(
			 			 model.nodeDataArray,
			 			 model.linkDataArray
	);
	  console.log('is it working?');
	  this.diagram.nodeTemplate =
			    $(go.Node, 'Auto',  // the Shape will go around the TextBlock
			      $(go.Shape, 'RoundedRectangle', { strokeWidth: 0, fill: 'white' },
			        // Shape.fill is bound to Node.data.color
			        new go.Binding('fill', 'color')),
			      $(go.TextBlock,
			        { margin: 8 },  // some room around the text
			        // TextBlock.text is bound to Node.data.key
			        new go.Binding('text', 'key')), 
			      {
			    	click: (e,obj) =>  this.showMessage("Clicked on " ,  obj,e)
			      }
			    );
  }
  
  public showMessage(e:string , obj?:any,evt?:any){
  	this.messageService.add({severity:'success', summary:'Control', detail:'Via MessageService ' + obj.part.data.key});
  	this.showDialog(obj , evt);
  }
  
  display: boolean = false;
  selectedVertex:string;
  
  public showDialog(obj?:any , evt?:any){
	  this.selectedVertex =  obj.part.data.key;
	  this.processElementData (obj.part.data);
  }
  
  public processElementData(data:any){
	  let classAlias = data.key.toLowerCase();
	  let className = data.name;
	  this
	  	.restService
	  	.proccessMetaClassUrl(classAlias.toLowerCase() + "/metaclass")
	  	.subscribe({
	  		next: next => this.processMetaClass(classAlias , className, next)
	  	});
  }
  toggle:boolean = false;
  
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
	  let identity = <Identity>next.identity;
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
	  this.selectedClass = <IMetaClass>{
		  classAlias: classAlias,
		  className: className,
		  metaAttributes: selectedAttr,
		  idendity: identity
	  };
	 }else {
		 this.selectedClass = <IMetaClass>{};
	 }
	 this.display=true;
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
  
  
  public processSubGrafo(){
	  if (this.selectedClass && this.selectedClass.classAlias){
		  let alias = this.selectedClass.classAlias;
		  console.log("alias " + alias);
		  let filteredLinks = this.model.linkDataArray.filter (l => l.from?.toLowerCase()==alias || l.to?.toLowerCase()==alias);
		  let filteredVertex = this.model.nodeDataArray.filter (v => this.filterVertexes(filteredLinks , v));
		  
		  const $ = go.GraphObject.make;

		this.diagram.model = new go.GraphLinksModel(
				filteredVertex,
				filteredLinks
		);
		  console.log('is it working? processSubGrafo');
		  this.diagram.nodeTemplate =
				    $(go.Node, 'Auto',  // the Shape will go around the TextBlock
				      $(go.Shape, 'RoundedRectangle', { strokeWidth: 0, fill: 'white' },
				        // Shape.fill is bound to Node.data.color
				        new go.Binding('fill', 'color')),
				      $(go.TextBlock,
				        { margin: 8 },  // some room around the text
				        // TextBlock.text is bound to Node.data.key
				        new go.Binding('text', 'key')), 
				      {
				    	click: (e,obj) =>  this.showMessage("Clicked on " ,  obj,e)
				      }
				    );
	  }
  }
  
  public filterVertexes(edges?:Link[]  , v?:any):boolean{
	  let ary = edges?.filter (e => e.from == v.key || e.to == v.key);
	  return  ary && ary.length > 0 ? true :false;
  } 
  
}


export interface Edge {
	source: string;
	target: string;
}

export interface Link {
	from?: string;
	to?: string;
}

export interface Vertex {
	key:string;
	name:string;
	color?:string;
}	

export interface Model{
	nodeDataArray :Vertex[];	
	linkDataArray : Link[];
}