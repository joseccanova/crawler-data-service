package org.nanotek.crawler.data.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import static org.springframework.http.ResponseEntity.*;

import static org.springframework.web.servlet.function.RequestPredicates.*;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import static org.springframework.web.servlet.function.RouterFunctions.*;

@SpringBootConfiguration(proxyBeanMethods = false)
public class GraphQlConfig {

//    @Bean
//    public <B extends Base<?,ID> , ID> RuntimeWiringConfigurer runtimeWiringConfigurer(BaseRepository<B, ID> repository) {
//
////        GraphQLScalarType scalarType = ... ;
////        SchemaDirectiveWiring directiveWiring = ... ;
//        DataFetcher<B> dataFetcher = QuerydslDataFetcher.builder(repository).single();
//        return wiringBuilder -> wiringBuilder
////                .scalar(scalarType)
////                .directiveWiring(directiveWiring)
//                .type("Query", builder -> builder.dataFetcher("baseEntity", dataFetcher));
//    }
//    
//    @Bean 
//    public <B extends Base<?,ID> , ID>  DataFetcher<B> singleDataFetcher(BaseRepository<B, ID> repository){
//    	return QuerydslDataFetcher.builder(repository).single();
//    }
//	
//	@Bean
//	public RouterFunction<ServerResponse> productListing(ProductService ps) {
//	    return RouterFunctions.route().GET("/product", req -> ok().body(ps.findAll()))
//	      .build();
//	}
}
