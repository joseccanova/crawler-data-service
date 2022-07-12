import { Injectable } from '@angular/core';
import { HttpClient , HttpHeaders } from '@angular/common/http'
import { Observable } from 'rxjs';

import { ISearchContainer } from 'src/app/class-model/class-model';

@Injectable({
  providedIn: 'root'
})
export class RestService {

	
  homeUrl = 'http://localhost:4200/api' 	
  classesUrl = '/model_relations/classes';	
  mappingsUrl = '/actuator/mappings';	
  graph="/model_relations/graph";
	
  constructor(protected client2 : HttpClient) { 
  }
  
  
  public getGraph () : Observable<any> {
	  return this.client2
			  	  .request<any>('GET', this.homeUrl + this.graph , {observe:'body' , headers: RestService.buildHttpHeaders ()});
  }
  
  
  public getClasses () : Observable<any> {
	  return this.client2
			  	  .request<any>('GET', this.homeUrl + this.classesUrl , {observe:'body'});
  }
  
  public getMappings () : Observable<any> {
	//contexts.mb-data-service.mappings.dispatcherServlets.dispatcherServlet[80].details.requestMappingConditions.patterns  
	  return this.client2
			  	  .request<any>('GET', this.homeUrl + this.mappingsUrl, {observe:'body' , headers : RestService.buildHttpHeaders ()});
  }
  
  public static buildHttpHeaders () : HttpHeaders 
  {
	  let headers  = new HttpHeaders({'Content-Type':  'application/json',
		  	'Accept' : '*/*' });
	  return headers;
  }
  
  public proccessMetaClassUrl(url:any):Observable<any>{
	  return this.client2
		  	  .request<any>('GET', this.homeUrl + url, {observe:'body' , headers : RestService.buildHttpHeaders ()});
  }
  
  public processSampleUrl(url:any):Observable<any>{
	  return this.client2
		  	  .request<any>('GET', this.homeUrl + url, {observe:'body' , body:{} ,  headers : RestService.buildHttpHeaders ()});
  }
  
  
  public proccessSearch(url?:any , container?:ISearchContainer):Observable<any>{
	  return this.client2.post( this.homeUrl + url ,  container  , {observe:'body' ,  headers : RestService.buildHttpHeaders ()});
  }
}
