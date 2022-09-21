## crawler-data-service
A simple experiment on persistence/repository/controller generation based on db schema
### Database Platform under usage - Postgres (since i have a schema to use)
This is an early version, the basic idea is generate a workable persistence layer using
jpa and spring to provide an openapi for each table in the schema crawled. 
this version probably will work. still have some hard code ahead but then we are talking about what we love.

Thanks to bytebuddy and schema-crawler the thing works. 

![](https://docs.google.com/drawings/d/e/2PACX-1vQrZjz_1AdIjHxAOfh9IzEe8aNKx94QDwpw9fO40FKVH9ktFe9mDiARtz0pmSXm4G8bkOAIkqGFQZoB/pub?w=640&h=480)

Recommended articles: 


-------------------------
###Maybe could help on this topic... 
https://martinfowler.com/data/

### Found OrientDB i remember already put my eyes on it. 
https://orientdb.org/docs/3.0.x/

### and this article. 

https://hal.archives-ouvertes.fr/hal-01803448/document

### Oracle Property Graphs. didn't get it how it works.

