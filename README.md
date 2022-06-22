## crawler-data-service
A simple experiment on persistence/repository/controller generation based on db schema
# Database Platform under usage - Postgres (since i have a schema to use)
This is an early version, the basic idea is generate a workable persistence layer using
jpa and spring to provide an openapi for each table in the schema crawled. 
this version probably will work. still have some hard code ahead but then we are talking about what we love.

Many thanks for those who trust and support me.

Its working for small schemas. on practice databases are exploited and the things will became slow.. we can fix altering schema-crawler to avoid some excessive scan.

Thanks to bytebuddy and schema-crawler the thing works. 


Recommended articles: 

[Great view on what is an object oriented programming] (https://www.youtube.com/watch?v=JDG2m5hN1vo&list=RDJDG2m5hN1vo&start_radio=1)
[Code code code : (https://www.youtube.com/watch?v=KB9dso-h_Es)]
- version 0.0.1-snapshot.
