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

export interface IAttribute{
	clazz:string; 
	columnName:string;
	fieldName:string;
	isId: boolean;
	length: number;
	sqlType: string, 
	required: boolean
}


export interface IClass {
	classAlias:string; 
	className: string;
}


export interface ISearchContainer {
	entity:any;
	sortParameters:Map<string,any>;
}

export interface IColumn{
}

export interface Identity {
	shortName:string; 
	definition?:string;
	columns?:Array<any>;
}

export interface IMetaClass extends IClass{
	metaAttributes : Array<IAttribute>;
	identity?: Identity;
}